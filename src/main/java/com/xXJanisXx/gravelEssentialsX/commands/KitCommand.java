package com.xXJanisXx.gravelEssentialsX.commands;

import com.xXJanisXx.gravelEssentialsX.GravelEssentialsX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KitCommand implements CommandExecutor {

    public KitCommand(GravelEssentialsX gravelEssentialsX) {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            
            ItemStack diamond = new ItemStack(Material.DIAMOND);
            ItemStack bricks = new ItemStack(Material.BRICK);
            
            bricks.setAmount(20);
            
            player.getInventory().addItem(bricks, diamond);
        }
        
        return true;

        sender.sendMessage(
                plugin.getPrefix().append(
                        Component.text("Verwendung: /ge reload", NamedTextColor.YELLOW)
    }
}  
  
