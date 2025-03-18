package com.dooji.variantswap;

import com.dooji.variantswap.network.VariantSwapNetworking;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantSwap implements ModInitializer {
    public static final String MOD_ID = "variant-swap";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Variant Swap] Initializing!");

        VariantSwapNetworking.init();
		VariantSwapConfig.loadConfig();
		VariantSwapCommand.register();
    }
}
