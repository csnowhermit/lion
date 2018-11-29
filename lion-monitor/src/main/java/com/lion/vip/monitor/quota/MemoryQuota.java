/**
 * FileName: MemoryQuota
 * Author:   Ren Xiaotian
 * Date:     2018/11/29 20:36
 */

package com.lion.vip.monitor.quota;

/**
 * 内存监控
 */
public interface MemoryQuota extends MonitorQuota {

    // Heap
    long heapMemoryCommitted();

    long heapMemoryInit();

    long heapMemoryMax();

    long heapMemoryUsed();

    // NonHeap
    long nonHeapMemoryCommitted();

    long nonHeapMemoryInit();

    long nonHeapMemoryMax();

    long nonHeapMemoryUsed();

    // PermGen
    long permGenCommitted();

    long permGenInit();

    long permGenMax();

    long permGenUsed();

    // OldGen
    long oldGenCommitted();

    long oldGenInit();

    long oldGenMax();

    long oldGenUsed();

    // EdenSpace
    long edenSpaceCommitted();

    long edenSpaceInit();

    long edenSpaceMax();

    long edenSpaceUsed();

    // Survivor
    long survivorCommitted();

    long survivorInit();

    long survivorMax();

    long survivorUsed();
}
