package com.dooji.variantswap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class VariantSwapConfig {
    private static final File configFile = new File("config/Variant Swap/config.json");
    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<Map<String, Integer>>(){}.getType();
    private static Map<String, Integer> configMap = new HashMap<>();

    public static void loadConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                configMap = gson.fromJson(reader, type);
            } catch (Exception e) {
                e.printStackTrace();
                setDefaults();
            }
        } else {
            setDefaults();
            saveConfig();
        }
    }

    private static void setDefaults() {
        configMap.put("delay", 50);
        configMap.put("opLevel", 4);
    }

    public static int getDelay() {
        return configMap.getOrDefault("delay", 50);
    }

    public static int getOpLevel() {
        return configMap.getOrDefault("opLevel", 4);
    }

    public static void setDelay(int delay) {
        configMap.put("delay", delay);
        saveConfig();
    }
    
    private static void saveConfig() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(configMap, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}