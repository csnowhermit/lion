/**
 * FileName: InfoQuota
 * Author:   Ren Xiaotian
 * Date:     2018/11/29 20:37
 */

package com.lion.vip.monitor.quota;

public interface InfoQuota extends MonitorQuota {
    String pid();

    double load();
}
