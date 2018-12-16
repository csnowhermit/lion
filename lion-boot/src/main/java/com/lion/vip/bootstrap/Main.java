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
