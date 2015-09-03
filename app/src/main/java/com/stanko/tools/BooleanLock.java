package com.stanko.tools;

/**
 * Created by CTAC on 20.03.2015.
 */
public class BooleanLock {

    private boolean value;

    public BooleanLock() {}

//    public BooleanLock(boolean b) {
//        value = b;
//    }

    public boolean isTrue() {
        return value;
    }
    public boolean isValue() {
        return value;
    }

    public void setTrue() {
        this.value = true;
    }
    public boolean setRunning() {
        if (this.value)
            return false;
        this.value = true;
        return true;
    }
    public boolean isRunning() {
        return value;
    }

    public void setFalse() {
        this.value = false;
    }
    public void setFinished() {
        this.value = false;
    }
    public boolean isFinished() {
        return !value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

}
