/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.animations.impl;

import cc.advantage.utils.render.animations.Animation;
import cc.advantage.utils.render.animations.Direction;

public class DecelerateAnimation
extends Animation {
    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public DecelerateAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    public DecelerateAnimation(int ms, double startPoint, double endPoint, Direction direction) {
        super(ms, startPoint, endPoint, direction);
    }

    public DecelerateAnimation(int ms, double startPoint, double endPoint) {
        super(ms, startPoint, endPoint);
    }

    @Override
    protected double getEquation(double x) {
        return 1.0 - (x - 1.0) * (x - 1.0);
    }
}

