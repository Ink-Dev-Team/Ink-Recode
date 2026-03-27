package com.ink.recode.utils.animations;

public class TimerUtil {
    private long time;

    public TimerUtil() {
        this.time = System.currentTimeMillis();
    }

    public void reset() {
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - this.time;
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - this.time >= time;
    }

    public void setTime(int time) {
        this.time = System.currentTimeMillis() - time;
    }
}