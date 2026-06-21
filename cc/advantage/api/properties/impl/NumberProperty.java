/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.properties.impl;

import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.Representation;
import java.util.function.Supplier;

public class NumberProperty
extends Property<Double> {
    private final double min;
    private final double max;
    private final double increment;
    private final Representation representation;

    public NumberProperty(String label, double value, Supplier<Boolean> dependency, double min, double max, double increment, Representation representation) {
        super(label, value, dependency);
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.representation = representation;
    }

    public NumberProperty(String label, double value, Supplier<Boolean> dependency, double min, double max, double increment) {
        this(label, value, dependency, min, max, increment, Representation.DOUBLE);
    }

    public NumberProperty(String label, double value, double min, double max, double increment, Representation representation) {
        this(label, value, () -> true, min, max, increment, representation);
    }

    public NumberProperty(String label, double value, double min, double max, double increment) {
        this(label, value, () -> true, min, max, increment, Representation.DOUBLE);
    }

    public Representation getRepresentation() {
        return this.representation;
    }

    @Override
    public void setValue(Double value) {
        if (this.value != null && ((Double)this.value).doubleValue() != value.doubleValue()) {
            if (value < this.min) {
                value = this.min;
            } else if (value > this.max) {
                value = this.max;
            }
        }
        super.setValue(value);
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public double getIncrement() {
        return this.increment;
    }

    public Number getRandomBetween() {
        long max;
        long min = (long)this.getMin();
        if (min == (max = (long)this.getMax())) {
            return min;
        }
        if (min > max) {
            long d = min;
            min = max;
            max = d;
        }
        final long random = (long)((double)min + (double)(max - min) * Math.random() * Math.random());
        return new Number(this){
            final /* synthetic */ NumberProperty this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public int intValue() {
                return Math.round(random);
            }

            @Override
            public long longValue() {
                return random;
            }

            @Override
            public float floatValue() {
                return random;
            }

            @Override
            public double doubleValue() {
                return random;
            }
        };
    }
}

