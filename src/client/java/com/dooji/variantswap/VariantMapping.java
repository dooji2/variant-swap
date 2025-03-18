package com.dooji.variantswap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.*;

public class VariantMapping {
    private final Map<Identifier, List<Identifier>> mapping = new HashMap<>();
    private static final Set<String> ignoreAdjectives = new HashSet<>(Arrays.asList("oak","spruce","birch","jungle","acacia","cherry","dark","mangrove","smooth","polished","cracked","mossy","chiseled"));
    
    public VariantMapping() {
        generateMappings();
        VariantSwapClient.LOGGER.info("[Variant Swap] Finished generating variant mappings. Total groups: {}", mapping.size());
    }

    private void generateMappings() {
        Set<Identifier> allItems = Registries.ITEM.getIds();
        Map<String, List<Identifier>> groups = new HashMap<>();

        for (Identifier id : allItems) {
            String candidate = null;

            Item item = Registries.ITEM.get(id);
            ItemStack stack = new ItemStack(item);
            
            for (TagKey<Item> tag : stack.streamTags().toList()) {
                Identifier tagId = tag.id();
                if ("variant_swap".equals(tagId.getNamespace())) {
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