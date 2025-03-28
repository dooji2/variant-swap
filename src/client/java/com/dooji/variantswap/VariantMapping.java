package com.dooji.variantswap;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.util.registry.Registry;

public class VariantMapping {
    private static final File configFile = new File("config/Variant Swap/mappings.json");
    private static final Gson gson = new Gson();
    private static final Object fileLock = new Object();

    private static final Set<String> ignoreAdjectives = new HashSet<>(Arrays.asList("oak","spruce","birch","jungle","acacia","cherry","dark","mangrove","smooth","polished","cracked","mossy","chiseled"));

    public VariantMapping() {}

    public void setup() {
        Map<String, List<String>> groups = generateMappings();
        saveMapping(groups);

        VariantSwapClient.LOGGER.info("[Variant Swap] Finished generating variant mappings. Total groups: {}", groups.size());
    }

    private Map<String, List<String>> generateMappings() {
        Set<Identifier> allItems = Registry.ITEM.getIds();
        Map<String, List<String>> groups = new HashMap<>();

        for (Identifier id : allItems) {
            String candidate = null;

            Item item = Registry.ITEM.get(id);
            ItemStack stack = new ItemStack(item);

            Map<Identifier, Tag<Item>> allItemTags = ItemTags.getTagGroup().getTags();
            for (Map.Entry<Identifier, Tag<Item>> entry : allItemTags.entrySet()) {
                Identifier tagId = entry.getKey();
                if ("variant_swap".equals(tagId.getNamespace()) && stack.isIn(entry.getValue())) {
                    candidate = tagId.getPath();
                    break;
                }
            }

            if (candidate == null) {
                String path = id.getPath();
                String[] tokens = path.split("_");

                if (tokens.length > 0) {
                    for (int i = tokens.length - 1; i >= 0; i--) {
                        if (!ignoreAdjectives.contains(tokens[i])) {
                            candidate = tokens[i];
                            break;
                        }
                    }
                }
            }

            if (candidate != null) {
                groups.computeIfAbsent(candidate, k -> new ArrayList<>()).add(id.toString());
            }
        }

        Iterator<Map.Entry<String, List<String>>> it = groups.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            if (entry.getValue().size() < 2) {
                it.remove();
            } else {
                Collections.sort(entry.getValue());
            }
        }

        return groups;
    }

    private void saveMapping(Map<String, List<String>> mapping) {
        synchronized(fileLock) {
            try {
                configFile.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(configFile)) {
                    gson.toJson(mapping, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCandidate(Identifier id) {
        String candidate = null;
        Item item = Registry.ITEM.get(id);
        ItemStack stack = new ItemStack(item);

        Map<Identifier, Tag<Item>> allItemTags = ItemTags.getTagGroup().getTags();
        for (Map.Entry<Identifier, Tag<Item>> entry : allItemTags.entrySet()) {
            Identifier tagId = entry.getKey();
            if ("variant_swap".equals(tagId.getNamespace()) && stack.isIn(entry.getValue())) {
                candidate = tagId.getPath();
                break;
            }
        }

        if (candidate == null && item instanceof BlockItem blockItem) {
            Map<Identifier, Tag<Block>> allBlockTags = BlockTags.getTagGroup().getTags();
            for (Map.Entry<Identifier, Tag<Block>> entry : allBlockTags.entrySet()) {
                Identifier tagId = entry.getKey();
                if ("variant_swap".equals(tagId.getNamespace()) &&
                        blockItem.getBlock().getDefaultState().isIn(entry.getValue())) {
                    candidate = tagId.getPath();
                    break;
                }
            }
        }

        if (candidate == null) {
            String path = id.getPath();
            String[] tokens = path.split("_");

            for (int i = tokens.length - 1; i >= 0; i--) {
                if (!ignoreAdjectives.contains(tokens[i])) {
                    candidate = tokens[i];
                    break;
                }
            }
        }

        return candidate;
    }

    public Identifier getNextVariant(Identifier current, boolean forward) {
        String candidate = getCandidate(current);

        if (candidate == null) {
            return null;
        }

        List<Identifier> group = loadGroup(candidate);
        if (group == null || group.isEmpty()) {
            return null;
        }

        int index = group.indexOf(current);
        if (index == -1) {
            return null;
        }

        int nextIndex = forward ? (index + 1) % group.size() : (index - 1 + group.size()) % group.size();
        return group.get(nextIndex);
    }

    public List<Identifier> getGroup(Identifier current) {
        String candidate = getCandidate(current);
        if (candidate == null) {
            return null;
        }

        return loadGroup(candidate);
    }

    private List<Identifier> loadGroup(String candidate) {
        synchronized(fileLock) {
            if (!configFile.exists()) {
                return null;
            }

            List<Identifier> group = new ArrayList<>();
            try (JsonReader reader = new JsonReader(new FileReader(configFile))) {
                reader.beginObject();

                while (reader.hasNext()) {
                    String key = reader.nextName();

                    if (key.equals(candidate)) {
                        reader.beginArray();

                        while (reader.hasNext()) {
                            group.add(new Identifier(reader.nextString()));
                        }

                        reader.endArray();
                    } else {
                        reader.skipValue();
                    }
                }

                reader.endObject();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return group;
        }
    }
}