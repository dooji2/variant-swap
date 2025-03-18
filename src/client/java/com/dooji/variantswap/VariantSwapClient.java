package com.dooji.variantswap;

import com.dooji.variantswap.network.VariantSwapClientNetworking;
import com.dooji.variantswap.network.payloads.VariantSwapRequestPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VariantSwapClient implements ClientModInitializer {
    public static final String MOD_ID = "variant-swap";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding variantSwapKey;
    public static VariantMapping variantMapping;

    public static long lastSwapTime = 0;
    public static int swapCooldown = 100;

    @Override
    public void onInitializeClient() {
        variantSwapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.variantswap.swap",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
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
                        
                        if (client.player.getInventory().getStack(slot).isEmpty()) {
                            return;
                        }

                        Identifier currentId = Registries.ITEM.getId(client.player.getInventory().getStack(slot).getItem());
                        List<Identifier> group = variantMapping.getMapping().get(currentId);

                        if (group == null) {
                            for (List<Identifier> maybeGroup : variantMapping.getMapping().values()) {
                                if (maybeGroup.contains(currentId)) {
                                    group = maybeGroup;
                                    break;
                                }
                            }
                        }

                        if (group == null || group.size() < 2) {
                            return;
                        }
                        
                        int currentIndex = group.indexOf(currentId);
                        if (currentIndex == -1) return;

                        int originalIndex = currentIndex;
                        Identifier targetCandidate = null;
                        for (int i = 1; i < group.size(); i++) {
                            currentIndex = (currentIndex + (forward ? 1 : -1) + group.size()) % group.size();

                            Identifier candidateId = group.get(currentIndex);
                            if (client.player.isCreative()) {
                                targetCandidate = candidateId;
                                break;
                            } else {
                                int bestSlot = -1;
                                int bestCount = 0;
                                for (int invSlot = 0; invSlot < client.player.getInventory().size(); invSlot++) {
                                    if (invSlot == slot) continue;

                                    if (!client.player.getInventory().getStack(invSlot).isEmpty()) {
                                        Identifier stackId = Registries.ITEM.getId(client.player.getInventory().getStack(invSlot).getItem());

                                        if (stackId.equals(candidateId)) {
                                            int count = client.player.getInventory().getStack(invSlot).getCount();

                                            if (count > bestCount) {
                                                bestCount = count;
                                                bestSlot = invSlot;
                                            }
                                        }
                                    }
                                }

                                if (bestSlot != -1) {
                                    targetCandidate = candidateId;
                                    break;
                                }
                            }

                            if (currentIndex == originalIndex) {
                                break;
                            }
                        }
                        
                        if (targetCandidate != null) {
                            VariantSwapHud.onScroll(slot, forward);
                            
                            VariantSwapRequestPayload payload = new VariantSwapRequestPayload(slot, targetCandidate.toString());
                            ClientPlayNetworking.send(payload);
                        }
                        
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
}