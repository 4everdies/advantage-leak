/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MapPopulator {
    public static <K, V> Map<K, V> createMap(Iterable<K> keys2, Iterable<V> values2) {
        return MapPopulator.populateMap(keys2, values2, Maps.newLinkedHashMap());
    }

    public static <K, V> Map<K, V> populateMap(Iterable<K> keys2, Iterable<V> values2, Map<K, V> map) {
        Iterator<V> iterator2 = values2.iterator();
        for (K k : keys2) {
            map.put(k, iterator2.next());
        }
        if (iterator2.hasNext()) {
            throw new NoSuchElementException();
        }
        return map;
    }
}

