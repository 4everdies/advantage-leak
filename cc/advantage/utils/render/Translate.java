/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render;

import cc.advantage.utils.client.MathUtils;
import net.minecraft.client.Minecraft;

public final class Translate {
    private double x;
    private double y;

    public Translate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void animate(double newX, double newY) {
        this.x = Translate.progressiveAnimation(this.x, newX, 1.0);
        this.y = Translate.progressiveAnimation(this.y, newY, 0.5);
    }

    public double getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public static double progressiveAnimation(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);
        int fps = Minecraft.getDebugFPS();
        if (dif > 0.0) {
            double animationSpeed = MathUtils.roundToDecimalPlace(Math.min(10.0, Math.max(0.05, 144.0 / (double)fps * (dif / 10.0) * speed)), 0.05);
            if (dif != 0.0 && dif < animationSpeed) {
                animationSpeed = dif;
            }
            if (now < desired) {
                return now + animationSpeed;
            }
            if (now > desired) {
                return now - animationSpeed;
            }
        }
        return now;
    }
}

