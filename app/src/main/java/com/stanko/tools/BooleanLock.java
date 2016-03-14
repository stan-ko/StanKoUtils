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

    private boolean value;

    public boolean setRunning() {
        if (this.value)
            return false;
        this.value = true;
        return true;
    }

    public boolean isRunning() {
        return value;
    }

    public void setFinished() {
        this.value = false;
    }

    public boolean isFinished() {
        return !value;
    }

//    public BooleanLock(boolean b) {
//        value = b;
//    }
//
//    public boolean isTrue() {
//        return value;
//    }
//    public boolean isValue() {
//        return value;
//    }
//
//    public void setTrue() {
//        this.value = true;
//    }
//    public void setFalse() {
//        this.value = false;
//    }
//    public void setValue(boolean value) {
//        this.value = value;
//    }

}
