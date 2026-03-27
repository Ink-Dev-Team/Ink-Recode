package com.ink.recode.utils.animations.impl;

import com.ink.recode.utils.animations.Animation;
import com.ink.recode.utils.animations.Direction;

public class EaseInOutQuad extends Animation {
    public EaseInOutQuad(long duration, double endValue) {
        super(duration, endValue);
    }

    @Override
    public void update() {
        long elapsed = timerUtil.getTime();
        double progress = Math.min((double) elapsed / duration, 1.0);

        if (direction == Direction.FORWARDS) {
            if (progress < 0.5) {
                value = endValue * 2 * progress * progress;
            } else {
                progress -= 1;
                value = endValue * (2 * progress * progress + 2 * progress + 1);
            }
        } else {
            if (progress < 0.5) {
                value = endValue - endValue * 2 * progress * progress;
            } else {
                progress -= 1;
                value = endValue - endValue * (2 * progress * progress + 2 * progress + 1);
            }
        }
    }
}