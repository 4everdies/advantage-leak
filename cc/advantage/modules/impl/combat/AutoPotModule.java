/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;

@ModuleInfo(label="Auto Pot", category=ModuleCategory.COMBAT)
public final class AutoPotModule
extends Module {
    private final Property<Boolean> smartPot = new Property<Boolean>("Smart Pot", false);
    private final NumberProperty health = new NumberProperty("Health", 6.0, 1.0, 10.0, 0.5);
    private final NumberProperty delay = new NumberProperty("Delay", 400.0, 0.0, 2000.0, 25.0);
    private final ModeProperty<PotMode> potMode = new ModeProperty<PotMode>("Pot Mode", PotMode.JumpOnly);
    private final Property<Boolean> speed = new Property<Boolean>("Speed", true);
    private final Property<Boolean> jumpBoost = new Property<Boolean>("Jump Boost", false);
    private final Property<Boolean> healing = new Property<Boolean>("Healing", true);
    private final Property<Boolean> regeneration = new Property<Boolean>("Regeneration", true);
    private final Property<Boolean> fireResistance = new Property<Boolean>("Fire Resistance", true);
    private final Property<Boolean> strength = new Property<Boolean>("Strength", true);
    private static final PotionType[] VALID_POTIONS = new PotionType[]{PotionType.HEALING, PotionType.REGEN, PotionType.SPEED, PotionType.STRENGTH, PotionType.FIRE_RESISTANCE, PotionType.JUMP_BOOST};
    private final Timer interactionTimer = new Timer();
    private int prevSlot = -1;
    private int potSlot = -1;
    public static boolean potting;
    private boolean thrown;
    private boolean readyToThrow;
    private int jumpTicks = -1;
    private boolean jump;
    public static int haltTicks;
    @EventLink
    public final Listener<MotionEvent> onMotion = event -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (event.isPre()) {
            if (this.readyToThrow && potting) {
                event.setYaw(Util.mc.thePlayer.rotationYaw);
                event.setPitch(this.jump && this.jumpTicks >= 0 ? -90.0f : 90.0f);
            }
            return;
        }
        if (potting && !this.thrown && this.potSlot != -1 && this.readyToThrow) {
            ItemStack heldItem = Util.mc.thePlayer.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemPotion) {
                Util.mc.playerController.sendUseItem(Util.mc.thePlayer, Util.mc.theWorld, heldItem);
            }
            this.thrown = true;
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        boolean forceJump;
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        boolean bl = forceJump = this.potMode.getValue() == PotMode.JumpOnly;
        if (Util.mc.currentScreen instanceof GuiInventory) {
            haltTicks = 10;
            this.interactionTimer.reset();
            return;
        }
        if (this.jump && this.jumpTicks >= 0) {
            Util.mc.thePlayer.motionX = 0.0;
            Util.mc.thePlayer.motionZ = 0.0;
        }
        if (potting && haltTicks < 0) {
            this.finishPotting();
        }
        if (haltTicks > 6) {
            --haltTicks;
            return;
        }
        if (this.jump) {
            --this.jumpTicks;
            if (Util.mc.thePlayer.onGround) {
                this.jump = false;
                this.jumpTicks = -1;
            }
        }
        if (!potting) {
            this.handlePotions(forceJump);
        }
        --haltTicks;
    };

    private void handlePotions(boolean forceJump) {
        boolean scaffoldOn;
        boolean near = this.isNearby(3.7f);
        ScaffoldModule scaffold = Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class);
        boolean bl = scaffoldOn = scaffold != null && scaffold.isEnabled();
        if (!(!scaffoldOn && Util.mc.thePlayer.onGround || (Util.mc.thePlayer.hurtResistantTime <= 0 || !near) && this.smartPot.getValue().booleanValue() && near)) {
            return;
        }
        if (!this.interactionTimer.hasTimeElapsed((Double)this.delay.getValue())) {
            return;
        }
        float hpTarget = ((Double)this.health.getValue()).floatValue() * 2.0f;
        for (int slot = 9; slot < 45; ++slot) {
            ItemStack stack = Util.mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemPotion) || !ItemPotion.isSplash(stack.getMetadata()) || !this.isBuffPotion(stack) || !this.validatePotion(stack, hpTarget) || this.isOverVoid()) continue;
            this.beginPot(slot, forceJump);
            return;
        }
    }

    private void beginPot(int slot, boolean forceJump) {
        boolean shouldJump;
        this.prevSlot = Util.mc.thePlayer.inventory.currentItem;
        double xDist = Util.mc.thePlayer.posX - Util.mc.thePlayer.lastTickPosX;
        double zDist = Util.mc.thePlayer.posZ - Util.mc.thePlayer.lastTickPosZ;
        double speedValue = StrictMath.sqrt(xDist * xDist + zDist * zDist);
        boolean bl = shouldJump = (speedValue < 0.27 || forceJump) && this.potMode.getValue() != PotMode.Floor;
        if (shouldJump && Util.mc.thePlayer.onGround && !this.isBlockAbove() && this.getJumpBoostModifier() == 0) {
            Util.mc.thePlayer.motionX = 0.0;
            Util.mc.thePlayer.motionZ = 0.0;
            Util.mc.thePlayer.jump();
            this.jump = true;
            this.jumpTicks = 9;
        }
        haltTicks = 6;
        if (slot >= 36) {
            Util.mc.thePlayer.inventory.currentItem = this.potSlot = slot - 36;
            Util.mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(this.potSlot));
        } else {
            this.potSlot = 6;
            Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, slot, this.potSlot, 2, Util.mc.thePlayer);
            Util.mc.thePlayer.inventory.currentItem = this.potSlot;
            Util.mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(this.potSlot));
        }
        potting = true;
        this.thrown = false;
        this.readyToThrow = true;
        this.interactionTimer.reset();
    }

    private void finishPotting() {
        potting = false;
        this.thrown = false;
        this.readyToThrow = false;
        if (this.prevSlot != -1) {
            Util.mc.thePlayer.inventory.currentItem = this.prevSlot;
            Util.mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(this.prevSlot));
            this.prevSlot = -1;
        }
        this.potSlot = -1;
    }

    private boolean validatePotion(ItemStack stack, float hpTarget) {
        ItemPotion itemPotion = (ItemPotion)stack.getItem();
        for (PotionEffect effect : itemPotion.getEffects(stack)) {
            if (!this.checkEffectAmplifier(stack, effect)) continue;
            for (PotionType potionType : VALID_POTIONS) {
                if (!this.isPotionTypeEnabled(potionType) || potionType.potionId != effect.getPotionID()) continue;
                for (Requirement requirement : potionType.requirements) {
                    if (requirement.test(hpTarget, effect.getAmplifier(), potionType.potionId)) continue;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean isPotionTypeEnabled(PotionType type) {
        return switch (type.ordinal()) {
            default -> throw new IncompatibleClassChangeError();
            case 0 -> this.speed.getValue();
            case 2 -> this.jumpBoost.getValue();
            case 5 -> this.healing.getValue();
            case 1 -> this.regeneration.getValue();
            case 3 -> this.fireResistance.getValue();
            case 4 -> this.strength.getValue();
        };
    }

    private boolean checkEffectAmplifier(ItemStack stack, PotionEffect effectToCheck) {
        int bestAmplifier = -1;
        ItemStack bestStack = null;
        for (int i = 9; i < 45; ++i) {
            Object object;
            ItemStack stackInSlot = Util.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stackInSlot == null || !((object = stackInSlot.getItem()) instanceof ItemPotion)) continue;
            ItemPotion itemPotion = (ItemPotion)object;
            object = itemPotion.getEffects(stackInSlot).iterator();
            while (object.hasNext()) {
                PotionEffect effect = (PotionEffect)object.next();
                int amplifier = effect.getAmplifier();
                if (effect.getPotionID() != effectToCheck.getPotionID() || amplifier <= bestAmplifier) continue;
                bestStack = stackInSlot;
                bestAmplifier = amplifier;
            }
        }
        return bestStack == stack;
    }

    private boolean isBuffPotion(ItemStack stack) {
        ItemPotion potion = (ItemPotion)stack.getItem();
        for (PotionEffect effect : potion.getEffects(stack)) {
            int id = effect.getPotionID();
            if (!(id == Potion.moveSpeed.id && this.speed.getValue() != false || id == Potion.regeneration.id && this.regeneration.getValue() != false || id == Potion.jump.id && this.jumpBoost.getValue() != false || id == Potion.heal.id && this.healing.getValue() != false || id == Potion.damageBoost.id && this.strength.getValue() != false || id == Potion.fireResistance.id && this.fireResistance.getValue() != false) && id != Potion.resistance.id) continue;
            return true;
        }
        return false;
    }

    private boolean isNearby(float dist) {
        for (EntityPlayer player : Util.mc.theWorld.playerEntities) {
            if (player == Util.mc.thePlayer || !(Util.mc.thePlayer.getDistanceToEntity(player) <= dist)) continue;
            return true;
        }
        return false;
    }

    private boolean isOverVoid() {
        for (int i = 0; i < 256; ++i) {
            if (!Util.mc.theWorld.getBlockState(new BlockPos(Util.mc.thePlayer.posX, (double)i, Util.mc.thePlayer.posZ)).getBlock().getMaterial().isSolid()) continue;
            return false;
        }
        return true;
    }

    private boolean isBlockAbove() {
        return !Util.mc.theWorld.getBlockState(new BlockPos(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY + 2.0, Util.mc.thePlayer.posZ)).getBlock().getMaterial().isReplaceable();
    }

    private int getJumpBoostModifier() {
        Potion jumpPotion = Potion.jump;
        if (Util.mc.thePlayer.isPotionActive(jumpPotion)) {
            return Util.mc.thePlayer.getActivePotionEffect(jumpPotion).getAmplifier() + 1;
        }
        return 0;
    }

    @Override
    public void onEnable() {
        this.resetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetState();
        super.onDisable();
    }

    private void resetState() {
        this.prevSlot = -1;
        this.potSlot = -1;
        this.jump = false;
        this.jumpTicks = -1;
        potting = false;
        haltTicks = -1;
        this.thrown = false;
        this.readyToThrow = false;
        this.interactionTimer.reset();
    }

    static {
        haltTicks = -1;
    }

    private static enum PotMode {
        Floor("Floor"),
        Jump("Jump"),
        JumpOnly("Jump Only");

        private final String label;

        private PotMode(String label) {
            this.label = label;
        }

        public String toString() {
            return this.label;
        }
    }

    private static enum PotionType {
        SPEED(Potion.moveSpeed.id, Requirements.BETTER_THAN_CURRENT.requirement),
        REGEN(Potion.regeneration.id, Requirements.HEALTH_BELOW.requirement, Requirements.BETTER_THAN_CURRENT.requirement),
        JUMP_BOOST(Potion.jump.id, Requirements.BETTER_THAN_CURRENT.requirement),
        FIRE_RESISTANCE(Potion.fireResistance.id, Requirements.BETTER_THAN_CURRENT.requirement),
        STRENGTH(Potion.damageBoost.id, Requirements.BETTER_THAN_CURRENT.requirement),
        HEALING(Potion.heal.id, Requirements.HEALTH_BELOW.requirement);

        private final int potionId;
        private final Requirement[] requirements;

        private PotionType(int potionId, Requirement ... requirements) {
            this.potionId = potionId;
            this.requirements = requirements;
        }
    }

    private static interface Requirement {
        public boolean test(float var1, int var2, int var3);
    }

    private static class BetterThanCurrentRequirement
    implements Requirement {
        private BetterThanCurrentRequirement() {
        }

        @Override
        public boolean test(float healthTarget, int currentAmplifier, int potionId) {
            Potion potion = Potion.potionTypes[potionId];
            if (potion == null) {
                return true;
            }
            PotionEffect effect = Util.mc.thePlayer.getActivePotionEffect(potion);
            return effect == null || effect.getAmplifier() < currentAmplifier;
        }
    }

    private static class HealthBelowRequirement
    implements Requirement {
        private HealthBelowRequirement() {
        }

        @Override
        public boolean test(float healthTarget, int currentAmplifier, int potionId) {
            return Util.mc.thePlayer.getHealth() < healthTarget;
        }
    }

    private static enum Requirements {
        BETTER_THAN_CURRENT(new BetterThanCurrentRequirement()),
        HEALTH_BELOW(new HealthBelowRequirement());

        private final Requirement requirement;

        private Requirements(Requirement requirement) {
            this.requirement = requirement;
        }
    }
}

