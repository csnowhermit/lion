package com.lion.vip.common.security;

import com.lion.vip.api.connection.Cipher;
import com.lion.vip.tools.crypto.RSAUtils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public final class RSACipher implements Cipher {
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public RSACipher(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return RSAUtils.decryptByPrivateKey(data, privateKey);
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return RSAUtils.encryptByPublicKey(data, publicKey);
    }

    @Override
    public String toString() {
        return "RsaCipher [privateKey=" + new String(privateKey.getEncoded()) + ", publicKey=" + new String(publicKey.getEncoded()) + "]";
    }

    public static RSACipher create() {
        return new RSACipher(CipherBox.I.getPrivateKey(), CipherBox.I.getPublicKey());
    }
}
