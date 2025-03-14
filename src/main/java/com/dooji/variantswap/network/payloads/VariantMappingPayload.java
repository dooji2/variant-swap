package com.dooji.variantswap.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public record VariantMappingPayload(Map<String, List<String>> mapping) implements CustomPayload {
    public static final CustomPayload.Id<VariantMappingPayload> ID = new CustomPayload.Id<>(Identifier.of("variant-swap", "mapping"));
    public static final PacketCodec<PacketByteBuf, VariantMappingPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.map(
                    (size) -> new java.util.HashMap<String, List<String>>(),
                    PacketCodecs.STRING,
                    PacketCodecs.collection(java.util.ArrayList::new, PacketCodecs.STRING)
            ),
            VariantMappingPayload::mapping,
            VariantMappingPayload::new
    );
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
