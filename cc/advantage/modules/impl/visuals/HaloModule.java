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

@ModuleInfo(label="Halo", category=ModuleCategory.VISUALS)
public class HaloModule
extends Module {
    private final ModeProperty<Quality> quality = new ModeProperty<Quality>("Quality", Quality.SMOOTH);
    private final Property<Boolean> showInFirstPerson = new Property<Boolean>("Show in First Person", false);
    private final Property<Boolean> rotate = new Property<Boolean>("Rotate", true);
    private final NumberProperty radius = new NumberProperty("Radius", 0.4, 0.2, 1.0, 0.05);
    private final NumberProperty height = new NumberProperty("Height", 0.5, 0.0, 1.5, 0.1);
    private final NumberProperty thickness = new NumberProperty("Thickness", 0.08, 0.02, 0.2, 0.01);
    private final NumberProperty tilt = new NumberProperty("Tilt", 15.0, 0.0, 45.0, 5.0);
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        double angle;
        int i;
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (Util.mc.gameSettings.thirdPersonView == 0 && !this.showInFirstPerson.getValue().booleanValue()) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
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
        GL11.glTranslated(x, adjustedY, z);
        double rotation = this.rotate.getValue() != false ? (double)System.currentTimeMillis() / 50.0 : 0.0;
        GL11.glRotated(rotation, 0.0, 1.0, 0.0);
        double tiltAngle = (Double)this.tilt.getValue();
        GL11.glRotated(tiltAngle, 1.0, 0.0, 0.0);
        Color color = ColorProcess.getColor();
        int segments = ((Quality)((Object)((Object)this.quality.getValue()))).segments;
        double rad = (Double)this.radius.getValue();
        double thick = (Double)this.thickness.getValue();
        GL11.glBegin(5);
        for (i = 0; i <= segments; ++i) {
            angle = Math.PI * 2 * (double)i / (double)segments;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.8f);
            GL11.glVertex3d(rad * cos, 0.0, rad * sin);
            GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.6f);
            GL11.glVertex3d((rad - thick) * cos, 0.0, (rad - thick) * sin);
        }
        GL11.glEnd();
        GL11.glBegin(6);
        GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.4f);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.1f);
        for (i = 0; i <= segments; ++i) {
            angle = Math.PI * 2 * (double)i / (double)segments;
            GL11.glVertex3d((rad - thick) * Math.cos(angle), 0.0, (rad - thick) * Math.sin(angle));
        }
        GL11.glEnd();
        GL11.glShadeModel(7424);
        GL11.glEnable(2929);
        GlStateManager.enableCull();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private static enum Quality {
        LOW("Low", 32),
        NORMAL("Normal", 64),
        HIGH("High", 128),
        SMOOTH("Smooth", 256);

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

