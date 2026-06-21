/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

@ModuleInfo(label="Legit Scaffold", category=ModuleCategory.PLAYER)
public final class LegitScaffoldModule
extends Module {
    private final NumberProperty delay = new NumberProperty("Delay", 50.0, 0.0, 200.0, 10.0);
    private final Property<Boolean> blockCheck = new Property<Boolean>("Blocks Only", true);
    private final Property<Boolean> directionCheck = new Property<Boolean>("Directional Check", true);
    private boolean wasOverBlock = false;
    private long lastSneakTime = 0L;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = e -> {
        if (e.isPre()) {
            if (!this.blockCheck.getValue().booleanValue() || Util.mc.thePlayer.getHeldItem() != null && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock && !this.directionCheck.getValue().booleanValue() || Util.mc.thePlayer.moveForward < 0.0f) {
                if (Util.mc.theWorld.getBlockState(new BlockPos(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY - 1.0, Util.mc.thePlayer.posZ)).getBlock() instanceof BlockAir && Util.mc.thePlayer.onGround) {
                    Util.mc.gameSettings.keyBindSneak.setPressed(true);
                    this.wasOverBlock = true;
                } else if (Util.mc.thePlayer.onGround) {
                    long delayTime;
                    long randomizedDelay;
                    long currentTime;
                    if (this.wasOverBlock) {
                        this.lastSneakTime = System.currentTimeMillis();
                    }
                    if ((currentTime = System.currentTimeMillis()) - this.lastSneakTime >= (randomizedDelay = (long)((double)(delayTime = ((Double)this.delay.getValue()).longValue()) * (Math.random() * 0.1 + 0.95)))) {
                        Util.mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindSneak.getKeyCode()));
                    }
                    this.wasOverBlock = false;
                }
            } else {
                Util.mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindSneak.getKeyCode()));
            }
        }
    };

    @Override
    public void onDisable() {
        Util.mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindSneak.getKeyCode()));
    }
}

