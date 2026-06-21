/*
 * Decompiled with CFR 0.152.
 */
package de.florianmichael.vialoadingbase.platform.viaversion;

import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.buffer.ByteBuf;

public class VLBViaAPIWrapper
extends ViaAPIBase<UserConnection> {
    @Override
    public ProtocolVersion getPlayerProtocolVersion(UserConnection player) {
        return player.getProtocolInfo().protocolVersion();
    }

    @Override
    public void sendRawPacket(UserConnection player, ByteBuf packet) {
        player.scheduleSendRawPacket(packet);
    }
}

