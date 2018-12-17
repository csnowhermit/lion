package com.lion.vip.bootstrap;

import com.lion.vip.tools.log.Logs;

/**
 * 程序的主入口
 */
public class Main {
    public static void main(String[] args) {
        Logs.init();
        Logs.Console.info("Launch Lion Server...");
        ServerLauncher serverLauncher = new ServerLauncher();
        serverLauncher.init();
        serverLauncher.start();
        addHook(serverLauncher);
    }

    /**
     * 注意：
     * 1.不要在ShutdownHook Thread里调用System.exit()方法，否则会造成死循环；
     * 2.如果有非守护线程，只有所有的非守护线程都结束了才会执行hook；
     * 3.Thread默认为非守护线程；
     * 4.注意线程抛出的异常，如果没有被捕获到，都会跑到Thread.dispatchUncauhtException
     *
     * @param serverLauncher
     */
    private static void addHook(ServerLauncher serverLauncher) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serverLauncher.stop();
            } catch (Exception e) {
                Logs.Console.error("Lion Server stop ex ", e);
            }

            Logs.Console.info("JVM exit, all service stopped.");
        }, "lion-shutdown-hook-thread"));
    }
}
