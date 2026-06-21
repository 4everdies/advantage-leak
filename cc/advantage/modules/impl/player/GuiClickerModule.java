/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Mouse;

@ModuleInfo(label="GUI Clicker", category=ModuleCategory.PLAYER)
public final class GuiClickerModule
extends Module {
    public int mouseDownTicks;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        if (Util.mc.currentScreen instanceof GuiContainer) {
            GuiContainer container = (GuiContainer)Util.mc.currentScreen;
            int i = Mouse.getEventX() * container.width / Util.mc.displayWidth;
            int j = container.height - Mouse.getEventY() * container.height / Util.mc.displayHeight - 1;
            try {
                if (Mouse.isButtonDown(0)) {
                    ++this.mouseDownTicks;
                    if (this.mouseDownTicks > 2 && Math.random() > 0.1) {
                        container.mouseClicked(i, j, 0);
                    }
                } else if (Mouse.isButtonDown(1)) {
                    ++this.mouseDownTicks;
                    if (this.mouseDownTicks > 2 && Math.random() > 0.1) {
                        container.mouseClicked(i, j, 1);
                    }
                } else {
                    this.mouseDownTicks = 0;
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    };
}

