package com.dooji.variantswap.network;

import com.dooji.variantswap.VariantSwapConfig;
import com.dooji.variantswap.network.payloads.VariantSwapRequestPayload;
import com.dooji.variantswap.network.payloads.VariantDelayPayload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class VariantSwapNetworking {
    public static void init() {
        PayloadTypeRegistry.playS2C().register(VariantDelayPayload.ID, VariantDelayPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(VariantSwapRequestPayload.ID, VariantSwapRequestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(VariantSwapRequestPayload.ID, (payload, context) -> {
            context.player().getServer().execute(() -> {
                processSwapRequest(context.player(), payload);
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            VariantDelayPayload delayPayload = new VariantDelayPayload(VariantSwapConfig.getDelay());
            ServerPlayNetworking.send(handler.getPlayer(), delayPayload);
        });
    }
    
    private static void processSwapRequest(ServerPlayerEntity player, VariantSwapRequestPayload payload) {
        int slot = payload.slot();
    
        if (player.getInventory().getStack(slot).isEmpty()) {
            return;
        }
    
        Identifier targetId = Identifier.of(payload.targetId());
        ItemStack heldStack = player.getInventory().getStack(slot);
    
        if (player.isCreative()) {
            Item candidateItem = Registries.ITEM.get(targetId);

            if (candidateItem == null) return;

            ItemStack newStack = new ItemStack(candidateItem, heldStack.getCount());
            player.getInventory().setStack(slot, newStack);

            return;
        }

        int bestSlot = -1;
        int bestCount = 0;
        for (int invSlot = 0; invSlot < player.getInventory().size(); invSlot++) {
            if (invSlot == slot) continue;

            ItemStack stack = player.getInventory().getStack(invSlot);
            if (!stack.isEmpty()) {
                Identifier stackId = Registries.ITEM.getId(stack.getItem());

                if (stackId.equals(targetId)) {
                    int count = stack.getCount();

                    if (count > bestCount) {
                        bestCount = count;
                        bestSlot = invSlot;
                    }
                }
            }
        }
    
        if (bestSlot != -1) {
            ItemStack targetStack = player.getInventory().getStack(bestSlot);

            player.getInventory().setStack(bestSlot, heldStack);
            player.getInventory().setStack(slot, targetStack);

            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, slot, player.getInventory().getStack(slot)));
            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, bestSlot, player.getInventory().getStack(bestSlot)));
        }
    }    
}