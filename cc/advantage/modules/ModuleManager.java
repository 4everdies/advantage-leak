/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.KeyPressEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.impl.client.AntiBotModule;
import cc.advantage.modules.impl.client.AutoDisableModule;
import cc.advantage.modules.impl.client.AutoPlayModule;
import cc.advantage.modules.impl.client.BlinkModule;
import cc.advantage.modules.impl.client.ClientSettingsModule;
import cc.advantage.modules.impl.client.ClientSpooferModule;
import cc.advantage.modules.impl.client.DisablerModule;
import cc.advantage.modules.impl.client.FakeLagModule;
import cc.advantage.modules.impl.client.FpsEnhancerModule;
import cc.advantage.modules.impl.client.HitSoundsModule;
import cc.advantage.modules.impl.client.KillSultsModule;
import cc.advantage.modules.impl.client.MCFModule;
import cc.advantage.modules.impl.client.MurderDetectorModule;
import cc.advantage.modules.impl.client.NoCrashModule;
import cc.advantage.modules.impl.client.NoRotateModule;
import cc.advantage.modules.impl.client.PluginDetectorModule;
import cc.advantage.modules.impl.client.ThemeModule;
import cc.advantage.modules.impl.client.TimerModule;
import cc.advantage.modules.impl.client.ToggleSoundsModule;
import cc.advantage.modules.impl.combat.AIFighterModule;
import cc.advantage.modules.impl.combat.AimAssistModule;
import cc.advantage.modules.impl.combat.AutoClickerModule;
import cc.advantage.modules.impl.combat.AutoGappleModule;
import cc.advantage.modules.impl.combat.AutoPotModule;
import cc.advantage.modules.impl.combat.AutoProjectileModule;
import cc.advantage.modules.impl.combat.AutoRodModule;
import cc.advantage.modules.impl.combat.AutoSoupModule;
import cc.advantage.modules.impl.combat.BackTrackModule;
import cc.advantage.modules.impl.combat.BlinkRangeModule;
import cc.advantage.modules.impl.combat.CriticalsModule;
import cc.advantage.modules.impl.combat.FastBowModule;
import cc.advantage.modules.impl.combat.HitFlickModule;
import cc.advantage.modules.impl.combat.HitSelectionModule;
import cc.advantage.modules.impl.combat.HitboxExpandModule;
import cc.advantage.modules.impl.combat.KeepSprintModule;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.modules.impl.combat.KnockbackDelayModule;
import cc.advantage.modules.impl.combat.LagBreakerModule;
import cc.advantage.modules.impl.combat.LagRangeModule;
import cc.advantage.modules.impl.combat.MoreKBModule;
import cc.advantage.modules.impl.combat.NoHitDelayModule;
import cc.advantage.modules.impl.combat.ReachModule;
import cc.advantage.modules.impl.combat.TPAuraModule;
import cc.advantage.modules.impl.combat.TargetStrafeModule;
import cc.advantage.modules.impl.combat.TickBaseModule;
import cc.advantage.modules.impl.combat.TimerRangeModule;
import cc.advantage.modules.impl.combat.TriggerBotModule;
import cc.advantage.modules.impl.combat.VelocityModule;
import cc.advantage.modules.impl.combat.WTapModule;
import cc.advantage.modules.impl.movement.ClickTeleportModule;
import cc.advantage.modules.impl.movement.FlightModule;
import cc.advantage.modules.impl.movement.JesusModule;
import cc.advantage.modules.impl.movement.LongJumpModule;
import cc.advantage.modules.impl.movement.NoJumpDelayModule;
import cc.advantage.modules.impl.movement.NoSlowModule;
import cc.advantage.modules.impl.movement.NoWebModule;
import cc.advantage.modules.impl.movement.PhaseModule;
import cc.advantage.modules.impl.movement.SafeWalkModule;
import cc.advantage.modules.impl.movement.SpeedModule;
import cc.advantage.modules.impl.movement.SpiderModule;
import cc.advantage.modules.impl.movement.SprintModule;
import cc.advantage.modules.impl.movement.StepModule;
import cc.advantage.modules.impl.movement.StopMovementModule;
import cc.advantage.modules.impl.player.AntiVoidModule;
import cc.advantage.modules.impl.player.AutoArmorModule;
import cc.advantage.modules.impl.player.AutoRecraftModule;
import cc.advantage.modules.impl.player.AutoToolModule;
import cc.advantage.modules.impl.player.BedNukerModule;
import cc.advantage.modules.impl.player.ClutchModule;
import cc.advantage.modules.impl.player.FastBreakModule;
import cc.advantage.modules.impl.player.FastPlaceModule;
import cc.advantage.modules.impl.player.FastUseModule;
import cc.advantage.modules.impl.player.GuiClickerModule;
import cc.advantage.modules.impl.player.InvMoveModule;
import cc.advantage.modules.impl.player.LegitScaffoldModule;
import cc.advantage.modules.impl.player.ManagerModule;
import cc.advantage.modules.impl.player.NoFallModule;
import cc.advantage.modules.impl.player.NoFireballModule;
import cc.advantage.modules.impl.player.RegenModule;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.modules.impl.player.SpinBotModule;
import cc.advantage.modules.impl.player.StealerModule;
import cc.advantage.modules.impl.visuals.AmbienceModule;
import cc.advantage.modules.impl.visuals.ArrayListModule;
import cc.advantage.modules.impl.visuals.AspectRatioModule;
import cc.advantage.modules.impl.visuals.BarrierVisionModule;
import cc.advantage.modules.impl.visuals.BedPlatesModule;
import cc.advantage.modules.impl.visuals.BlockOutlineModule;
import cc.advantage.modules.impl.visuals.BreadCrumbsModule;
import cc.advantage.modules.impl.visuals.CameraModule;
import cc.advantage.modules.impl.visuals.CapesModule;
import cc.advantage.modules.impl.visuals.ChamsModule;
import cc.advantage.modules.impl.visuals.ChinaHatModule;
import cc.advantage.modules.impl.visuals.DamageFXModule;
import cc.advantage.modules.impl.visuals.ESPModule;
import cc.advantage.modules.impl.visuals.FireFliesModule;
import cc.advantage.modules.impl.visuals.FreeLookModule;
import cc.advantage.modules.impl.visuals.FullBrightModule;
import cc.advantage.modules.impl.visuals.GlintModule;
import cc.advantage.modules.impl.visuals.HaloModule;
import cc.advantage.modules.impl.visuals.InfoDisplayModule;
import cc.advantage.modules.impl.visuals.ItemESPModule;
import cc.advantage.modules.impl.visuals.MotionBlurModule;
import cc.advantage.modules.impl.visuals.NameTagsModule;
import cc.advantage.modules.impl.visuals.NickHiderModule;
import cc.advantage.modules.impl.visuals.NotificationsModule;
import cc.advantage.modules.impl.visuals.OverlayModule;
import cc.advantage.modules.impl.visuals.PostProcessingModule;
import cc.advantage.modules.impl.visuals.RadarModule;
import cc.advantage.modules.impl.visuals.ScoreboardModule;
import cc.advantage.modules.impl.visuals.SessionInformationModule;
import cc.advantage.modules.impl.visuals.StorageESPModule;
import cc.advantage.modules.impl.visuals.TargetInterfaceModule;
import cc.advantage.modules.impl.visuals.TracersModule;
import cc.advantage.modules.impl.visuals.TrajectoriesModule;
import cc.advantage.modules.impl.visuals.WatermarkModule;
import com.google.common.collect.ImmutableClassToInstanceMap;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private final ImmutableClassToInstanceMap<Module> instanceMap;
    private final Map<ModuleCategory, List<Module>> modulesByCategory;
    @EventLink
    public final Listener<KeyPressEvent> onKeyPress = event -> {
        int keyPressed = event.getKey();
        for (Module module : this.getModules()) {
            int moduleBind = module.getKey();
            if (moduleBind != keyPressed) continue;
            module.toggle();
        }
    };

    public ModuleManager() {
        this.instanceMap = this.putInInstanceMap(new KillAuraModule(), new AIFighterModule(), new TPAuraModule(), new TargetStrafeModule(), new CriticalsModule(), new TickBaseModule(), new TimerRangeModule(), new BlinkRangeModule(), new LagRangeModule(), new LagBreakerModule(), new KnockbackDelayModule(), new VelocityModule(), new BackTrackModule(), new HitFlickModule(), new MoreKBModule(), new FastBowModule(), new WTapModule(), new HitSelectionModule(), new AutoClickerModule(), new KeepSprintModule(), new AimAssistModule(), new TriggerBotModule(), new AutoRodModule(), new AutoProjectileModule(), new AutoPotModule(), new AutoGappleModule(), new AutoSoupModule(), new NoHitDelayModule(), new ReachModule(), new HitboxExpandModule(), new SprintModule(), new SpeedModule(), new FlightModule(), new LongJumpModule(), new NoSlowModule(), new NoWebModule(), new SafeWalkModule(), new StepModule(), new JesusModule(), new NoJumpDelayModule(), new StopMovementModule(), new PhaseModule(), new SpiderModule(), new ClickTeleportModule(), new ScaffoldModule(), new NoFallModule(), new AntiVoidModule(), new AutoToolModule(), new AutoRecraftModule(), new FastBreakModule(), new FastUseModule(), new RegenModule(), new FastPlaceModule(), new StealerModule(), new ManagerModule(), new AutoArmorModule(), new InvMoveModule(), new LegitScaffoldModule(), new BedNukerModule(), new SpinBotModule(), new NoFireballModule(), new GuiClickerModule(), new ClutchModule(), new ClientSettingsModule(), new ThemeModule(), new FpsEnhancerModule(), new AutoDisableModule(), new NoCrashModule(), new PluginDetectorModule(), new ToggleSoundsModule(), new HitSoundsModule(), new AntiBotModule(), new MCFModule(), new DisablerModule(), new FakeLagModule(), new ClientSpooferModule(), new AutoPlayModule(), new TimerModule(), new BlinkModule(), new NoRotateModule(), new MurderDetectorModule(), new KillSultsModule(), new ArrayListModule(), new WatermarkModule(), new InfoDisplayModule(), new NotificationsModule(), new RadarModule(), new CapesModule(), new GlintModule(), new CameraModule(), new AmbienceModule(), new ESPModule(), new ChamsModule(), new TracersModule(), new DamageFXModule(), new TargetInterfaceModule(), new OverlayModule(), new PostProcessingModule(), new ScoreboardModule(), new SessionInformationModule(), new NickHiderModule(), new NameTagsModule(), new StorageESPModule(), new ChinaHatModule(), new HaloModule(), new FireFliesModule(), new BreadCrumbsModule(), new AspectRatioModule(), new TrajectoriesModule(), new BedPlatesModule(), new FullBrightModule(), new FreeLookModule(), new BarrierVisionModule(), new BlockOutlineModule(), new MotionBlurModule(), new ItemESPModule());
        this.modulesByCategory = this.buildCategoryCache();
        this.getModules().forEach(Module::reflectProperties);
        this.getModules().forEach(Module::resetPropertyValues);
        Advantage.INSTANCE.getEventBus().subscribe(this);
    }

    public void postInit() {
        this.getModules().forEach(Module::resetPropertyValues);
    }

    private ImmutableClassToInstanceMap<Module> putInInstanceMap(Module ... modules) {
        ImmutableClassToInstanceMap.Builder modulesBuilder = ImmutableClassToInstanceMap.builder();
        Arrays.stream(modules).forEach(module -> modulesBuilder.put(module.getClass(), module));
        return modulesBuilder.build();
    }

    private Map<ModuleCategory, List<Module>> buildCategoryCache() {
        EnumMap<ModuleCategory, List<Object>> cache = new EnumMap<ModuleCategory, List<Object>>(ModuleCategory.class);
        for (Object category : ModuleCategory.values()) {
            cache.put((ModuleCategory)((Object)category), new ArrayList());
        }
        for (Module module : this.getModules()) {
            ((List)cache.get((Object)module.getCategory())).add(module);
        }
        for (Object category : ModuleCategory.values()) {
            cache.put((ModuleCategory)((Object)category), Collections.unmodifiableList((List)cache.get(category)));
        }
        return Collections.unmodifiableMap(cache);
    }

    public Collection<Module> getModules() {
        return this.instanceMap.values();
    }

    public <T extends Module> T getModule(Class<T> moduleClass) {
        return (T)((Module)this.instanceMap.getInstance(moduleClass));
    }

    public Module getModule(String label) {
        return this.getModules().stream().filter(module -> module.getLabel().replaceAll(" ", "").equalsIgnoreCase(label)).findFirst().orElse(null);
    }

    public static <T extends Module> T getInstance(Class<T> clazz) {
        return Advantage.INSTANCE.getModuleManager().getModule(clazz);
    }

    public List<Module> getModulesForCategory(ModuleCategory category) {
        return this.modulesByCategory.getOrDefault((Object)category, Collections.emptyList());
    }
}

