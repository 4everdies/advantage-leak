/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.animations.impl;

import cc.advantage.utils.render.animations.Animation;
import cc.advantage.utils.render.animations.Direction;

public class EaseBackIn
extends Animation {
    private final float easeAmount;

    public EaseBackIn(int ms, double endPoint, float easeAmount) {
        super(ms, endPoint);
        this.easeAmount = easeAmount;
    }

    public EaseBackIn(int ms, double endPoint, float easeAmount, Direction direction) {
        super(ms, endPoint, direction);
        this.easeAmount = easeAmount;
    }

    @Override
    protected boolean correctOutput() {
        return true;
    }

    @Override
    protected double getEquation(double x) {
        float shrink = this.easeAmount + 1.0f;
        return Math.max(0.0, 1.0 + (double)shrink * Math.pow(x - 1.0, 3.0) + (double)this.easeAmount * Math.pow(x - 1.0, 2.0));
    }
}

