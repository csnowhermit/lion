package com.lion.vip.network.netty.http;

import com.google.common.primitives.Ints;
import com.lion.vip.api.Constants;
import com.lion.vip.tools.config.CC;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

/**
 * Request 上下文
 */
public class RequestContext implements TimerTask, HttpCallback {
    private static final int TIMEOUT = CC.lion.http.default_read_timeout;    //http默认的超时时间
    private final long startTime = System.currentTimeMillis();
    private AtomicBoolean cancelled = new AtomicBoolean(false);    //是否已取消
    final int readTimeout;
    private long endTime = startTime;
    private String uri;
    private HttpCallback callback;
    FullHttpRequest request;
    String host;

    public RequestContext(int readTimeout, String uri, HttpCallback callback, FullHttpRequest request) {
        this.readTimeout = readTimeout;
        this.uri = uri;
        callback = callback;
        this.request = request;
    }

    public RequestContext(FullHttpRequest request, HttpCallback callback) {
        this.callback = callback;
        this.request = request;
        this.uri = request.uri();
        this.readTimeout = parseTimeout();
    }

    private int parseTimeout() {
        String timeout = request.headers().get(Constants.HTTP_HEAD_READ_TIMEOUT);
        if (timeout != null) {
            request.headers().remove(Constants.HTTP_HEAD_READ_TIMEOUT);
            Integer integer = Ints.tryParse(timeout);
            if (integer != null && integer > 0) {
                return integer;
            }
        }
        return TIMEOUT;
    }

    public int getReadTimeout() {
        return readTimeout;
    }


    @Override
    public void onResponse(HttpResponse response) {
        callback.onResponse(response);
        endTime = System.currentTimeMillis();
        destory();
    }

    private void destory() {
        this.request = null;
        this.callback = null;
    }

    @Override
    public void onFailure(int statusCode, String reasonPhrase) {
        callback.onFailure(statusCode, reasonPhrase);
        endTime = System.currentTimeMillis();
        destory();
    }

    @Override
    public void onException(Throwable throwable) {
        callback.onException(throwable);
        endTime = System.currentTimeMillis();
        destory();
    }

    @Override
    public void onTimeout() {
        callback.onTimeout();
        endTime = System.currentTimeMillis();
        destory();
    }

    @Override
    public boolean onRedirect(HttpResponse response) {
        endTime = System.currentTimeMillis();
        return callback.onRedirect(response);
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        if (tryDone()) {
            if (callback != null) {
                callback.onTimeout();
            }
        }
    }

    /**
     * 由于检测请求超时的任务存在，为了防止多线程下重复处理
     *
     * @return
     */
    public boolean tryDone() {
        return cancelled.compareAndSet(false, true);
    }

    @Override
    public String toString() {
        return "RequestContext{" +
                "startTime=" + startTime +
                ", cancelled=" + cancelled +
                ", readTimeout=" + readTimeout +
                ", uri='" + uri + '\'' +
                ", callback=" + callback +
                ", request=" + request +
                ", host='" + host + '\'' +
                '}';
    }
}
