/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.misc;

import java.util.ArrayList;
import java.util.List;

public class Manager<T> {
    protected List<T> elements;

    public Manager(List<T> elements) {
        this.elements = elements;
    }

    public Manager() {
        this.elements = new ArrayList<T>();
    }

    public List<T> getElements() {
        return this.elements;
    }
}

