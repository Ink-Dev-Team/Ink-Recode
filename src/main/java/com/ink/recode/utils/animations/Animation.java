package com.ink.recode.utils.animations;

public abstract class Animation {
    protected final long duration;
    protected final double startValue;
    protected final double endValue;
    protected double value;
    protected Direction direction;
    public TimerUtil timerUtil = new TimerUtil();

    public Animation(long duration, double endValue) {
        this.duration = duration;
        this.startValue = 0;
        this.endValue = endValue;
        this.value = startValue;
        this.direction = Direction.FORWARDS;
        this.timerUtil.reset();
    }

    public abstract void update();

    public double getOutput() {
        update();
        return value;
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            timerUtil.reset();
        }
    }

    public void reset() {
        timerUtil.reset();
    }
}