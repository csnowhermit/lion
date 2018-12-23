package com.lion.vip.client.push;

import com.lion.vip.api.push.AckModel;
import com.lion.vip.api.push.PushCallback;
import com.lion.vip.api.push.PushResult;
import com.lion.vip.api.router.ClientLocation;
import com.lion.vip.client.LionClient;
import com.lion.vip.common.message.gateway.GatewayPushMessage;
import com.lion.vip.common.router.RemoteRouter;
import com.lion.vip.tools.common.TimeLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.server.RemoteServer;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

public final class PushRequest extends FutureTask<PushResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushRequest.class);
    private static final Callable<PushResult> NONE = () -> new PushResult(PushResult.CODE_FAILURE);

    private enum Status {
        init,
        success,
        failure,
        offline,
        timeout
    }

    ;

    private final AtomicReference<Status> status = new AtomicReference<>(Status.init);
    private final TimeLine timeLine = new TimeLine("Push-Time-Line");

    private final LionClient lionClient;

    private AckModel ackModel;
    private Set<String> tags;
    private String condition;
    private PushCallback callback;
    private String userId;
    private byte[] content;
    private int timeout;
    private ClientLocation location;
    private int sessionId;
    private String taskId;
    private Future<?> future;
    private PushResult result;

    private void send2ConnServer(RemoteRouter remoteRouter) {
        timeLine.addTimePoint("lookup-remote");

        if (remoteRouter != null) {
            location = remoteRouter.getRouteValue();
        }

        if (remoteRouter == null || remoteRouter.isOffline()) {
            //1.没有查到，说明用户已下线
            offline();
            return;
        }

        timeLine.addTimePoint("check-gateway-conn");

        //2.通过网关连接，把消息发送到所在机器
        boolean success = lionClient.getGatewayConnectionFactory().send(
                location.getHostAndPort(),
                connection -> GatewayPushMessage.build(connection)
                        .setUserId(userId)
                        .setContent(content)
                        .setClientType(location.getClientType())
                        .setTimeout(timeout - 500)
                        .setTags(tags)
                        .addFlag(ackModel.flag),
                pushMessage -> {
                    timeLine.addTimePoint("send-to-gateway-begin");
                    pushMessage.sendRaw(f -> {
                        timeLine.addTimePoint("send-to-gateway-end");
                        if (f.isSuccess()) {
                            LOGGER.debug("send to gateway server success, location={}, conn={}", location, f.channel());
                        } else {
                            LOGGER.error("send to gateway server failure, location={}, conn={}", location, f.channel(), f.cause());
                            failure();
                        }
                    });
                    PushRequest.this.content = null;    //释放内存
                    sessionId = pushMessage.getSessionId();
                    future = lionClient.getPushRequestBus().put(sessionId, PushRequest.this);
                }
        );

        if (!success) {
            LOGGER.error("get gateway connection failure, location={}", location);
            failure();
        }
    }

    /**
     * Future 提交任务
     *
     * @param status
     */
    private void submit(Status status) {
        if (this.status.compareAndSet(Status.init, status)) {    //防止重复调用
            boolean isTimeoutEnd = status == Status.timeout;    //任务是否超时结束

            if (future != null && !isTimeoutEnd) {         //如果是超时任务，则不用再取消一次
                future.cancel(true);    //取消超时任务
            }

            this.timeLine.end();    //结束时间流统计
            super.set(getResult());    //设置同步调用的返回结果

            //回调callback
            if (callback != null) {
                if (isTimeoutEnd) {    //超时结束时，当前线程已经是线程池里的线程，直接调用callback
                    callback.onResult(getResult());
                } else {    //非超时结束时，当前线程为Netty线程池，要异步执行callback
                    lionClient.getPushRequestBus().asyncCall(this);
                }
            }
        }
        LOGGER.info("push request {} end, {}, {}, {}", status, userId, location, timeLine);
    }


}
