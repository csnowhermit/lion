package com.lion.vip.core.handler;

import com.google.common.base.Strings;
import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.api.spi.net.DNSMapping;
import com.lion.vip.api.spi.net.DNSMappingManager;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.HttpRequestMessage;
import com.lion.vip.common.message.HttpResponseMessage;
import com.lion.vip.core.LionServer;
import com.lion.vip.network.netty.http.HttpCallback;
import com.lion.vip.network.netty.http.HttpClient;
import com.lion.vip.network.netty.http.RequestContext;
import com.lion.vip.tools.common.Profiler;
import com.lion.vip.tools.log.Logs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * http代理的处理类
 */
public class HttpProxyHandler extends BaseMessageHandler<HttpRequestMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyHandler.class);
    private final DNSMappingManager dnsMappingManager = DNSMappingManager.create();
    private final HttpClient httpClient;

    public HttpProxyHandler(LionServer lionServer) {
        this.httpClient = lionServer.getHttpClient();
    }

    @Override
    public HttpRequestMessage decode(Packet packet, Connection connection) {
        return new HttpRequestMessage(packet, connection);
    }

    @Override
    public void handle(HttpRequestMessage message) {
        //1.参数校验
        String method = message.getMethod();
        String uri = message.uri;
        if (Strings.isNullOrEmpty(uri)) {
            HttpResponseMessage.from(message)
                    .setStatusCode(400)
                    .setReasonPhrase("Bad Request")
                    .sendRaw();
            Logs.HTTP.warn("receive bad request url is empty, request={}", message);
        }

        //2.url转换
        uri = doDNSMapping(uri);
        Profiler.enter("time cost on [create FullHttpRequest]");

        //3.包装成HttpRequest
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method), uri, getBody(message));
        setHeaders(request, message);    //设置header
        Profiler.enter("time cost on [HttpClient.request]");

        //4.发送请求
        httpClient.request(new RequestContext(request, new DefaultFullHttpCallback(message)));
    }

    /**
     * 得到request body部分
     *
     * @param message
     * @return
     */
    private ByteBuf getBody(HttpRequestMessage message) {
        return message.body == null ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(message.body);
    }

    /**
     * 设置http请求头
     *
     * @param request
     * @param message
     */
    private void setHeaders(FullHttpRequest request, HttpRequestMessage message) {
        Map<String, String> headers = message.headers;
        if (headers != null) {
            HttpHeaders httpHeaders = request.headers();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }

        if (message.body != null && message.body.length > 0) {
            request.headers().add(CONTENT_LENGTH, Integer.toString(message.body.length));
        }

        InetSocketAddress remoteAddress = (InetSocketAddress) message.getConnection();
        String remoteIP = remoteAddress.getAddress().getHostAddress();
//        String remoteIP = remoteAddress.getAddress().getHostName();    //使用getHostName()耗时较长
        request.headers().add("x-forwarded-for", remoteIP);
        request.headers().add("x-forwarded-post", Integer.toString(remoteAddress.getPort()));
    }

    /**
     * url做DNS转换
     *
     * @param urlStr
     * @return
     */
    private String doDNSMapping(String urlStr) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url == null) {
            return urlStr;
        }

        String host = url.getHost();
        DNSMapping mapping = dnsMappingManager.lookup(host);
        if (mapping == null) {
            return urlStr;
        }

        return mapping.translate(url);
    }

    /**
     * 默认http回调类
     */
    private static class DefaultFullHttpCallback implements HttpCallback {
        private final HttpRequestMessage request;
        private int redirectCount;

        public DefaultFullHttpCallback(HttpRequestMessage message) {
            this.request = message;
        }

        @Override
        public void onResponse(HttpResponse httpResponse) {
            HttpResponseMessage response = HttpResponseMessage.from(request)
                    .setStatusCode(httpResponse.status().code())
                    .setReasonPhrase(httpResponse.status().reasonPhrase());
            for (Map.Entry<String, String> entry : httpResponse.headers()) {
                response.addHeader(entry.getKey(), entry.getValue());
            }

            if (httpResponse instanceof FullHttpResponse) {
                ByteBuf content = ((FullHttpResponse) httpResponse).content();
                if (content != null && content.readableBytes() > 0) {
                    byte[] body = new byte[content.readableBytes()];
                    content.readBytes(body);
                    response.body = body;
                    response.addHeader(CONTENT_LENGTH.toString(), Integer.toString(response.body.length));
                }
            }

            response.send();
            Logs.HTTP.info("send proxy request success end request={}, response={}", request, httpResponse);
        }

        @Override
        public void onFailure(int statusCode, String reasonPhrase) {
            HttpResponseMessage.from(request)
                    .setStatusCode(statusCode)
                    .setReasonPhrase(reasonPhrase)
                    .sendRaw();
            Logs.HTTP.warn("send proxt request failure end request={}, response={}", request, statusCode + ":" + reasonPhrase);
        }

        @Override
        public void onException(Throwable throwable) {
            HttpResponseMessage.from(request)
                    .setStatusCode(502)
                    .setReasonPhrase("Bad Gateway")
                    .sendRaw();

            LOGGER.warn("send proxy request ex end request={}, response={}", request, 502, throwable);
            Logs.HTTP.error("send proxy request ex end request={}, response={}, error={}", request, 502, throwable.getMessage());
        }

        @Override
        public void onTimeout() {
            HttpResponseMessage.from(request)
                    .setStatusCode(408)
                    .setReasonPhrase("Request Timeout")
                    .sendRaw();
            LOGGER.warn("send proxy request timeout end request={}, response={}", request, 408, "request timeout");
            Logs.HTTP.warn("send proxy request timeout end request={}, response={}", request, 408);
        }

        @Override
        public boolean onRedirect(HttpResponse response) {
            return redirectCount++ < 5;
        }
    }
}
