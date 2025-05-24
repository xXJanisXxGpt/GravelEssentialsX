package com.xXJanisXx.gravelEssentialsX.managers;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager {

    private final GravelEssentialsX plugin;
    private final Map<String, List<ItemStack>> kits;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public KitManager(GravelEssentialsX plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        loadKits();
    }

    public void loadKits() {
        kits.clear();
        ConfigurationSection kitsSection = plugin.getConfig().getConfigurationSection("kits");

        if (kitsSection == null) {
            plugin.getLogger().warning("No Kits found!");
            return;
        }

        for (String kitName : kitsSection.getKeys(false)) {
            List<ItemStack> kitItems = new ArrayList<>();
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);

            if (kitSection == null) continue;

            ConfigurationSection itemsSection = kitSection.getConfigurationSection("items");
            if (itemsSection == null) continue;

            for (String itemKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
                if (itemSection == null) continue;

                String materialName = itemSection.getString("material");
                int amount = itemSection.getInt("amount", 1);
                String name = itemSection.getString("name");
                List<String> lore = itemSection.getStringList("lore");

                try {
                    assert materialName != null;
                    Material material = Material.valueOf(materialName.toUpperCase());
                    ItemStack item = new ItemStack(material, amount);
                    ItemMeta meta = item.getItemMeta();

                    if (name != null && !name.isEmpty()) {
                        if (name.contains("§")) {
                            meta.displayName(Component.text(name));
                        } else {
                            meta.displayName(miniMessage.deserialize(name));
                        }
                    }

                    if (!lore.isEmpty()) {
                        List<Component> loreComponents = new ArrayList<>();
                        for (String line : lore) {
                            if (line.contains("§")) {
                                loreComponents.add(Component.text(line));
                            } else {
                                loreComponents.add(miniMessage.deserialize(line));
                            }
                        }
                        meta.lore(loreComponents);
                    }

                    item.setItemMeta(meta);
                    kitItems.add(item);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Ungültiges Item für Kit " + kitName + ": " + materialName);
                }
            }

            kits.put(kitName, kitItems);
            plugin.getLogger().info("Kit '" + kitName + "' mit " + kitItems.size() + " Items geladen!");
        }
    }

    public boolean giveKit(Player player, String kitName) {
        List<ItemStack> items = kits.get(kitName.toLowerCase());

        if (items == null || items.isEmpty()) {
            return false;
        }

        for (ItemStack item : items) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());

            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        return true;
    }

    public List<String> getKitNames() {
        return new ArrayList<>(kits.keySet());
    }
}