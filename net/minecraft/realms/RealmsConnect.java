/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.realms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsConnect {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen onlineScreen;
    private volatile boolean aborted = false;
    private NetworkManager connection;

    public RealmsConnect(RealmsScreen p_i1079_1_) {
        this.onlineScreen = p_i1079_1_;
    }

    public void connect(final String p_connect_1_, final int p_connect_2_) {
        Realms.setConnectedToRealms(true);
        new Thread(this, "Realms-connect-task"){
            final /* synthetic */ RealmsConnect this$0;
            {
                this.this$0 = this$0;
                super(arg0);
            }

            @Override
            public void run() {
                InetAddress inetaddress = null;
                try {
                    inetaddress = InetAddress.getByName(p_connect_1_);
                    if (this.this$0.aborted) {
                        return;
                    }
                    this.this$0.connection = NetworkManager.createNetworkManagerAndConnect(inetaddress, p_connect_2_, Minecraft.getMinecraft().gameSettings.isUsingNativeTransport());
                    if (this.this$0.aborted) {
                        return;
                    }
                    this.this$0.connection.setNetHandler(new NetHandlerLoginClient(this.this$0.connection, Minecraft.getMinecraft(), this.this$0.onlineScreen.getProxy()));
                    if (this.this$0.aborted) {
                        return;
                    }
                    this.this$0.connection.sendPacket(new C00Handshake(47, p_connect_1_, p_connect_2_, EnumConnectionState.LOGIN));
                    if (this.this$0.aborted) {
                        return;
                    }
                    this.this$0.connection.sendPacket(new C00PacketLoginStart(Minecraft.getMinecraft().getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception) {
                    Realms.clearResourcePack();
                    if (this.this$0.aborted) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to world", (Throwable)unknownhostexception);
                    Minecraft.getMinecraft().getResourcePackRepository().clearResourcePack();
                    Realms.setScreen(new DisconnectedRealmsScreen(this.this$0.onlineScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", "Unknown host '" + p_connect_1_ + "'")));
                }
                catch (Exception exception) {
                    Realms.clearResourcePack();
                    if (this.this$0.aborted) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to world", (Throwable)exception);
                    String s = exception.toString();
                    if (inetaddress != null) {
                        String s1 = inetaddress.toString() + ":" + p_connect_2_;
                        s = s.replaceAll(s1, "");
                    }
                    Realms.setScreen(new DisconnectedRealmsScreen(this.this$0.onlineScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", s)));
                }
            }
        }.start();
    }

    public void abort() {
        this.aborted = true;
    }

    public void tick() {
        if (this.connection != null) {
            if (this.connection.isChannelOpen()) {
                this.connection.processReceivedPackets();
            } else {
                this.connection.checkDisconnected();
            }
        }
    }
}

