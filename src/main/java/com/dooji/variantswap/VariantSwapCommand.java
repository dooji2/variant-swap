package com.dooji.variantswap;

import com.dooji.variantswap.network.payloads.VariantDelayPayload;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class VariantSwapCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher, registryAccess, environment);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("variantswap")
            .then(CommandManager.literal("cooldown")
                .then(CommandManager.literal("reset")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();

                        if (!source.hasPermissionLevel(VariantSwapConfig.getOpLevel())) {
                            source.sendError(Text.translatable("variantswap.command.insufficient_permission"));
                            return 0;
                        }

                        VariantSwapConfig.setDelay(50);
                        sendDelayPayloadToAll(source.getServer());
                        source.sendFeedback(() -> Text.translatable("variantswap.command.cooldown_reset", 50), false);
                        return 1;
                    })
                )
                .then(CommandManager.argument("newMilliseconds", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();

                        if (!source.hasPermissionLevel(VariantSwapConfig.getOpLevel())) {
                            source.sendError(Text.translatable("variantswap.command.insufficient_permission"));
                            return 0;
                        }

                        int newDelay = IntegerArgumentType.getInteger(context, "newMilliseconds");
                        VariantSwapConfig.setDelay(newDelay);
                        sendDelayPayloadToAll(source.getServer());
                        source.sendFeedback(() -> Text.translatable("variantswap.command.cooldown_set", newDelay), false);
                        return 1;
                    })
                )
            )
        );
    }

    private static void sendDelayPayloadToAll(MinecraftServer server) {
        VariantDelayPayload payload = new VariantDelayPayload(VariantSwapConfig.getDelay());

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}