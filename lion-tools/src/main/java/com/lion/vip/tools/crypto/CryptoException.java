/**
 * FileName: CryptoException
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 16:28
 */

package com.lion.vip.tools.crypto;

import java.io.Serializable;

/**
 * 自定义：加解密异常类
 */
public class CryptoException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1574695216879440019L;

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
