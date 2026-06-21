/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.properties.impl;

import cc.advantage.api.properties.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class MultiModeProperty<T extends Enum<T>>
extends Property<List<T>> {
    private final T[] values;

    @SafeVarargs
    public MultiModeProperty(String label, Supplier<Boolean> dependency, T ... values2) {
        super(label, Arrays.asList(values2), dependency);
        if (values2.length == 0) {
            throw new RuntimeException("Must have at least one default value.");
        }
        this.values = this.getEnumConstants();
    }

    @SafeVarargs
    public MultiModeProperty(String label, T ... values2) {
        this(label, () -> true, (Enum[])values2);
    }

    private T[] getEnumConstants() {
        return (Enum[])((Enum)((List)this.value).get(0)).getClass().getEnumConstants();
    }

    public T[] getValues() {
        return this.values;
    }

    public boolean isSelected(T variant) {
        return ((List)this.getValue()).contains(variant);
    }

    @Override
    public void setValue(int index) {
        ArrayList<T> values2 = new ArrayList<T>((Collection)this.value);
        T referencedVariant = this.values[index];
        if (values2.contains(referencedVariant)) {
            values2.remove(referencedVariant);
        } else {
            values2.add(referencedVariant);
        }
        this.setValue(values2);
    }
}

