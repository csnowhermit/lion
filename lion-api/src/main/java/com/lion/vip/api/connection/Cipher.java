/**
 * FileName: Cipher
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 11:23
 */

package com.lion.vip.api.connection;

/**
 * 数据加解密接口
 */
public interface Cipher {

    /**
     * 数据加密
     *
     * @param data
     * @return
     */
    byte[] encrypt(byte[] data);

    /**
     * 数据解密
     *
     * @param data
     * @return
     */
    byte[] decrypt(byte[] data);
}
