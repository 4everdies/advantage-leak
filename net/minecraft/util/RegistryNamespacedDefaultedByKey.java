/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.util.RegistryNamespaced;
import org.apache.commons.lang3.Validate;

public class RegistryNamespacedDefaultedByKey<K, V>
extends RegistryNamespaced<K, V> {
    private final K defaultValueKey;
    private V defaultValue;

    public RegistryNamespacedDefaultedByKey(K defaultValueKeyIn) {
        this.defaultValueKey = defaultValueKeyIn;
    }

    @Override
    public void register(int id, K key, V value) {
        if (this.defaultValueKey.equals(key)) {
            this.defaultValue = value;
        }
        super.register(id, key, value);
    }

    public void validateKey() {
        Validate.notNull(this.defaultValueKey);
    }

    @Override
    public V getObject(K name) {
        Object v = super.getObject(name);
        return v == null ? this.defaultValue : v;
    }

    @Override
    public V getObjectById(int id) {
        Object v = super.getObjectById(id);
        return v == null ? this.defaultValue : v;
    }
}

