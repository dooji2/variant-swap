package com.dooji.variantswap.network;

import com.dooji.variantswap.VariantSwapConfig;
import com.dooji.variantswap.network.payloads.VariantSwapRequestPayload;
import com.dooji.variantswap.network.payloads.VariantDelayPayload;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class VariantSwapNetworking {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(VariantSwapRequestPayload.ID, (server, player, handler, buf, responseSender) -> {
            VariantSwapRequestPayload payload = VariantSwapRequestPayload.decode(buf);
            server.execute(() -> {
                processSwapRequest(player, payload);
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            VariantDelayPayload delayPayload = new VariantDelayPayload(VariantSwapConfig.getDelay());
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            
            delayPayload.encode(buf);
            ServerPlayNetworking.send(handler.getPlayer(), VariantDelayPayload.ID, buf);
        });
    }
    
    private static void processSwapRequest(ServerPlayerEntity player, VariantSwapRequestPayload payload) {
        int slot = payload.slot();
    
        if (player.getInventory().getStack(slot).isEmpty()) {
            return;
        }
    
        Identifier targetId = new Identifier(payload.targetId());
        ItemStack heldStack = player.getInventory().getStack(slot);
    
        if (player.isCreative()) {
            Item candidateItem = Registry.ITEM.get(targetId);

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
                Identifier stackId = Registry.ITEM.getId(stack.getItem());

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