package com.dooji.variantswap;

import com.dooji.variantswap.network.payloads.VariantDelayPayload;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.TranslatableText;

public class VariantSwapCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("variant-swap")
            .then(CommandManager.literal("cooldown")
                .then(CommandManager.literal("reset")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();

                        if (!source.hasPermissionLevel(VariantSwapConfig.getOpLevel())) {
                            source.sendError(new TranslatableText("variantswap.command.insufficient_permission"));
                            return 0;
                        }

                        VariantSwapConfig.setDelay(50);
                        sendDelayPayloadToAll(source.getServer());
                        source.sendFeedback(new TranslatableText("variantswap.command.cooldown_reset", 50), false);
                        return 1;
                    })
                )
                .then(CommandManager.argument("newMilliseconds", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();

                        if (!source.hasPermissionLevel(VariantSwapConfig.getOpLevel())) {
                            source.sendError(new TranslatableText("variantswap.command.insufficient_permission"));
                            return 0;
                        }

                        int newDelay = IntegerArgumentType.getInteger(context, "newMilliseconds");
                        VariantSwapConfig.setDelay(newDelay);
                        sendDelayPayloadToAll(source.getServer());
                        source.sendFeedback(new TranslatableText("variantswap.command.cooldown_set", newDelay), false);
                        return 1;
                    })
                )
            )
        );
    }

    private static void sendDelayPayloadToAll(MinecraftServer server) {
        VariantDelayPayload payload = new VariantDelayPayload(VariantSwapConfig.getDelay());
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        payload.encode(buf);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, VariantDelayPayload.ID, buf);
        }
    }
}