/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiConnecting
extends GuiScreen {
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private NetworkManager networkManager;
    private boolean cancel;
    private final GuiScreen previousGuiScreen;

    public GuiConnecting(GuiScreen p_i1181_1_, Minecraft mcIn, ServerData p_i1181_3_) {
        this.mc = mcIn;
        this.previousGuiScreen = p_i1181_1_;
        ServerAddress serveraddress = ServerAddress.fromString(p_i1181_3_.serverIP);
        mcIn.loadWorld(null);
        mcIn.setServerData(p_i1181_3_);
        this.connect(serveraddress.getIP(), serveraddress.getPort());
    }

    public GuiConnecting(GuiScreen p_i1182_1_, Minecraft mcIn, String hostName, int port) {
        this.mc = mcIn;
        this.previousGuiScreen = p_i1182_1_;
        mcIn.loadWorld(null);
        this.connect(hostName, port);
    }

    private void connect(final String ip, final int port) {
        logger.info("Connecting to " + ip + ", " + port);
        new Thread(this, "Server Connector #" + CONNECTION_ID.incrementAndGet()){
            final /* synthetic */ GuiConnecting this$0;
            {
                this.this$0 = this$0;
                super(arg0);
            }

            @Override
            public void run() {
                InetAddress inetaddress = null;
                try {
                    if (this.this$0.cancel) {
                        return;
                    }
                    inetaddress = InetAddress.getByName(ip);
                    this.this$0.networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, ((GuiConnecting)this.this$0).mc.gameSettings.isUsingNativeTransport());
                    this.this$0.networkManager.setNetHandler(new NetHandlerLoginClient(this.this$0.networkManager, this.this$0.mc, this.this$0.previousGuiScreen));
                    this.this$0.networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN));
                    this.this$0.networkManager.sendPacket(new C00PacketLoginStart(this.this$0.mc.getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception) {
                    if (this.this$0.cancel) {
                        return;
                    }
                    logger.error("Couldn't connect to server", (Throwable)unknownhostexception);
                    this.this$0.mc.displayGuiScreen(new GuiDisconnected(this.this$0.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", "Unknown host")));
                }
                catch (Exception exception) {
                    if (this.this$0.cancel) {
                        return;
                    }
                    logger.error("Couldn't connect to server", (Throwable)exception);
                    String s = exception.toString();
                    if (inetaddress != null) {
                        String s1 = String.valueOf(inetaddress) + ":" + port;
                        s = s.replaceAll(s1, "");
                    }
                    this.this$0.mc.displayGuiScreen(new GuiDisconnected(this.this$0.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", s)));
                }
            }
        }.start();
    }

    @Override
    public void updateScreen() {
        if (this.networkManager != null) {
            if (this.networkManager.isChannelOpen()) {
                this.networkManager.processReceivedPackets();
            } else {
                this.networkManager.checkDisconnected();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel", new Object[0])));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.cancel = true;
            if (this.networkManager != null) {
                this.networkManager.closeChannel(new ChatComponentText("Aborted"));
            }
            this.mc.displayGuiScreen(this.previousGuiScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if (this.networkManager == null) {
            GuiConnecting.drawCenteredString(this.fontRendererObj, I18n.format("connect.connecting", new Object[0]), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        } else {
            GuiConnecting.drawCenteredString(this.fontRendererObj, I18n.format("connect.authorizing", new Object[0]), this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

