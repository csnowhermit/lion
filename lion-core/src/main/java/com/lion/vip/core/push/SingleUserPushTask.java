package com.lion.vip.core.push;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.message.Message;
import com.lion.vip.api.spi.push.IPushMessage;
import com.lion.vip.common.message.PushMessage;
import com.lion.vip.common.qps.FlowControl;
import com.lion.vip.common.router.RemoteRouter;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.ack.AckTask;
import com.lion.vip.core.router.LocalRouter;
import com.lion.vip.tools.common.TimeLine;
import com.lion.vip.tools.log.Logs;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 单用户推送任务
 */
public final class SingleUserPushTask implements PushTask, ChannelFutureListener {
    private final FlowControl flowControl;
    private final IPushMessage message;
    private int messageId;
    private long start;
    private final TimeLine timeLine = new TimeLine();
    private final LionServer lionServer;

    public SingleUserPushTask(LionServer lionServer, IPushMessage message, FlowControl flowControl) {
        this.flowControl = flowControl;
        this.message = message;
        this.lionServer = lionServer;
        this.timeLine.begin("push-center-begin");
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return ((Message) message).getConnection().getChannel().eventLoop();
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (checkTimeout()) {
            return;
        }

        if (future.isSuccess()) {    //推送成功
            if (message.isNeedAck()) {//需要客户端ACK, 添加等待客户端响应ACK的任务
                addAckTask(messageId);
            } else {
                lionServer.getPushCenter().getPushListener().onSuccess(message, timeLine.successEnd().getTimePoints());
            }

            Logs.PUSH.info("[SingleUserPush] push message to client success, timeLine={}, message={}", timeLine, message);
        } else {    //推送失败
            lionServer.getPushCenter().getPushListener().onFailure(message, timeLine.failureEnd().getTimePoints());
            Logs.PUSH.error("[SingleUserPush] push message to client failure, message={}, conn={}", message, future.channel());
        }
    }

    /**
     * 添加ACK任务到队列, 等待客户端响应
     *
     * @param messageId
     */
    private void addAckTask(int messageId) {
        timeLine.addTimePoint("waiting-ack");

        //因为要进队列，可以提前释放一些比较占用内存的字段，便于垃圾回收
        message.finalized();

        AckTask task = AckTask.from(messageId)
                .setCallback(new PushAckCallback(message, timeLine, lionServer.getPushCenter()));

        lionServer.getPushCenter().getAckTaskQueue().add(task, message.getTimeoutMills() - (int) (System.currentTimeMillis() - start));

    }

    @Override
    public void run() {
        if (checkTimeout()) {    //超时
            return;
        }

        if (checkLocal(message)) {    //如果本地存在连接，直接返回
            return;
        }

        checkRemote(message);    //本地连接不存在，检测远程路由
    }

    /**
     * 检测是否超时
     *
     * @return 超时，返回true；否则返回false
     */
    private boolean checkTimeout() {
        if (start > 0) {
            if (System.currentTimeMillis() - start > message.getTimeoutMills()) {
                lionServer.getPushCenter().getPushListener().onTimeout(message, timeLine.timeoutEnd().getTimePoints());
                Logs.PUSH.info("[SingleUserPush] push message to client timeout, timeLine={}, message={}", timeLine, message);

                return true;
            }
        } else {
            start = System.currentTimeMillis();
        }
        return false;
    }

    /**
     * 检测是否本地存在连接
     *
     * @param message
     * @return
     */
    private boolean checkLocal(IPushMessage message) {
        String userId = message.getUserId();
        int clientType = message.getClientType();

        LocalRouter localRouter = lionServer.getRouterCenter().getLocalRouterManager().lookup(userId, clientType);

        //1.检测本地路由是否存在
        if (localRouter == null) {    //如果本地路由不存在
            return false;
        }

        Connection connection = localRouter.getRouteValue();

        //2.检测是否已连接
        if (!connection.isConnected()) {    //如果连接失效，则先删除本地路由，再查远程路由
            Logs.PUSH.warn("[SingleUserPush] find local router but conn disconnected, message={}, conn={}", message, connection);
            lionServer.getRouterCenter().getLocalRouterManager().unregister(userId, clientType);
            return false;
        }

        //3.检测channel通道是否可写：检测TCP缓冲区是否已满且写队列超过最高阀值
        if (!connection.getChannel().isWritable()) {    //如果不可写
            lionServer.getPushCenter().getPushListener().onFailure(message, timeLine.failureEnd().getTimePoints());

            Logs.PUSH.error("[SingleUserPush] push message to client failure, tcp sender too busy, message={}, conn={}", message, connection);
            return true;
        }

        //4.检测qps, 是否超过流控限制，如果超过则进队列延后发送
        if (flowControl.checkQps()) {
            timeLine.addTimePoint("before-send");
            //5.连接可用，直接下发消息到手机客户端
            PushMessage pushMessage = PushMessage.build(connection).setContent(message.getContent());
            pushMessage.getPacket().addFlag(message.getFlags());
            messageId = pushMessage.getSessionId();
            pushMessage.send(this);
        } else {   //超过流控限制, 进队列延后发送
            lionServer.getPushCenter().delayTask(flowControl.getDelay(), this);
        }
        return true;
    }


    /**
     * 检测远程路由：如果不存在，则直接返回 用户已下线；
     * 如果是本机，则直接删除路由信息；
     * 如果是其他机器，则让PushClient重推；
     *
     * @param message
     */
    private void checkRemote(IPushMessage message) {

        String userId = message.getUserId();
        int clientType = message.getClientType();
        RemoteRouter remoteRouter = lionServer.getRouterCenter().getRemoteRouterManager().lookup(userId, clientType);

        //1.如果远程路由信息也不存在，说明该用户此时不在线
        if (remoteRouter == null || remoteRouter.isOffline()) {
            lionServer.getPushCenter().getPushListener().onOffline(message, timeLine.end("offline-end").getTimePoints());

            Logs.PUSH.info("[SingleUserPush] remote router not exists user offline, message={}, ");
            return;
        }

        //2.如果查出的远程机器是当前机器，说明路由已经失效，此时用户已下线，需要删除失效的缓存
        if (remoteRouter.getRouteValue().isThisMachine(lionServer.getGatewayServerNode().getHost(), lionServer.getGatewayServerNode().getPort())) {
            lionServer.getPushCenter().getPushListener().onOffline(message, timeLine.end("offline-end").getTimePoints());

            //删除失效的远程缓存
            lionServer.getRouterCenter().getRemoteRouterManager().unregister(userId, clientType);

            Logs.PUSH.info("[SingleUserPush] find remote router in this server, but local router not exists, userId={}, clientType={}, router={}, ", userId, clientType, remoteRouter);
            return;
        }

        //3.否则说明用户在另外一台机器上，路由信息发生变更，让PushClient重推
        lionServer.getPushCenter().getPushListener().onRedirect(message, timeLine.end("redirect-end").getTimePoints());

        Logs.PUSH.info("[SingleUserPush] find router in another server, userId={}, clientType={}, router={}", userId, clientType, remoteRouter);
    }


}
