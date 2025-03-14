package com.dooji.variantswap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public class VariantSwapInputHandler implements GLFWScrollCallbackI {
    private static double scrollDelta = 0;
    private static boolean registered = false;

    private static GLFWScrollCallbackI previousCallback = null;
    private static KeyBinding variantSwapKey;

    public static boolean isRegistered() {
        return registered;
    }

    public static void setVariantSwapKey(KeyBinding key) {
        variantSwapKey = key;
    }

    public static void register() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.getWindow() != null) {
            long window = client.getWindow().getHandle();
            previousCallback = GLFW.glfwSetScrollCallback(window, new VariantSwapInputHandler());
            registered = true;
        }
    }

    @Override
    public void invoke(long window, double xoffset, double yoffset) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null) {
            if (previousCallback != null) {
                previousCallback.invoke(window, xoffset, yoffset);
            }
            return;
        }

        if (variantSwapKey != null && variantSwapKey.isPressed()) {
            scrollDelta += yoffset;
        } else {
            if (previousCallback != null) {
                previousCallback.invoke(window, xoffset, yoffset);
            }
        }
    }

    public static double getScrollDelta() {
        return scrollDelta;
    }

    public static void decrementScrollDelta(double amount) {
        if (scrollDelta > 0) {
            scrollDelta = Math.max(0, scrollDelta - amount);
        } else if (scrollDelta < 0) {
            scrollDelta = Math.min(0, scrollDelta + amount);
        }
    }

    public static void resetScrollDelta() {
        scrollDelta = 0;
    }
}