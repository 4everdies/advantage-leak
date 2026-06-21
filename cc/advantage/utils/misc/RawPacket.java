/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.utils.misc;

import java.io.IOException;
import lombok.Generated;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

public abstract class RawPacket
implements Packet {
    private final int packetID;
    private final EnumConnectionState direction;

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void processPacket(INetHandler handler) {
    }

    @Generated
    public RawPacket(int packetID, EnumConnectionState direction) {
        this.packetID = packetID;
        this.direction = direction;
    }

    @Generated
    public int getPacketID() {
        return this.packetID;
    }

    @Generated
    public EnumConnectionState getDirection() {
        return this.direction;
    }
}

