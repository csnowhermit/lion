package com.lion.vip.test.util;

import com.lion.vip.tools.Utils;
import org.junit.Test;

/**
 * IP测试
 */
public class IPTest {
    @Test
    public void getLocalIP() throws Exception {
        System.out.println(Utils.lookupLocalIP());
        System.out.println(Utils.lookupExtranetIp());

    }
}
