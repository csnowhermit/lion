/**
 * FileName: HttpClientHandler
 * Author:   Ren Xiaotian
 * Date:     2018/11/23 17:02
 */

package com.lion.vip.network.netty.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理Http请求的具体处理逻辑
 */
@ChannelHandler.Sharable
public class HttpClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientHandler.class);
    private NettyHttpClient nettyHttpClient;

    public HttpClientHandler(NettyHttpClient nettyHttpClient) {
        this.nettyHttpClient = nettyHttpClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        RequestContext requestContext = ctx.channel().attr(nettyHttpClient.requestKey).getAndSet(null);

        try {
            if (requestContext != null && requestContext.tryDone()) {
                requestContext.onException(cause);
            }
        } finally {
            nettyHttpClient.pool.tryRelease(ctx.channel());
        }
        LOGGER.error("http client caught an ex, info={}", requestContext, cause);
    }
}
