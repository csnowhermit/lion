/**
 * FileName: ClientClassifier
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 16:12
 */

package com.lion.vip.api.router;

public interface ClientClassifier {
    ClientClassifier I = ClientClassifierFactory.create();

    int getClientType(String osName);
}
