package com.lion.vip.monitor.quota;

/**
 * 线程配额监控
 */
public interface ThreadQuota extends MonitorQuota {

    int daemonThreadCount();

    int threadCount();

    long totalStartedThreadCount();

    int deadLockedThreadCount();

}
