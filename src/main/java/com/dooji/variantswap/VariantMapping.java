package com.dooji.variantswap;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class VariantMapping {
    private final Map<Identifier, List<Identifier>> mapping = new HashMap<>();
    private static final File configFile = new File("config/Variant Swap/mappings.json");
    private static final Set<String> ignoreAdjectives = new HashSet<>(Arrays.asList("oak","spruce","birch","jungle","acacia","cherry","dark","mangrove","smooth","polished","cracked","mossy","chiseled"));
    
    public VariantMapping() {
        if (configFile.exists()) {
            loadMapping();
        }

        generateMappings();
        saveMapping();
    }

    private void generateMappings() {
        Set<Identifier> allItems = Registries.ITEM.getIds();
        Map<String, List<Identifier>> groups = new HashMap<>();

        for (Identifier id : allItems) {
            String path = id.getPath();
            String[] tokens = path.split("_");

            if (tokens.length == 0) continue;

            String candidate = null;
            for (int i = tokens.length - 1; i >= 0; i--) {
                if (!ignoreAdjectives.contains(tokens[i])) {
                    candidate = tokens[i];
                    break;
                }
            }

            if (candidate != null) {
                groups.computeIfAbsent(candidate, k -> new ArrayList<>()).add(id);
            }
        }

        for (List<Identifier> group : groups.values()) {
            if (group.size() >= 2) {
                group.sort(Comparator.comparing(Identifier::toString));

                for (Identifier id : group) {
                    if (!mapping.containsKey(id)) {
                        mapping.put(id, new ArrayList<>(group));
                    }
                }
            }
        }
    }

    private void loadMapping() {
        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            Map<String, List<String>> loaded = gson.fromJson(reader, type);

            for (Map.Entry<String, List<String>> entry : loaded.entrySet()) {
                Identifier key = Identifier.of(entry.getKey());
                List<Identifier> values = new ArrayList<>();

                for (String s : entry.getValue()) {
                    values.add(Identifier.of(s));
                }

                mapping.put(key, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveMapping() {
        try {
            configFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(configFile)) {
                Gson gson = new Gson();
                Map<String, List<String>> out = new HashMap<>();

                for (Map.Entry<Identifier, List<Identifier>> entry : mapping.entrySet()) {
                    List<String> list = new ArrayList<>();

                    for (Identifier id : entry.getValue()) {
                        list.add(id.toString());
                    }

                    out.put(entry.getKey().toString(), list);
                }

                gson.toJson(out, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Identifier getNextVariant(Identifier current, boolean forward) {
        List<Identifier> variants = mapping.get(current);

        if (variants == null) {
            for (List<Identifier> group : mapping.values()) {
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

    public Map<Identifier, List<Identifier>> getMapping() {
        return mapping;
    }

    public Map<String, List<String>> getMappingAsString() {
        Map<String, List<String>> map = new HashMap<>();

        for (Map.Entry<Identifier, List<Identifier>> entry : mapping.entrySet()) {
            List<String> list = new ArrayList<>();

            for (Identifier id : entry.getValue()) {
                list.add(id.toString());
            }

            map.put(entry.getKey().toString(), list);
        }

        return map;
    }

    public void updateMappingFromString(Map<String, List<String>> newMapping) {
        mapping.clear();

        for (Map.Entry<String, List<String>> entry : newMapping.entrySet()) {
            Identifier key = Identifier.of(entry.getKey());
            List<Identifier> list = new ArrayList<>();

            for (String s : entry.getValue()) {
                list.add(Identifier.of(s));
            }

            mapping.put(key, list);
        }
    }
}