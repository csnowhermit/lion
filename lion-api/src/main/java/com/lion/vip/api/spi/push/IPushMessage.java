/**
 * FileName: IPushMessage
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 10:40
 */

package com.lion.vip.api.spi.push;

import com.lion.vip.api.common.Condition;

public interface IPushMessage {
    boolean isBroadcast();

    String getUserId();

    int getClientType();

    byte[] getContent();

    boolean isNeedAck();

    byte getFlags();

    int getTimeoutMills();

    default String getTaskId() {
        return null;
    }

    default Condition getCondition() {
        return null;
    }

    default void finalized() {

    }
}
