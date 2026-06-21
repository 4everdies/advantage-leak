/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockPos;

@ModuleInfo(label="Auto Tool", category=ModuleCategory.PLAYER)
public class AutoToolModule
extends Module {
    private static final Property<Boolean> sneakOnly = new Property<Boolean>("Sneak Only", false);
    public static boolean shouldSwap = true;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = e -> {
        if (sneakOnly.getValue().booleanValue() && !Util.mc.thePlayer.isSneaking()) {
            return;
        }
        if (!Util.mc.gameSettings.keyBindAttack.isKeyDown() || Util.mc.objectMouseOver == null) {
            return;
        }
        BlockPos pos = Util.mc.objectMouseOver.getBlockPos();
        if (pos == null) {
            return;
        }
        int itemToUse = this.getBestToolSlot(pos);
        if (itemToUse == -1) {
            return;
        }
        if (shouldSwap) {
            Util.mc.thePlayer.inventory.currentItem = itemToUse;
        }
    };
    @EventLink
    public final Listener<AttackEvent> attackEventListener = e -> {
        float bestStr = 0.0f;
        int itemToUse = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = Util.mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null || !(itemStack.getItem() instanceof ItemSword)) continue;
            ItemSword item = (ItemSword)itemStack.getItem();
            if (!(item.attackDamage > bestStr)) continue;
            bestStr = item.attackDamage;
            itemToUse = i;
        }
        if (itemToUse != -1) {
            Util.mc.thePlayer.inventory.currentItem = itemToUse;
        }
    };

    private int getBestToolSlot(BlockPos pos) {
        Block block = Util.mc.theWorld.getBlockState(pos).getBlock();
        float bestStr = 1.0f;
        int itemTouse = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = Util.mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null || !(itemStack.getStrVsBlock(block) > bestStr)) continue;
            bestStr = itemStack.getStrVsBlock(block);
            itemTouse = i;
        }
        return itemTouse;
    }
}

