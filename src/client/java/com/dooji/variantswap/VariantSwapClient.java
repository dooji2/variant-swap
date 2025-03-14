package com.dooji.variantswap;

import com.dooji.variantswap.network.VariantSwapClientNetworking;
import com.dooji.variantswap.network.payloads.VariantSwapRequestPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

public class VariantSwapClient implements ClientModInitializer {
    private static KeyBinding variantSwapKey;
    public static VariantMapping variantMapping;

    public static long lastSwapTime = 0;
    public static int swapCooldown = 100;

    @Override
    public void onInitializeClient() {
        variantSwapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.variantswap.swap",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_SHIFT,
                "category.variantswap"
        ));

        VariantSwapInputHandler.setVariantSwapKey(variantSwapKey);
        variantMapping = new VariantMapping();
        VariantSwapHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!VariantSwapInputHandler.isRegistered() && client.getWindow() != null) {
                VariantSwapInputHandler.register();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (variantSwapKey.isPressed() && client.player != null) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= lastSwapTime + swapCooldown) {
                    double delta = VariantSwapInputHandler.getScrollDelta();

                    if (Math.abs(delta) >= 1.0) {
                        boolean forward = delta > 0;
                        int slot = client.player.getInventory().selectedSlot;

                        VariantSwapHud.onScroll(slot, forward);
                        VariantSwapRequestPayload payload = new VariantSwapRequestPayload(slot, forward);

                        ClientPlayNetworking.send(payload);

                        lastSwapTime = currentTime;
                        VariantSwapInputHandler.decrementScrollDelta(2.0);
                    }
                }
            } else {
                VariantSwapInputHandler.resetScrollDelta();
            }
        });

        VariantSwapClientNetworking.init();
    }

    public static Identifier getNextVariant(Identifier current, boolean forward) {
        List<Identifier> variants = variantMapping.getMapping().get(current);

        if (variants == null) {
            for (List<Identifier> group : variantMapping.getMapping().values()) {
                if (group.contains(current)) {
                    variants = group;
                    break;
                }
            }
        }

        if (variants != null) {
            int index = variants.indexOf(current);

            if (index != -1) {
                int nextIndex = forward ? (index + 1) % variants.size() : (index - 1 + variants.size()) % variants.size();
                return variants.get(nextIndex);
            }
        }

        return null;
    }

    public static void updateMapping(Map<String, List<String>> newMapping) {
        variantMapping.updateMappingFromString(newMapping);
    }
}