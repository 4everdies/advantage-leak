/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.utils.render.animations.impl;

import cc.advantage.utils.render.animations.Animation;
import cc.advantage.utils.render.animations.Direction;
import cc.advantage.utils.render.animations.impl.SmoothStepAnimation;
import lombok.Generated;

public class ContinualAnimation {
    private float output;
    private float endpoint;
    private Animation animation = new SmoothStepAnimation(0, 0.0, Direction.BACKWARDS);

    public void animate(float destination, int ms) {
        this.output = this.endpoint - this.animation.getOutput().floatValue();
        this.endpoint = destination;
        if (this.output != this.endpoint - destination) {
            this.animation = new SmoothStepAnimation(ms, (double)(this.endpoint - this.output), Direction.BACKWARDS);
        }
    }

    public boolean isDone() {
        return this.output == this.endpoint || this.animation.isDone();
    }

    public float getOutput() {
        this.output = this.endpoint - this.animation.getOutput().floatValue();
        return this.output;
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }
}

