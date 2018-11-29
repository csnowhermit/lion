/**
 * FileName: MonitorQuota
 * Author:   Ren Xiaotian
 * Date:     2018/11/29 20:29
 */

package com.lion.vip.monitor.quota;

/**
 * 配额监控总接口
 */
public interface MonitorQuota {
    Object monitor(Object... args);
}
