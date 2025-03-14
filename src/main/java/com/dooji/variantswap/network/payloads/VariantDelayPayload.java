package com.dooji.variantswap.network.payloads;

import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record VariantDelayPayload(int delay) implements CustomPayload {
    public static final CustomPayload.Id<VariantDelayPayload> ID = new CustomPayload.Id<>(Identifier.of("variant-swap", "delay"));
    public static final PacketCodec<PacketByteBuf, VariantDelayPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, VariantDelayPayload::delay,
            VariantDelayPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public PacketByteBuf toPacketByteBuf() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(delay);
        return buf;
    }
}