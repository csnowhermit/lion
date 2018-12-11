package com.lion.vip.core.ack;

public interface AckCallback {
    void onSuccess(AckTask context);

    void onTimeout(AckTask context);
}
