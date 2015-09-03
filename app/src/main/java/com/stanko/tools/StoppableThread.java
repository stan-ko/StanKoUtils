package com.stanko.tools;

public class StoppableThread extends Thread {
    public boolean isStopped;
    public boolean isPaused;

    public StoppableThread(Runnable runnable) {
        super(runnable);
    }

}
