package com.dooji.variantswap.network;

import com.dooji.variantswap.VariantSwapClient;
import com.dooji.variantswap.network.payloads.VariantMappingPayload;
import com.dooji.variantswap.network.payloads.VariantDelayPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class VariantSwapClientNetworking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(VariantMappingPayload.ID, (payload, context) -> {
            context.client().execute(() -> VariantSwapClient.updateMapping(payload.mapping()));
        });
        
        ClientPlayNetworking.registerGlobalReceiver(VariantDelayPayload.ID, (payload, context) -> {
            int delay = payload.delay();
            VariantSwapClient.swapCooldown = delay;
        });
    }
}