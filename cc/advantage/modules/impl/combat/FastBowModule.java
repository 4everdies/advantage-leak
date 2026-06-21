/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.world.TickEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(label="Fast Bow", category=ModuleCategory.COMBAT)
public final class FastBowModule
extends Module {
    public static final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Vanilla);
    @EventLink
    public final Listener<TickEvent> tickEventListener = event -> {
        this.setSuffix(String.valueOf(mode.getValue()));
        if (Util.mc.thePlayer.getItemInUse() == null || Util.mc.thePlayer.inventory.getCurrentItem() == null) {
            return;
        }
        if ((Util.mc.thePlayer.getItemInUseDuration() >= 15 || mode.getValue() == Mode.Vanilla) && Util.mc.thePlayer.getItemInUse().getItem() instanceof ItemBow) {
            for (int i = 0; i < (mode.getValue() == Mode.Vanilla ? 20 : 8); ++i) {
                Util.mc.getNetHandler().addToSendQueue(new C03PacketPlayer(Util.mc.thePlayer.onGround));
            }
            Util.mc.playerController.onStoppedUsingItem(Util.mc.thePlayer);
        }
    };

    public static enum Mode {
        Vanilla,
        NCP;

    }
}

