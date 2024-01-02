package com.tongtech.proxy.core.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SingleThreadSequentialExecutor extends Thread {

    private static final Log Logger = ProxyConfig.getServerLog();

    private final BlockingQueue<Runnable> WorkerQueue = new LinkedBlockingQueue<Runnable>();
    private volatile boolean Running = true;

    public SingleThreadSequentialExecutor() {
        this.setDaemon(true);
        this.start();
    }

    public void execute(Runnable command) {
        WorkerQueue.add(command);
    }

    public void shutdown() {
        try {
            this.Running = false;
            this.interrupt();
        } catch (Throwable t) {
        }
    }

    public void run() {
        while (Running) {
            Runnable command = null;
            try {
                command = WorkerQueue.poll(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
            if (command != null) {
                try {
                    command.run();
                } catch (Throwable t) {
                    Logger.warnLog(t, "SingleThreadQueuedExecutor()run() Runnable error: {}", t.getMessage());
                }
            }
        }
    }

    public void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }
}
