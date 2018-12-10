package com.lion.vip.core.session;

import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.common.security.AESCipher;

/**
 * 可重复使用的session
 */
public final class ReusableSession {
    public String sessionId;
    public long expireTime;
    public SessionContext context;

    public static String encode(SessionContext context) {
        StringBuffer sb = new StringBuffer();
        sb.append(context.osName).append(',');
        sb.append(context.osVersion).append(',');
        sb.append(context.clientVersion).append(',');
        sb.append(context.deviceId).append(',');
        sb.append(context.cipher);
        return sb.toString();
    }

    public static ReusableSession decode(String value) {
        String[] array = value.split(",");
        if (array.length != 6) return null;
        SessionContext context = new SessionContext();
        context.osName = array[0];
        context.osVersion = array[1];
        context.clientVersion = array[2];
        context.deviceId = array[3];
        byte[] key = AESCipher.toArray(array[4]);
        byte[] iv = AESCipher.toArray(array[5]);
        if (key == null || iv == null) return null;
        context.cipher = new AESCipher(key, iv);
        ReusableSession session = new ReusableSession();
        session.context = context;
        return session;
    }
}
