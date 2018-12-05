package com.lion.vip.monitor.quota;

public interface InfoQuota extends MonitorQuota {
    String pid();

    double load();
}
