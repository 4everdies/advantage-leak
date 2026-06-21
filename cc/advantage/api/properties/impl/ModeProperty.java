/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.properties.impl;

import cc.advantage.api.properties.Property;
import java.util.function.Supplier;

public class ModeProperty<T extends Enum<T>>
extends Property<T> {
    private final T[] values = this.getEnumConstants();

    public ModeProperty(String label, T value, Supplier<Boolean> dependency) {
        super(label, value, dependency);
    }

    public ModeProperty(String label, T value) {
        this(label, value, () -> true);
    }

    private T[] getEnumConstants() {
        return (Enum[])((Enum)this.value).getClass().getEnumConstants();
    }

    public T[] getValues() {
        return this.values;
    }

    @Override
    public void setValue(int index) {
        this.setValue(this.values[index]);
    }
}

