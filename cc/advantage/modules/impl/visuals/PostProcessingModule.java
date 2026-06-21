/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.shaders.Bloom;
import cc.advantage.utils.render.shaders.Blur;
import cc.advantage.utils.render.shaders.Shadow;
import net.minecraft.client.shader.Framebuffer;

@ModuleInfo(label="Post Processing", category=ModuleCategory.VISUALS)
public final class PostProcessingModule
extends Module {
    public final Property<Boolean> blur = new Property<Boolean>("Blur", true);
    public final NumberProperty blurRadius = new NumberProperty("Blur Radius", 10.0, this.blur::getValue, 1.0, 128.0, 1.0);
    public final NumberProperty blurCompression = new NumberProperty("Blur Compression", 2.0, this.blur::getValue, 0.1, 16.0, 0.1);
    public final NumberProperty blurStrength = new NumberProperty("Blur Strength", 1.0, this.blur::getValue, 0.0, 5.0, 0.05);
    public final Property<Boolean> shadow = new Property<Boolean>("Shadow", true);
    public final NumberProperty shadowRadius = new NumberProperty("Shadow Radius", 50.0, this.shadow::getValue, 0.0, 128.0, 1.0);
    public final NumberProperty shadowOffset = new NumberProperty("Shadow Offset", 1.0, this.shadow::getValue, 0.0, 16.0, 1.0);
    public final NumberProperty shadowStrength = new NumberProperty("Shadow Strength", 1.0, this.shadow::getValue, 0.0, 5.0, 0.1);
    public final Property<Boolean> bloom = new Property<Boolean>("Bloom", true);
    public final NumberProperty bloomIterations = new NumberProperty("Bloom Iterations", 4.0, () -> this.bloom.getValue(), 1.0, 8.0, 1.0);
    public final NumberProperty bloomOffset = new NumberProperty("Bloom Offset", 1.0, () -> this.bloom.getValue(), 0.0, 8.0, 1.0);
    public final NumberProperty bloomStrength = new NumberProperty("Bloom Strength", 1.2, () -> this.bloom.getValue(), 0.0, 5.0, 0.05);
    public static Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    public void renderShaders() {
        if (!this.isEnabled()) {
            return;
        }
        if (this.blur.getValue().booleanValue()) {
            Blur.startBlur();
            Advantage.INSTANCE.getEventBus().post(new ShaderEvent(ShaderEvent.ShaderType.BLUR));
            Blur.endBlur(((Double)this.blurRadius.getValue()).floatValue(), ((Double)this.blurCompression.getValue()).floatValue(), ((Double)this.blurStrength.getValue()).floatValue());
            RenderUtils.resetColor();
        }
        if (this.shadow.getValue().booleanValue()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer, true);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(true);
            RenderUtils.resetColor();
            Advantage.INSTANCE.getEventBus().post(new ShaderEvent(ShaderEvent.ShaderType.SHADOW));
            stencilFramebuffer.unbindFramebuffer();
            RenderUtils.resetColor();
            if (PostProcessingModule.stencilFramebuffer.framebufferTexture > 0) {
                Shadow.renderShadow(PostProcessingModule.stencilFramebuffer.framebufferTexture, ((Double)this.shadowRadius.getValue()).intValue(), ((Double)this.shadowOffset.getValue()).intValue(), ((Double)this.shadowStrength.getValue()).floatValue());
            }
        }
        if (this.bloom.getValue().booleanValue()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);
            Advantage.INSTANCE.getEventBus().post(new ShaderEvent(ShaderEvent.ShaderType.BLOOM));
            stencilFramebuffer.unbindFramebuffer();
            Bloom.renderBloom(PostProcessingModule.stencilFramebuffer.framebufferTexture, ((Double)this.bloomIterations.getValue()).intValue(), ((Double)this.bloomOffset.getValue()).intValue(), ((Double)this.bloomStrength.getValue()).floatValue());
        }
    }
}

