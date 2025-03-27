package com.dooji.variantswap.network;

import com.dooji.variantswap.VariantSwapClient;
import com.dooji.variantswap.network.payloads.VariantDelayPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class VariantSwapClientNetworking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(VariantDelayPayload.ID, (client, handler, buf, responseSender) -> {
            VariantDelayPayload payload = VariantDelayPayload.decode(buf);
            VariantSwapClient.swapCooldown = payload.delay();
        });
    }
}