package com.lion.vip.core.session;

import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.api.spi.common.CacheManager;
import com.lion.vip.api.spi.common.CacheManagerFactory;
import com.lion.vip.common.CacheKeys;
import com.lion.vip.tools.common.Strings;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.crypto.MD5Utils;

/**
 * 可重复使用的session 管理器
 */
public final class ReusableSessionManager {
    private final int expiredTime = CC.lion.core.session_expired_time;
    private final CacheManager cacheManager = CacheManagerFactory.create();

    /**
     * 缓存session到CacheManager
     *
     * @param session
     * @return
     */
    public boolean cacheSession(ReusableSession session) {
        String key = CacheKeys.getSessionKey(session.sessionId);
        cacheManager.set(key, ReusableSession.encode(session.context), expiredTime);
        return true;
    }

    public ReusableSession querySession(String sessionId) {
        String key = CacheKeys.getSessionKey(sessionId);
        String value = cacheManager.get(key, String.class);
        if (Strings.isBlank(value)) {
            return null;
        }
        return ReusableSession.decode(value);
    }

    public ReusableSession genSession(SessionContext context) {
        long now = System.currentTimeMillis();
        ReusableSession session = new ReusableSession();
        session.context = context;
        session.sessionId = MD5Utils.encrypt(context.deviceId + now);
        session.expireTime = now + expiredTime * 1000;

        return session;
    }

}
