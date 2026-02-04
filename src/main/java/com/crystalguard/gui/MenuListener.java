package com.crystalguard.gui;

import com.crystalguard.CrystalGuardPlugin;
import com.crystalguard.arena.Arena;
import com.crystalguard.arena.ArenaMatch;
import com.crystalguard.arena.MobConfig;
import com.crystalguard.arena.SpawnerConfig;
import com.crystalguard.player.PlayerProfile;
import com.crystalguard.player.PlayerRole;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuListener implements Listener {
    private final CrystalGuardPlugin plugin;

    public MenuListener(CrystalGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }
        switch (holder.getType()) {
            case PLAYER_MAIN -> handlePlayerMain(player, current);
            case ARENA_SELECT -> handleArenaSelect(player, current);
            case POSITION_SELECT -> handlePositionSelect(player, current);
            case CLASS_SELECT -> handleClassSelect(player, current);
            case ADMIN_ARENA_LIST -> handleAdminArenaList(player, current);
            case ADMIN_ARENA_EDITOR -> handleAdminArenaEditor(player, current, holder.getArenaName());
            case ADMIN_SPAWNER_MENU -> handleSpawnerMenu(player, current, holder.getArenaName(), event);
            case ADMIN_MOB_EDITOR -> handleMobEditor(player, event, holder.getArenaName(), holder.getSpawnerIndex());
            case ADMIN_MOB_TYPE_SELECT -> handleMobTypeSelect(player, current, holder.getArenaName(), holder.getSpawnerIndex());
            default -> {
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        if (holder.getType() == MenuType.ADMIN_MOB_EDITOR) {
            Player player = (Player) event.getPlayer();
            saveEquipment(player, holder.getArenaName(), holder.getSpawnerIndex(), event.getInventory());
        }
    }

    private void handlePlayerMain(Player player, ItemStack current) {
        PlayerProfile profile = plugin.getPlayerManager().getProfile(player);
        Material type = current.getType();
        if (type == Material.EMERALD) {
            player.openInventory(MenuBuilder.createArenaSelect(plugin));
        } else if (type == Material.ARMOR_STAND) {
            player.openInventory(MenuBuilder.createPositionSelect(plugin, player));
        } else if (type == Material.NETHER_STAR) {
            player.openInventory(MenuBuilder.createClassSelect());
        } else if (type == Material.DIAMOND_BLOCK) {
            if (profile.getSelectedArena() == null || profile.getRole() == null || profile.getPositionIndex() <= 0) {
                player.sendMessage("§cОберіть арену, клас і позицію.");
                return;
            }
            Arena arena = profile.getSelectedArena();
            ArenaMatch match = plugin.getArenaManager().getOrCreateMatch(arena);
            match.addParticipant(player);
            if (!arena.isRunning()) {
                match.startMatch();
            }
            Location warp = arena.getPlayerWarps().get(profile.getPositionIndex() - 1);
            if (warp != null) {
                player.teleport(warp);
            }
            player.sendMessage("§aВи приєдналися до матчу!");
        }
    }

    private void handleArenaSelect(Player player, ItemStack current) {
        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        Optional<Arena> arena = plugin.getArenaManager().getArena(name);
        if (arena.isEmpty()) {
            player.sendMessage("§cАрена не знайдена.");
            return;
        }
        PlayerProfile profile = plugin.getPlayerManager().getProfile(player);
        profile.setSelectedArena(arena.get());
        player.sendMessage("§aОбрано арену: " + arena.get().getName());
        player.openInventory(MenuBuilder.createPlayerMain(plugin, player));
    }

    private void handlePositionSelect(Player player, ItemStack current) {
        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        if (!name.startsWith("Позиція")) {
            return;
        }
        int position = Integer.parseInt(name.split(" ")[1]);
        PlayerProfile profile = plugin.getPlayerManager().getProfile(player);
        profile.setPositionIndex(position);
        player.sendMessage("§aОбрана позиція: " + position);
        player.openInventory(MenuBuilder.createPlayerMain(plugin, player));
    }

    private void handleClassSelect(Player player, ItemStack current) {
        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        for (PlayerRole role : PlayerRole.values()) {
            if (role.getDisplayName().equalsIgnoreCase(name)) {
                if (!canSelectRole(player, role)) {
                    player.sendMessage("§cЦей клас уже зайнятий двома гравцями.");
                    return;
                }
                plugin.getPlayerManager().getProfile(player).setRole(role);
                player.sendMessage("§aВи обрали клас: " + role.getDisplayName());
                player.openInventory(MenuBuilder.createPlayerMain(plugin, player));
                return;
            }
        }
    }

    private boolean canSelectRole(Player player, PlayerRole role) {
        int count = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) {
                continue;
            }
            PlayerProfile profile = plugin.getPlayerManager().getProfile(online);
            if (role.equals(profile.getRole())) {
                count++;
            }
        }
        return count < 2;
    }

    private void handleAdminArenaList(Player player, ItemStack current) {
        if (current.getType() == Material.ANVIL) {
            String name = "Arena" + (plugin.getArenaManager().getArenas().size() + 1);
            Arena arena = plugin.getArenaManager().createArena(name);
            player.sendMessage("§aСтворено арену: " + name);
            player.openInventory(MenuBuilder.createAdminArenaEditor(arena));
            return;
        }
        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        plugin.getArenaManager().getArena(name).ifPresent(arena -> player.openInventory(MenuBuilder.createAdminArenaEditor(arena)));
    }

    private void handleAdminArenaEditor(Player player, ItemStack current, String arenaName) {
        Optional<Arena> optional = plugin.getArenaManager().getArena(arenaName);
        if (optional.isEmpty()) {
            player.sendMessage("§cАрена не знайдена.");
            return;
        }
        Arena arena = optional.get();
        Material type = current.getType();
        if (type == Material.LODESTONE) {
            int index = current.getItemMeta().getDisplayName().contains("1") ? 0 :
                    current.getItemMeta().getDisplayName().contains("2") ? 1 :
                            current.getItemMeta().getDisplayName().contains("3") ? 2 :
                                    current.getItemMeta().getDisplayName().contains("4") ? 3 : 4;
            arena.getPlayerWarps().set(index, player.getLocation());
            player.sendMessage("§aWarp позиції " + (index + 1) + " встановлено.");
        } else if (type == Material.SKELETON_SKULL) {
            arena.setDeadWarp(player.getLocation());
            player.sendMessage("§awarp_dead встановлено.");
        } else if (type == Material.END_CRYSTAL) {
            arena.setCrystalLocation(player.getLocation());
            player.sendMessage("§aКристал встановлено.");
        } else if (type == Material.ENDER_PEARL) {
            arena.getSpawnPoints().add(player.getLocation());
            player.sendMessage("§aТочку спавну додано. Всього: " + arena.getSpawnPoints().size());
        } else if (type == Material.BARRIER) {
            arena.getSpawnPoints().clear();
            player.sendMessage("§cТочки спавну очищено.");
        } else if (type == Material.SPAWNER) {
            player.openInventory(MenuBuilder.createSpawnerMenu(arena));
            return;
        } else if (type == Material.LIME_DYE) {
            ArenaMatch match = plugin.getArenaManager().getOrCreateMatch(arena);
            if (arena.isRunning()) {
                match.stopMatch(false);
                player.sendMessage("§cМатч зупинено.");
            } else {
                match.startMatch();
                player.sendMessage("§aМатч запущено.");
            }
        } else if (type == Material.EMERALD_BLOCK) {
            plugin.getArenaManager().saveArenas();
            player.sendMessage("§aАрена збережена.");
        } else if (type == Material.REDSTONE_BLOCK) {
            plugin.getArenaManager().deleteArena(arena);
            player.sendMessage("§cАрена видалена.");
            player.openInventory(MenuBuilder.createAdminArenaList(plugin));
            return;
        }
        player.openInventory(MenuBuilder.createAdminArenaEditor(arena));
    }

    private void handleSpawnerMenu(Player player, ItemStack current, String arenaName, InventoryClickEvent event) {
        Optional<Arena> optional = plugin.getArenaManager().getArena(arenaName);
        if (optional.isEmpty()) {
            return;
        }
        Arena arena = optional.get();
        int slot = event.getSlot();
        if (slot < 0 || slot >= arena.getSpawners().size()) {
            return;
        }
        SpawnerConfig spawner = arena.getSpawners().get(slot);
        if (event.getClick() == org.bukkit.event.inventory.ClickType.SWAP_OFFHAND) {
            spawner.setEnabled(!spawner.isEnabled());
            player.sendMessage("§aСтатус спавнера: " + (spawner.isEnabled() ? "увімкнено" : "вимкнено"));
        } else if (event.getClick() == ClickType.MIDDLE) {
            player.openInventory(MenuBuilder.createMobEditor(arena, slot));
            return;
        } else if (player.isSneaking() && event.isLeftClick()) {
            spawner.setSpawnPointIndex(Math.max(1, spawner.getSpawnPointIndex() - 1));
        } else if (player.isSneaking() && event.isRightClick()) {
            int max = Math.max(1, arena.getSpawnPoints().size());
            spawner.setSpawnPointIndex(Math.min(max, spawner.getSpawnPointIndex() + 1));
        } else if (event.isLeftClick() && !event.isShiftClick()) {
            spawner.setStartWave(Math.max(1, spawner.getStartWave() - 1));
        } else if (event.isRightClick() && !event.isShiftClick()) {
            spawner.setStartWave(Math.min(5, spawner.getStartWave() + 1));
        } else if (event.isShiftClick() && event.isLeftClick()) {
            spawner.setEndWave(Math.max(1, spawner.getEndWave() - 1));
        } else if (event.isShiftClick() && event.isRightClick()) {
            spawner.setEndWave(Math.min(5, spawner.getEndWave() + 1));
        } else if (event.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
            spawner.setMobsPerWave(Math.min(50, spawner.getMobsPerWave() + 1));
        } else if (event.getClick() == org.bukkit.event.inventory.ClickType.DROP) {
            spawner.setMobsPerWave(Math.max(1, spawner.getMobsPerWave() - 1));
        } else if (event.getClick() == org.bukkit.event.inventory.ClickType.CONTROL_DROP) {
            spawner.setSpawnPeriodSeconds(Math.min(60, spawner.getSpawnPeriodSeconds() + 1));
        } else if (event.getClick() == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK) {
            spawner.setSpawnPeriodSeconds(Math.max(1, spawner.getSpawnPeriodSeconds() - 1));
        } else {
            player.openInventory(MenuBuilder.createMobEditor(arena, slot));
            return;
        }
        player.openInventory(MenuBuilder.createSpawnerMenu(arena));
    }

    private void handleMobEditor(Player player, InventoryClickEvent event, String arenaName, int spawnerIndex) {
        Optional<Arena> optional = plugin.getArenaManager().getArena(arenaName);
        if (optional.isEmpty()) {
            return;
        }
        Arena arena = optional.get();
        SpawnerConfig spawner = arena.getSpawners().get(spawnerIndex);
        MobConfig mobConfig = spawner.getMobConfig();
        int slot = event.getSlot();
        if (slot == 4) {
            player.openInventory(MenuBuilder.createMobTypeSelect(arena, spawnerIndex));
            return;
        }
        if (slot == 13) {
            mobConfig.setHpMultiplier(adjustValue(mobConfig.getHpMultiplier(), event.isRightClick(), 0.1, 0.5, 5.0));
        } else if (slot == 14) {
            mobConfig.setDamageMultiplier(adjustValue(mobConfig.getDamageMultiplier(), event.isRightClick(), 0.1, 0.5, 5.0));
        } else if (slot == 15) {
            mobConfig.setSpeedMultiplier(adjustValue(mobConfig.getSpeedMultiplier(), event.isRightClick(), 0.1, 0.5, 3.0));
        } else if (slot == 16) {
            mobConfig.setEliteChance(adjustValue(mobConfig.getEliteChance(), event.isRightClick(), 0.05, 0.0, 0.5));
        } else if (slot == 31) {
            mobConfig.setAggroPlayers(!mobConfig.isAggroPlayers());
        } else if (slot == 32) {
            mobConfig.setPreferCrystalWhenIdle(!mobConfig.isPreferCrystalWhenIdle());
        } else if (slot == 53) {
            MobRandomizer.randomizeMob(mobConfig);
        } else if (isEquipmentSlot(slot)) {
            event.setCancelled(false);
            return;
        }
        player.openInventory(MenuBuilder.createMobEditor(arena, spawnerIndex));
    }

    private void handleMobTypeSelect(Player player, ItemStack current, String arenaName, int spawnerIndex) {
        Optional<Arena> optional = plugin.getArenaManager().getArena(arenaName);
        if (optional.isEmpty()) {
            return;
        }
        Arena arena = optional.get();
        SpawnerConfig spawner = arena.getSpawners().get(spawnerIndex);
        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        try {
            spawner.getMobConfig().setType(EntityType.valueOf(name));
        } catch (IllegalArgumentException ignored) {
            return;
        }
        player.openInventory(MenuBuilder.createMobEditor(arena, spawnerIndex));
    }

    private void saveEquipment(Player player, String arenaName, int spawnerIndex, Inventory inventory) {
        Optional<Arena> optional = plugin.getArenaManager().getArena(arenaName);
        if (optional.isEmpty()) {
            return;
        }
        Arena arena = optional.get();
        SpawnerConfig spawner = arena.getSpawners().get(spawnerIndex);
        MobConfig mobConfig = spawner.getMobConfig();
        mobConfig.setHelmet(cleanStack(inventory.getItem(10)));
        mobConfig.setChestplate(cleanStack(inventory.getItem(19)));
        mobConfig.setLeggings(cleanStack(inventory.getItem(28)));
        mobConfig.setBoots(cleanStack(inventory.getItem(37)));
        mobConfig.setMainHand(cleanStack(inventory.getItem(25)));
        mobConfig.setOffHand(cleanStack(inventory.getItem(34)));
        plugin.getArenaManager().saveArenas();
        player.sendMessage("§aЕкіпірування моба збережено.");
    }

    private ItemStack cleanStack(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.getType() == Material.BARRIER || stack.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return null;
        }
        return stack.clone();
    }

    private boolean isEquipmentSlot(int slot) {
        return slot == 10 || slot == 19 || slot == 28 || slot == 37 || slot == 25 || slot == 34;
    }

    private double adjustValue(double value, boolean rightClick, double delta, double min, double max) {
        double next = value + (rightClick ? -delta : delta);
        return Math.max(min, Math.min(max, next));
    }
}
