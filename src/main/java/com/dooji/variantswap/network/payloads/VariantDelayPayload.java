package com.dooji.variantswap.network.payloads;

import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record VariantDelayPayload(int delay) {
    public static final Identifier ID = new Identifier("variant-swap", "delay");

    public void encode(PacketByteBuf buf) {
        buf.writeInt(delay);
    }

    public static VariantDelayPayload decode(PacketByteBuf buf) {
        return new VariantDelayPayload(buf.readInt());
    }

    public PacketByteBuf toPacketByteBuf() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        encode(buf);
        
        return buf;
    }
}