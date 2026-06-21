/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="China Hat", category=ModuleCategory.VISUALS)
public final class ChinaHatModule
extends Module {
    private final ModeProperty<Quality> quality = new ModeProperty<Quality>("Quality", Quality.SMOOTH);
    private final Property<Boolean> showInFirstPerson = new Property<Boolean>("Show in First Person", false);
    private final Property<Boolean> rotate = new Property<Boolean>("Rotate", false);
    private final NumberProperty radius = new NumberProperty("Radius", 0.65, 0.3, 1.5, 0.05);
    private final NumberProperty height = new NumberProperty("Height", 0.5, 0.2, 1.5, 0.1);
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (Util.mc.gameSettings.thirdPersonView == 0 && !this.showInFirstPerson.getValue().booleanValue()) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);
        GlStateManager.disableCull();
        GL11.glDisable(2929);
        float partialTicks = Util.mc.timer.renderPartialTicks;
        double x = Util.mc.thePlayer.lastTickPosX + (Util.mc.thePlayer.posX - Util.mc.thePlayer.lastTickPosX) * (double)partialTicks - Util.mc.getRenderManager().viewerPosX;
        double y = Util.mc.thePlayer.lastTickPosY + (Util.mc.thePlayer.posY - Util.mc.thePlayer.lastTickPosY) * (double)partialTicks - Util.mc.getRenderManager().viewerPosY + (double)Util.mc.thePlayer.getEyeHeight() + (Double)this.height.getValue();
        double z = Util.mc.thePlayer.lastTickPosZ + (Util.mc.thePlayer.posZ - Util.mc.thePlayer.lastTickPosZ) * (double)partialTicks - Util.mc.getRenderManager().viewerPosZ;
        double adjustedY = Util.mc.thePlayer.isSneaking() ? y - 0.2 : y;
        Color color = this.getChinaHatColor();
        double rotation = this.rotate.getValue() != false ? (double)((Util.mc.thePlayer.prevRenderYawOffset + (Util.mc.thePlayer.renderYawOffset - Util.mc.thePlayer.prevRenderYawOffset) * partialTicks) / 60.0f + 20.0f) : 0.0;
        int segments = ((Quality)((Object)((Object)this.quality.getValue()))).segments;
        double rad = (Double)this.radius.getValue();
        GL11.glBegin(5);
        for (int i = 0; i <= segments; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)segments;
            double outerX = x + rad * Math.cos(angle + rotation);
            double outerZ = z + rad * Math.sin(angle + rotation);
            GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.3f);
            GL11.glVertex3d(outerX, adjustedY - 0.25, outerZ);
            GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.8f);
            GL11.glVertex3d(x, adjustedY, z);
        }
        GL11.glEnd();
        GL11.glShadeModel(7424);
        GL11.glEnable(2929);
        GlStateManager.enableCull();
        GL11.glDisable(2848);
        GL11.glDisable(2832);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    };

    private Color getChinaHatColor() {
        return ColorProcess.getColor();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private static enum Quality {
        UMBRELLA("Umbrella", 16),
        VERY_LOW("Very Low", 32),
        LOW("Low", 64),
        NORMAL("Normal", 128),
        HIGH("High", 256),
        VERY_HIGH("Very High", 512),
        SMOOTH("Smooth", 1024);

        public final String name;
        public final int segments;

        private Quality(String name, int segments) {
            this.name = name;
            this.segments = segments;
        }

        public String toString() {
            return this.name;
        }
    }
}

