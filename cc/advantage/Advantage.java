/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage;

import cc.advantage.api.commands.CommandHandler;
import cc.advantage.api.commands.impl.BindCommand;
import cc.advantage.api.commands.impl.BindsCommand;
import cc.advantage.api.commands.impl.ClientNameCommand;
import cc.advantage.api.commands.impl.ConfigCommand;
import cc.advantage.api.commands.impl.HelpCommand;
import cc.advantage.api.commands.impl.HideCommand;
import cc.advantage.api.commands.impl.ToggleCommand;
import cc.advantage.api.config.BindsConfig;
import cc.advantage.api.config.ConfigManager;
import cc.advantage.api.events.Event;
import cc.advantage.api.events.impl.game.ClientStartupEvent;
import cc.advantage.api.events.impl.game.KeyPressEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.discord.DiscordRPCManager;
import cc.advantage.interfaces.click.ClickInterface;
import cc.advantage.interfaces.click.WindowClickInterface;
import cc.advantage.modules.ModuleManager;
import cc.advantage.modules.impl.client.ClientSettingsModule;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.modules.impl.visuals.NotificationsModule;
import cc.advantage.processes.BackgroundProcess;
import cc.advantage.processes.BadPacketsProcess;
import cc.advantage.processes.BlinkProcess;
import cc.advantage.processes.ColorProcess;
import cc.advantage.processes.LagManagerSubscriber;
import cc.advantage.processes.LagProcess;
import cc.advantage.processes.RotationProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.client.BuildType;
import cc.advantage.utils.render.FontUtils;
import de.florianmichael.viamcp.ViaMCP;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.bus.impl.EventBus;
import java.util.Arrays;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class Advantage {
    public static final Advantage INSTANCE = new Advantage();
    public static final String NAME = "Advantage";
    public static final String BUILD = BuildType.RELEASE.getName();
    public static final String VERSION = " " + BUILD;
    public static final String FULL = "Advantage " + VERSION;
    private static boolean loggedIn;
    private static String loggedUsername;
    private EventBus<Event> eventBus;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private BindsConfig bindsConfig;
    private CommandHandler commandHandler;
    private BackgroundProcess backgroundProcess;
    private RotationProcess rotationProcess;
    private ColorProcess colorProcess;
    private ClickInterface clickInterface;
    private WindowClickInterface windowClickInterface;
    private LagProcess lagProcess;
    private BlinkProcess blinkProcess;
    private BadPacketsProcess badPacketsProcess;
    private TargetSelectionProcess targetSelectionProcess;
    private static long startTime;
    @EventLink
    public final Listener<ClientStartupEvent> onClientStart = e -> {
        startTime = System.currentTimeMillis();
        this.moduleManager = new ModuleManager();
        this.moduleManager.postInit();
        this.configManager = new ConfigManager();
        this.getEventBus().subscribe(this.configManager);
        this.bindsConfig = new BindsConfig();
        this.getEventBus().subscribe(this.bindsConfig);
        this.backgroundProcess = new BackgroundProcess();
        this.getEventBus().subscribe(this.backgroundProcess);
        this.rotationProcess = new RotationProcess();
        this.getEventBus().subscribe(this.rotationProcess);
        this.colorProcess = new ColorProcess();
        this.getEventBus().subscribe(this.colorProcess);
        this.configManager.loadConfig("default");
        this.bindsConfig.loadFromFile();
        this.lagProcess = new LagProcess();
        this.getEventBus().subscribe(this.lagProcess);
        this.blinkProcess = new BlinkProcess();
        this.getEventBus().subscribe(this.blinkProcess);
        this.badPacketsProcess = new BadPacketsProcess();
        this.getEventBus().subscribe(this.badPacketsProcess);
        this.targetSelectionProcess = new TargetSelectionProcess();
        this.getEventBus().subscribe(this.targetSelectionProcess);
        this.getEventBus().subscribe(new LagManagerSubscriber());
        this.commandHandler = new CommandHandler();
        this.commandHandler.commands.addAll(Arrays.asList(new BindCommand(), new ClientNameCommand(), new BindsCommand(), new ToggleCommand(), new ConfigCommand(), new HideCommand(), new HelpCommand()));
        this.getEventBus().subscribe(this.commandHandler);
        if (this.moduleManager.getModule(KillAuraModule.class).isEnabled()) {
            this.moduleManager.getModule(KillAuraModule.class).setEnabled(false);
        }
        if (this.moduleManager.getModule(ScaffoldModule.class).isEnabled()) {
            this.moduleManager.getModule(ScaffoldModule.class).setEnabled(false);
        }
        try {
            ViaMCP.create();
            ViaMCP.INSTANCE.initAsyncSlider();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        DiscordRPCManager.start();
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPCManager::stop));
    };
    @EventLink
    public final Listener<KeyPressEvent> keyPressEventListener = e -> {
        if (e.getKey() == Keyboard.KEY_RSHIFT) {
            switch ((ClientSettingsModule.ClickInterface)((Object)((Object)ClientSettingsModule.clickInterface.getValue()))) {
                case Normal: {
                    if (this.clickInterface == null) {
                        this.clickInterface = new ClickInterface();
                    }
                    Minecraft.getMinecraft().displayGuiScreen(this.clickInterface);
                    break;
                }
                case Window: {
                    if (this.windowClickInterface == null) {
                        this.windowClickInterface = new WindowClickInterface();
                    }
                    Minecraft.getMinecraft().displayGuiScreen(this.windowClickInterface);
                }
            }
        }
    };
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> {
        String desiredFont;
        String currentFont;
        if (!INSTANCE.getModuleManager().getModule(ClientSettingsModule.class).isEnabled()) {
            INSTANCE.getModuleManager().getModule(ClientSettingsModule.class).setEnabled(true);
        }
        if (!INSTANCE.getModuleManager().getModule(NotificationsModule.class).isEnabled()) {
            INSTANCE.getModuleManager().getModule(NotificationsModule.class).setEnabled(true);
        }
        if (!INSTANCE.getModuleManager().getModule(ClientSettingsModule.class).isHidden()) {
            INSTANCE.getModuleManager().getModule(ClientSettingsModule.class).setHidden(true);
        }
        if (!INSTANCE.getModuleManager().getModule(NotificationsModule.class).isHidden()) {
            INSTANCE.getModuleManager().getModule(NotificationsModule.class).setHidden(true);
        }
        if (!(currentFont = FontUtils.getCurrentFont().getNameFontTTF().toLowerCase()).equals(desiredFont = ((ClientSettingsModule.Font)((Object)((Object)ClientSettingsModule.font.getValue()))).toString().toLowerCase())) {
            FontUtils.setCurrentFont(desiredFont);
        }
    };

    private Advantage() {
        this.getEventBus().subscribe(this);
    }

    public EventBus<Event> getEventBus() {
        if (this.eventBus == null) {
            this.eventBus = new EventBus();
        }
        return this.eventBus;
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        return obj;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static String getLoggedUsername() {
        return loggedUsername;
    }

    public static void onLoginSuccess(String username) {
        loggedUsername = username;
        loggedIn = true;
    }

    public static void logout() {
        loggedUsername = null;
        loggedIn = false;
    }

    @Generated
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    @Generated
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Generated
    public BindsConfig getBindsConfig() {
        return this.bindsConfig;
    }

    @Generated
    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    @Generated
    public static long getStartTime() {
        return startTime;
    }
}
