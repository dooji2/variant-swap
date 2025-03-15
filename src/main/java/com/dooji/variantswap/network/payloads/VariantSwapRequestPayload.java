package com.dooji.variantswap.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record VariantSwapRequestPayload(int slot, String targetId) implements CustomPayload {
    public static final CustomPayload.Id<VariantSwapRequestPayload> ID = new CustomPayload.Id<>(Identifier.of("variant-swap", "swap_request"));
    public static final PacketCodec<PacketByteBuf, VariantSwapRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, VariantSwapRequestPayload::slot,
            PacketCodecs.STRING, VariantSwapRequestPayload::targetId,
            VariantSwapRequestPayload::new
    );
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}