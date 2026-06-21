/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.item.ItemBlock;

@ModuleInfo(label="Safe Walk", category=ModuleCategory.MOVEMENT)
public final class SafeWalkModule
extends Module {
    private final Property<Boolean> blocksOnly = new Property<Boolean>("Blocks Only", false);
    private final Property<Boolean> backwardsOnly = new Property<Boolean>("Backwards Only", false);
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        Util.mc.thePlayer.safeWalk = Util.mc.thePlayer.onGround && (!Util.mc.gameSettings.keyBindForward.isKeyDown() || this.backwardsOnly.getValue() == false) && (Util.mc.thePlayer.inventory.getCurrentItem() != null && Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock || this.blocksOnly.getValue() == false);
    };
}

