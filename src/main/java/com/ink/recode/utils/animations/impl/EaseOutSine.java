package com.ink.recode.utils.animations.impl;

import com.ink.recode.utils.animations.Animation;
import com.ink.recode.utils.animations.Direction;

public class EaseOutSine extends Animation {
    public EaseOutSine(long duration, double endValue) {
        super(duration, endValue);
    }

    @Override
    public void update() {
        long elapsed = timerUtil.getTime();
        double progress = Math.min((double) elapsed / duration, 1.0);

        if (direction == Direction.FORWARDS) {
            value = endValue * Math.sin(progress * Math.PI / 2);
        } else {
            value = endValue * (1 - Math.sin(progress * Math.PI / 2));
        }
    }
}