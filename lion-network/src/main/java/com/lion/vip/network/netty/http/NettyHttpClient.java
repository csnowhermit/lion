package com.lion.vip.network.netty.http;

import com.lion.vip.api.service.BaseService;
import com.lion.vip.tools.config.CC;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.util.AttributeKey;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpHeaderNames.KEEP_ALIVE;

public class NettyHttpClient extends BaseService implements HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpClient.class);
    private static final int maxContentLength = (int) CC.lion.http.max_content_length;
    final AttributeKey<RequestContext> requestKey = AttributeKey.newInstance("request");
    final HttpConnectionPool pool = new HttpConnectionPool();

    private Bootstrap bootstrap;
    private EventLoopGroup eventExecutors;
    private Timer timer;

    @Override
    public void request(RequestContext context) throws Exception {
        URI uri = new URI(context.request.uri());
        String host = context.host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        //1.设置请求头
        context.request.headers().set(HOST, host);               //映射后的host
        context.request.headers().set(CONNECTION, KEEP_ALIVE);   //保存长连接

        //2.添加请求超时检测队列


//        //2.添加请求超时检测队列
//        timer.newTimeout(context, context.readTimeout, TimeUnit.MILLISECONDS);
//
//        //3.先尝试从连接池里取可用链接，去取不到就创建新链接。
//        Channel channel = pool.tryAcquire(host);
//        if (channel == null) {
//            final long startCreate = System.currentTimeMillis();
//            LOGGER.debug("create new channel, host={}", host);
//            ChannelFuture f = b.connect(host, port);
//            f.addListener((ChannelFutureListener) future -> {
//                LOGGER.debug("create new channel cost={}", (System.currentTimeMillis() - startCreate));
//                if (future.isSuccess()) {//3.1.把请求写到http server
//                    writeRequest(future.channel(), context);
//                } else {//3.2如果链接创建失败，直接返回客户端网关超时
//                    context.tryDone();
//                    context.onFailure(504, "Gateway Timeout");
//                    LOGGER.warn("create new channel failure, request={}", context);
//                }
//            });
//        } else {
//            //3.1.把请求写到http server
//            writeRequest(channel, context);
//        }

    }


}
