/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.utils.client;

import lombok.Generated;

public enum BuildType {
    RELEASE("Release"),
    ALPHA("Alpha"),
    BETA("Beta"),
    DEV("Development");

    private final String name;

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    private BuildType(String name) {
        this.name = name;
    }
}

