/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.render;

import cc.advantage.api.events.Event;
import lombok.Generated;

public class ShaderEvent
implements Event {
    private ShaderType shaderType;

    public ShaderEvent(ShaderType shaderType) {
        this.shaderType = shaderType;
    }

    @Generated
    public ShaderType getShaderType() {
        return this.shaderType;
    }

    @Generated
    public void setShaderType(ShaderType shaderType) {
        this.shaderType = shaderType;
    }

    public static enum ShaderType {
        BLUR,
        SHADOW,
        BLOOM;

    }
}

