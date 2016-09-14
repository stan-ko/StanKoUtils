package com.stanko.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Authors:
 * Stan Koshutsky <Stan.Koshutsky@gmail.com>
 */

public final class BackgroundThreadFactory implements ThreadFactory {

    private int priority;
    private boolean daemon;
    private final String namePrefix;
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public BackgroundThreadFactory() {
        this(android.os.Process.THREAD_PRIORITY_BACKGROUND, true);
    }

    public BackgroundThreadFactory(int priority) {
        this(priority, true);
    }

    public BackgroundThreadFactory(int priority, boolean daemon) {
        this.priority = priority;
        this.daemon = daemon;
        this.namePrefix = "jobpool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
        t.setDaemon(daemon);
        t.setPriority(priority);
        return t;
    }
}