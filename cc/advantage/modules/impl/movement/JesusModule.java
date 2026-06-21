/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.player.BlockCollideEvent;
import cc.advantage.api.events.impl.player.JumpEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PlayerUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(label="Jesus", category=ModuleCategory.MOVEMENT)
public final class JesusModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Vanilla);
    private final Property<Boolean> allowJump = new Property<Boolean>("Allow User Jump", true);
    @EventLink
    public final Listener<BlockCollideEvent> onBlockAABB = event -> {
        if (event.getBlock() instanceof BlockLiquid && !Util.mc.gameSettings.keyBindSneak.isKeyDown()) {
            int x = event.getX();
            int y = event.getY();
            int z = event.getZ();
            event.setCollisionBoundingBox(AxisAlignedBB.fromBounds(x, y, z, x + 1, y + 1, z + 1));
        }
    };
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        if (this.mode.getValue() == Mode.NCP && Util.mc.thePlayer.ticksExisted % 2 == 0 && PlayerUtils.onLiquid()) {
            event.setPosY(event.getPosY() - 0.015625);
        }
    };
    @EventLink
    public final Listener<JumpEvent> onJump = event -> {
        if (!this.allowJump.getValue().booleanValue() && PlayerUtils.onLiquid()) {
            event.setCancelled();
        }
    };

    private static enum Mode {
        Vanilla,
        NCP;

    }
}

