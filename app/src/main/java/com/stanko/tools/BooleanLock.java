package com.stanko.tools;

/**
 * Created by Stan on 20.03.2015.
 *
 * Intended to some Thread based or async tasks to be synchronized on this object.
 * Some task tries to mark it has been started using setRunning(). If method returns true
 * its marked as running otherwise means some other thread started the same job already.
 * When the job is done the task or thread should mark it using setFinished() method so other
 * threads could restart or repeat the same job.
 */

public class BooleanLock {

    private boolean isLocked;

    public synchronized boolean setRunning() {
        if (isLocked) // already locked/running
            return false;
        this.isLocked = true;
        return true;
    }

    public synchronized boolean isRunning() {
        return isLocked;
    }

    public synchronized void setFinished() {
        this.isLocked = false;
    }

    public synchronized boolean isFinished() {
        return !isLocked;
    }

}
