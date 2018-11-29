/**
 * FileName: ResultCollector
 * Author:   Ren Xiaotian
 * Date:     2018/11/29 22:00
 */

package com.lion.vip.monitor.data;

import com.lion.vip.monitor.quota.impl.*;
import com.lion.vip.monitor.service.ThreadPoolManager;

/**
 * 监控结果收集
 */
public class ResultCollector {
    private final JVMInfo jvmInfo;
    private final JVMGC jvmgc;
    private final JVMMemory jvmMemory;
    private final JVMThread jvmThread;
    private final JVMThreadPool jvmThreadPool;

    public ResultCollector(ThreadPoolManager threadPoolManager) {
        this.jvmInfo = new JVMInfo();
        this.jvmgc = new JVMGC();
        this.jvmMemory = new JVMMemory();
        this.jvmThread = new JVMThread();
        this.jvmThreadPool = new JVMThreadPool(threadPoolManager);
    }

    public MonitorResult collect() {
        MonitorResult result = new MonitorResult();
        result.addResult("jvm-info", jvmInfo.monitor());
        result.addResult("jvm-gc", jvmgc.monitor());
        result.addResult("jvm-memory", jvmMemory.monitor());
        result.addResult("jvm-thread", jvmThread.monitor());
        result.addResult("jvm-thread-pool", jvmThreadPool.monitor());
        return result;
    }


    public JVMInfo getJvmInfo() {
        return jvmInfo;
    }

    public JVMGC getJvmgc() {
        return jvmgc;
    }

    public JVMMemory getJvmMemory() {
        return jvmMemory;
    }

    public JVMThread getJvmThread() {
        return jvmThread;
    }

    public JVMThreadPool getJvmThreadPool() {
        return jvmThreadPool;
    }
}
