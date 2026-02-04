package com.crystalguard;

import com.crystalguard.gui.MenuBuilder;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CrystalGuardCommand implements CommandExecutor, TabCompleter {
    private final CrystalGuardPlugin plugin;

    public CrystalGuardCommand(CrystalGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cКоманда доступна лише гравцям.");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("crystalguard.admin")) {
                player.sendMessage("§cНемає прав для адмін-меню.");
                return true;
            }
            player.openInventory(MenuBuilder.createAdminArenaList(plugin));
            return true;
        }
        player.openInventory(MenuBuilder.createPlayerMain(plugin, player));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("admin");
        }
        return options;
    }
}
