/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.RotationUtils;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;

@ModuleInfo(label="No Fireball", category=ModuleCategory.PLAYER)
public final class NoFireballModule
extends Module {
    private final Property<Boolean> rotate = new Property<Boolean>("Rotate", true);
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        for (Entity entity : Util.mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityFireball) || !(entity.getDistanceToEntity(Util.mc.thePlayer) < 5.0f)) continue;
            if (this.rotate.getValue().booleanValue()) {
                RotationProcess.setRotations(RotationUtils.calculate(entity), 10.0, MovementFix.NORMAL);
            }
            Util.mc.clickMouse();
            break;
        }
    };
}

