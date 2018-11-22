/**
 * FileName: ClientClassifier
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 16:12
 */

package com.lion.vip.api.router;

import com.lion.vip.api.spi.router.ClientClassifierFactory;

public interface ClientClassifier {
    ClientClassifier I = ClientClassifierFactory.create();

    int getClientType(String osName);
}
