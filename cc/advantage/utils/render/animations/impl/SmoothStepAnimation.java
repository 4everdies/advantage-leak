/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.animations.impl;

import cc.advantage.utils.render.animations.Animation;
import cc.advantage.utils.render.animations.Direction;

public class SmoothStepAnimation
extends Animation {
    public SmoothStepAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public SmoothStepAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    @Override
    protected double getEquation(double x) {
        return -2.0 * Math.pow(x, 3.0) + 3.0 * Math.pow(x, 2.0);
    }
}

