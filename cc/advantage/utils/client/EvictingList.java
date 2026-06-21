/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.utils.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import lombok.Generated;

public class EvictingList<T>
extends LinkedList<T>
implements Serializable {
    private int maxSize;

    public EvictingList(int maxSize) {
        this.maxSize = maxSize;
    }

    public EvictingList(Collection<? extends T> c, int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T t) {
        if (this.size() >= this.getMaxSize()) {
            this.removeFirst();
        }
        return super.add(t);
    }

    public boolean isFull() {
        return this.size() >= this.getMaxSize();
    }

    public EvictingList<T> reverse() {
        EvictingList list = new EvictingList(this.maxSize);
        for (int i = this.size() - 1; i >= 0; --i) {
            list.add((T)this.get(i));
        }
        return list;
    }

    @Generated
    public int getMaxSize() {
        return this.maxSize;
    }

    @Generated
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}

