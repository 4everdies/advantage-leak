/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.modules;

import lombok.Generated;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    CLIENT("Client"),
    VISUALS("Visuals"),
    CONFIGS("Configs");

    private final String name;

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    private ModuleCategory(String name) {
        this.name = name;
    }
}

