/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.client;

import cc.advantage.utils.Util;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils
extends Util {
    public static final float PI = (float)Math.PI;
    public static final float TO_RADIANS = (float)Math.PI / 180;
    public static final float TO_DEGREES = 57.295776f;

    public static double wrappedDifference(double number1, double number2) {
        return Math.min(Math.abs(number1 - number2), Math.min(Math.abs(number1 - 360.0) - Math.abs(number2 - 0.0), Math.abs(number2 - 360.0) - Math.abs(number1 - 0.0)));
    }

    public static double roundToDecimalPlace(double value, double inc) {
        double halfOfInc = inc / 2.0;
        double floored = StrictMath.floor(value / inc) * inc;
        if (value >= floored + halfOfInc) {
            return new BigDecimal(StrictMath.ceil(value / inc) * inc, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
        }
        return new BigDecimal(floored, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
    }

    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        }
        if (min > max) {
            double d = min;
            min = max;
            max = d;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double PI2 = Math.PI;
        double output = 1.0 / Math.sqrt(2.0 * PI2 * (double)(sigma * sigma));
        return (float)(output * Math.exp((double)(-(x * x)) / (2.0 * (double)(sigma * sigma))));
    }

    public static double lerp(double pct, double start, double end) {
        return start + pct * (end - start);
    }

    public static float lerp(float min, float max, float delta) {
        return min + (max - min) * delta;
    }
}

