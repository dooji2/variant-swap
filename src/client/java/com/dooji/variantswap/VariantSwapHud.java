package com.dooji.variantswap;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class VariantSwapHud implements HudRenderCallback {
    private static List<Identifier> currentGroup = null;

    private static int selectedIndex = 0;
    private static Identifier selectedVariant = null;
    private static long displayEndTime = 0;

    private static final long DISPLAY_DURATION_MS = 5000;
    private static final int VISIBLE_COUNT = 7;
    private static final int BASE_ICON_SIZE = 16;
    private static final int SELECTED_ICON_SIZE = 24;
    private static final int SPACING = 8;

    private static float currentY = -(SELECTED_ICON_SIZE + 4);
    private static float finalY = 20f;

    public static void onScroll(int slot, boolean forward) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack heldStack = client.player.getInventory().getStack(slot);
        if (heldStack.isEmpty()) return;

        Identifier currentId = Registries.ITEM.getId(heldStack.getItem());
        List<Identifier> group = VariantSwapClient.variantMapping.getGroup(currentId);

        if (group == null || group.size() < 2) return;

        if (client.player.isCreative()) {
            int currentIndex = group.indexOf(currentId);

            if (currentIndex == -1) currentIndex = 0;

            int newIndex = forward ? (currentIndex + 1) % group.size() : (currentIndex - 1 + group.size()) % group.size();
            currentGroup = group;
            selectedIndex = newIndex;
            selectedVariant = group.get(newIndex);
        } else {
            List<Identifier> availableVariants = new ArrayList<>();

            for (Identifier variant : group) {
                if (client.player.getInventory().count(Registries.ITEM.get(variant)) > 0) availableVariants.add(variant);
            }

            if (availableVariants.size() <= 1) return;

            Identifier heldVariant = currentId;
            int currentAvailIndex = availableVariants.indexOf(heldVariant);

            if (currentAvailIndex == -1) currentAvailIndex = 0;

            int newAvailIndex = forward ? (currentAvailIndex + 1) % availableVariants.size() : (currentAvailIndex - 1 + availableVariants.size()) % availableVariants.size();
            selectedVariant = availableVariants.get(newAvailIndex);
            selectedIndex = group.indexOf(selectedVariant);
            currentGroup = group;
        }

        displayEndTime = System.currentTimeMillis() + DISPLAY_DURATION_MS;

        if (currentGroup != null && currentY < -(SELECTED_ICON_SIZE + 4) + 1) {
            currentY = -(SELECTED_ICON_SIZE + 4);
        }
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickDelta) {
        long currentTime = System.currentTimeMillis();

        if (currentGroup == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();

        if (currentTime <= displayEndTime) {
            finalY = isWthitPresent() ? 40f : 20f;
        } else {
            finalY = -(SELECTED_ICON_SIZE + 4);
        }
        
        currentY += (finalY - currentY) * 0.1f;
        int y = Math.round(currentY);

        // int totalGradientHeight = 64;
        // int midY = totalGradientHeight / 2;
        // context.fillGradient(0, 0, screenWidth, midY, 0xCC000000, 0xCC000000);
        // context.fillGradient(0, midY, screenWidth, totalGradientHeight, 0xCC000000, 0x000000);        

        List<Identifier> listToRender = currentGroup;
        int selectedAvailIndex = selectedIndex;

        if (!client.player.isCreative()) {
            List<Identifier> availableVariants = new ArrayList<>();

            for (Identifier variant : currentGroup) {
                if (client.player.getInventory().count(Registries.ITEM.get(variant)) > 0) {
                    availableVariants.add(variant);
                }
            }

            if (availableVariants.size() <= 1) return;

            listToRender = availableVariants;
            int indexInAvailable = availableVariants.indexOf(selectedVariant);
            selectedAvailIndex = indexInAvailable == -1 ? 0 : indexInAvailable;
        }

        int centerX = screenWidth / 2;
        int centerIndex = VISIBLE_COUNT / 2;

        for (int i = 0; i < VISIBLE_COUNT; i++) {
            int offset = i - centerIndex;
            int variantIndex = Math.floorMod(selectedAvailIndex + offset, listToRender.size());
            int iconSize = offset == 0 ? SELECTED_ICON_SIZE : BASE_ICON_SIZE;

            float itemAlpha = 1.0f;
            int x = centerX + offset * (BASE_ICON_SIZE + SPACING) - (iconSize / 2);
            int adjustedY = offset == 0 ? y : y + ((SELECTED_ICON_SIZE - BASE_ICON_SIZE) / 2);

            if (offset == 0) {
                int bgX = x - 2;
                int bgY = y - 2;
                int bgSize = iconSize + 4;

                context.fill(bgX, bgY, bgX + bgSize, bgY + bgSize, 0x88000000);

                int thickness = 2;

                context.fill(bgX, bgY, bgX + bgSize, bgY + thickness, 0xFFFFFFFF);
                context.fill(bgX, bgY, bgX + thickness, bgY + bgSize, 0xFFFFFFFF);
                context.fill(bgX, bgY + bgSize - thickness, bgX + bgSize, bgY + bgSize, 0xFFFFFFFF);
                context.fill(bgX + bgSize - thickness, bgY, bgX + bgSize, bgY + bgSize, 0xFFFFFFFF);
            }

            Identifier id = listToRender.get(variantIndex);
            Item item = Registries.ITEM.get(id);
            ItemStack stack = new ItemStack(item);

            float scale = iconSize / 16.0f;

            context.getMatrices().push();
            context.getMatrices().translate(x, adjustedY, 0);
            context.getMatrices().scale(scale, scale, scale);

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, itemAlpha);
            context.drawItem(stack, 0, 0, 0);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            context.getMatrices().pop();
        }

        if (currentTime > displayEndTime && currentY <= -(SELECTED_ICON_SIZE + 4) + 1) {
            currentGroup = null;
        }
    }

    private static boolean isWthitPresent() {
        return FabricLoader.getInstance().isModLoaded("wthit");
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new VariantSwapHud());
    }
}