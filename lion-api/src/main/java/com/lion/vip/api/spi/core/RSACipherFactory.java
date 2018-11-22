/**
 * FileName: RSACipherFactory
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 9:56
 */

package com.lion.vip.api.spi.core;

import com.lion.vip.api.connection.Cipher;
import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

/**
 * RSA加解密算法：工厂类
 */
public interface RSACipherFactory extends Factory<RSACipherFactory> {
    static Cipher create(){
        return (Cipher) SpiLoader.load(RSACipherFactory.class).get();
    }
}
