package com.lion.vip.bootstrap.job;

import com.lion.vip.tools.log.Logs;

import java.util.function.Supplier;

public abstract class BootJob {
    protected BootJob next;

    protected abstract void start();

    protected abstract void stop();

    public BootJob getNext() {
        return next;
    }

    public BootJob setNext(BootJob next) {
        this.next = next;
        return this;
    }

    public String getNextName() {
        return next.getName();
    }

    protected String getName() {
        return this.getClass().getName();
    }

    public void startNext() {
        if (next != null) {
            Logs.Console.info("start bootstrap job [{}]", getNextName());
            next.start();
        }
    }

    public void stopNext() {
        if (next != null) {
            Logs.Console.info("stopped bootstrap job [{}]", getNextName());
        }
    }

    public BootJob setNext(Supplier<BootJob> next, boolean enabled) {
        if (enabled) {
            return setNext(next.get());
        }
        return this;
    }

}
