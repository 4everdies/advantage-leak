/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.modules.impl.movement.SpeedModule;
import cc.advantage.modules.impl.player.AutoArmorModule;
import cc.advantage.modules.impl.player.ManagerModule;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.modules.impl.player.StealerModule;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(label="Auto Disable", category=ModuleCategory.CLIENT)
public class AutoDisableModule
extends Module {
    public Property<Boolean> disableKillAuraWhenScaffoldProperty = new Property<Boolean>("Disable Kill Aura On Scaffold", true);
    public Property<Boolean> disableSpeedOnScaffoldProperty = new Property<Boolean>("Disable Speed On Scaffold", true);
    public Property<Boolean> disableKillAuraProperty = new Property<Boolean>("Disable Kill Aura On Flag", false);
    public Property<Boolean> disableSpeedProperty = new Property<Boolean>("Disable Speed On Flag", false);
    public Property<Boolean> disableScaffoldProperty = new Property<Boolean>("Disable Scaffold On Flag", true);
    public Property<Boolean> disableInvManagerProperty = new Property<Boolean>("Disable Manager On Flag", true);
    public Property<Boolean> disableAutoArmorProperty = new Property<Boolean>("Disable AutoArmor On Flag", true);
    public Property<Boolean> disableChestStealerProperty = new Property<Boolean>("Disable Stealer On Flag", true);
    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (this.disableKillAuraWhenScaffoldProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled() && Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class).isEnabled()) {
            Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).toggle();
        }
        if (this.disableSpeedOnScaffoldProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class).isEnabled() && Advantage.INSTANCE.getModuleManager().getModule(SpeedModule.class).isEnabled()) {
            Advantage.INSTANCE.getModuleManager().getModule(SpeedModule.class).toggle();
        }
    };
    @EventLink
    private final Listener<PacketReceiveEvent> packetReceiveEventListener = e -> {
        if (e.getPacket() instanceof S08PacketPlayerPosLook) {
            if (this.disableKillAuraProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled()) {
                Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).toggle();
            }
            if (this.disableSpeedProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(SpeedModule.class).isEnabled()) {
                Advantage.INSTANCE.getModuleManager().getModule(SpeedModule.class).toggle();
            }
            if (this.disableInvManagerProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(ManagerModule.class).isEnabled()) {
                Advantage.INSTANCE.getModuleManager().getModule(ManagerModule.class).toggle();
            }
            if (this.disableAutoArmorProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(AutoArmorModule.class).isEnabled()) {
                Advantage.INSTANCE.getModuleManager().getModule(AutoArmorModule.class).toggle();
            }
            if (this.disableChestStealerProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(StealerModule.class).isEnabled()) {
                Advantage.INSTANCE.getModuleManager().getModule(StealerModule.class).toggle();
            }
            if (this.disableScaffoldProperty.getValue().booleanValue() && Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class).isEnabled()) {
                Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class).toggle();
            }
        }
    };
}

