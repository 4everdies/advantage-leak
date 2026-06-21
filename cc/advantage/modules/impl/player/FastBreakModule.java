/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(label="Fast Break", category=ModuleCategory.PLAYER)
public final class FastBreakModule
extends Module {
    public final NumberProperty speed = new NumberProperty("Speed", 15.0, 1.0, 100.0, 1.0);
    public final NumberProperty delay = new NumberProperty("Delay", 0.0, 0.0, 4.0, 1.0);
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        this.setSuffix(((Double)this.speed.getValue()).intValue() + "%");
        if (!Util.mc.playerController.isInCreativeMode() && Util.mc.objectMouseOver != null && Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            float damage;
            float curBlockDamageMP;
            Util.mc.playerController.setBlockHitDelay(Math.min(Util.mc.playerController.getBlockHitDelay(), ((Double)this.delay.getValue()).intValue() + 1));
            if (Util.mc.playerController.getIsHittingBlock() && (curBlockDamageMP = Util.mc.playerController.getCurBlockDamageMP()) < (damage = 0.3f * (((Double)this.speed.getValue()).floatValue() / 100.0f))) {
                Util.mc.playerController.setCurBlockDamageMP(damage);
            }
        }
    };
}

