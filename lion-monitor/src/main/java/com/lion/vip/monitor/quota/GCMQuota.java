/**
 * FileName: GCMQuota
 * Author:   Ren Xiaotian
 * Date:     2018/11/29 20:30
 */

package com.lion.vip.monitor.quota;

/**
 * GC配额监控
 */
public interface GCMQuota extends MonitorQuota {

    /**
     * 新生代GC收集次数
     *
     * @return
     */
    long yongGcCollectionCount();

    /**
     * 新生代GC收集时间
     *
     * @return
     */
    long yongGcCollectionTime();

    /**
     * 全内存GC收集次数
     *
     * @return
     */
    long fullGcCollectionCount();

    /**
     * 全内存GC收集时间
     *
     * @return
     */
    long fullGcCollectionTime();

    long spanYongGcCollectionCount();

    long spanYongGcCollectionTime();

    long spanFullGcCollectionCount();

    long spanFullGcCollectionTime();
}
