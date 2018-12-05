package com.lion.vip.common.security;

import com.lion.vip.api.connection.Cipher;
import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.core.RSACipherFactory;

@Spi
public class DefaultRsaCipherFactory implements RSACipherFactory {
    private static final RSACipher RSA_CIPHER = RSACipher.create();

    @Override
    public Cipher get() {
        return RSA_CIPHER;
    }
}
