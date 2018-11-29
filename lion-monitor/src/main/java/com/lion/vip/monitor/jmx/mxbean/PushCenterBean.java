/**
 * FileName: PushCenterBean
 * Author:   Ren Xiaotian
 * Date:     2018/11/29 20:19
 */

package com.lion.vip.monitor.jmx.mxbean;

import com.lion.vip.monitor.jmx.MBeanInfo;

import java.util.concurrent.atomic.AtomicLong;

public final class PushCenterBean implements PushCenterMXBean, MBeanInfo {

    private final AtomicLong taskNum;

    public PushCenterBean(AtomicLong taskNum) {
        this.taskNum = taskNum;
    }

    @Override
    public String getName() {
        return "PushCenter";
    }

    @Override
    public long getTaskNum() {
        return taskNum.get();
    }
}
