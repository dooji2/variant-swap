package com.dooji.variantswap.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record VariantSwapRequestPayload(int slot, String targetId) {
    public static final Identifier ID = new Identifier("variant-swap", "swap_request");

    public void encode(PacketByteBuf buf) {
        buf.writeInt(slot);
        buf.writeString(targetId);
    }

    public static VariantSwapRequestPayload decode(PacketByteBuf buf) {
        return new VariantSwapRequestPayload(buf.readInt(), buf.readString(32767));
    }
}