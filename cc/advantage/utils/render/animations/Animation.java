/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.animations;

import cc.advantage.utils.client.Timer;
import cc.advantage.utils.render.animations.Direction;

public abstract class Animation {
    public Timer timerUtil = new Timer();
    protected int duration;
    protected double endPoint;
    protected Direction direction;
    private double startPoint;

    public Animation(int ms, double endPoint) {
        this(ms, endPoint, Direction.FORWARDS);
    }

    public Animation(int ms, double endPoint, Direction direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }

    public Animation(int ms, double startPoint, double endPoint, Direction direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
        this.startPoint = startPoint;
    }

    public Animation(int ms, double startPoint, double endPoint) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.startPoint = startPoint;
    }

    public boolean finished(Direction direction) {
        return this.isDone() && this.direction.equals((Object)direction);
    }

    public double getLinearOutput() {
        return 1.0 - (double)this.timerUtil.getTime() / (double)this.duration * this.endPoint;
    }

    public double getEndPoint() {
        return this.endPoint;
    }

    public void setEndPoint(double endPoint) {
        this.endPoint = endPoint;
    }

    public void reset() {
        this.timerUtil.reset();
    }

    public boolean isDone() {
        return this.timerUtil.hasTimeElapsed(this.duration);
    }

    public void changeDirection() {
        this.setDirection(this.direction.opposite());
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Animation setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            this.timerUtil.setTime(System.currentTimeMillis() - ((long)this.duration - Math.min((long)this.duration, this.timerUtil.getTime())));
        }
        return this;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected boolean correctOutput() {
        return false;
    }

    public Double getOutput() {
        double elapsedTime = this.timerUtil.getTime();
        double progress = elapsedTime / (double)this.duration;
        if (this.direction.forwards()) {
            if (this.isDone()) {
                return this.endPoint;
            }
            return this.interpolate(this.startPoint, this.endPoint, this.getEquation(progress));
        }
        if (this.isDone()) {
            return this.startPoint;
        }
        if (this.correctOutput()) {
            double revTime = Math.min((double)this.duration, Math.max(0.0, (double)this.duration - elapsedTime));
            double reverseProgress = revTime / (double)this.duration;
            return this.interpolate(this.startPoint, this.endPoint, this.getEquation(reverseProgress));
        }
        return this.interpolate(this.startPoint, this.endPoint, 1.0 - this.getEquation(progress));
    }

    private double interpolate(double start, double end, double factor) {
        return start + (end - start) * factor;
    }

    protected abstract double getEquation(double var1);
}

