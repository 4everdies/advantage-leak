/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.utils.client;

import lombok.Generated;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public class EnumFacingOffset {
    public EnumFacing enumFacing;
    private final Vec3 offset;

    public EnumFacingOffset(EnumFacing enumFacing, Vec3 offset) {
        this.enumFacing = enumFacing;
        this.offset = offset;
    }

    @Generated
    public EnumFacing getEnumFacing() {
        return this.enumFacing;
    }

    @Generated
    public Vec3 getOffset() {
        return this.offset;
    }
}

