package com.lion.vip.client.push;

import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.client.LionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PushRequestBus extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(PushRequestBus.class);
    private final Map<Integer, PushRequest> requestMap = new ConcurrentHashMap<>(1024);
    private ScheduledExecutorService scheduledExecutorService;
    private final LionClient lionClient;

    public PushRequestBus(LionClient lionClient) {
        this.lionClient = lionClient;
    }

    public Future<?> put(int sessionId, PushRequest request) {
        requestMap.put(sessionId, request);
        return scheduledExecutorService.schedule(request, request.getTimeout(), TimeUnit.MILLISECONDS);
    }

    public PushRequest getAndRemove(int sessionId) {
        return requestMap.remove(sessionId);
    }

    public void asyncCall(Runnable runnable) {
        scheduledExecutorService.execute(runnable);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        scheduledExecutorService = lionClient.getThreadPoolManager().getPushClientTimer();
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }

        listener.onSuccess();
    }
}
