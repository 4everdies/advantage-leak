/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.misc;

import cc.advantage.utils.misc.RawPacket;
import java.io.IOException;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.PacketBuffer;

public final class PlayPongC2SPacket
extends RawPacket {
    private int parameter;

    public PlayPongC2SPacket(int parameter) {
        super(0, EnumConnectionState.PLAY);
        this.parameter = parameter;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.parameter = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(this.parameter);
    }

    @Override
    public int getPacketID() {
        return 29;
    }
}

