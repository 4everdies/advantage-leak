/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.utils.misc;

import lombok.Generated;

public enum MovementFix {
    OFF("Off"),
    NORMAL("Normal"),
    TRADITIONAL("Traditional"),
    BACKWARDS_SPRINT("Backwards Sprint");

    final String name;

    public String toString() {
        return this.name;
    }

    @Generated
    private MovementFix(String name) {
        this.name = name;
    }
}

