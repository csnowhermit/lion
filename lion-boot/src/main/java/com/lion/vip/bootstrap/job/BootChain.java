package com.lion.vip.bootstrap.job;

import com.lion.vip.api.event.ServerShutdownEvent;
import com.lion.vip.api.event.ServerStartupEvent;
import com.lion.vip.api.spi.core.ServerEventListenerFactory;
import com.lion.vip.tools.event.EventBus;
import com.lion.vip.tools.log.Logs;

import java.util.function.Supplier;

/**
 * 系统引导链
 */
public final class BootChain {
    private final BootJob boot = new BootJob() {
        {
            ServerEventListenerFactory.create();    //初始化服务监听
        }

        @Override
        protected void start() {
            Logs.Console.info("bootstrap chain starting...");
            startNext();
        }

        @Override
        protected void stop() {
            stopNext();
            Logs.Console.info("bootstrap chain stopped");
            Logs.Console.info("===================================================================");
            Logs.Console.info("====================LION SERVER STOPPED SUCCESS===================");
            Logs.Console.info("===================================================================");
        }
    };

    private BootJob last = boot;

    public void start() {
        boot.start();
    }

    public void stop() {
        boot.stop();
    }

    public static BootChain chain() {
        return new BootChain();
    }

    public BootChain boot() {
        return this;
    }

    public void end() {
        setNext(new BootJob() {
            @Override
            protected void start() {
                EventBus.post(new ServerStartupEvent());
                Logs.Console.info("bootstrap chain started.");
                Logs.Console.info("===================================================================");
                Logs.Console.info("====================LION SERVER START SUCCESS=====================");
                Logs.Console.info("===================================================================");
            }

            @Override
            protected void stop() {
                Logs.Console.info("bootstrap chain stopping...");
                EventBus.post(new ServerShutdownEvent());
            }

            @Override
            protected String getName() {
                return "LastBoot";
            }
        });
    }

    public BootChain setNext(BootJob bootJob) {
        this.last = last.setNext(bootJob);
        return this;
    }

    public BootChain setNext(Supplier<BootJob> next, boolean enabled) {
        if (enabled) {
            return setNext(next.get());
        }
        return this;
    }

}
