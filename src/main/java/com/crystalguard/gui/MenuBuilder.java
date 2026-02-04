package com.crystalguard.gui;

import com.crystalguard.CrystalGuardPlugin;
import com.crystalguard.arena.Arena;
import com.crystalguard.arena.MobConfig;
import com.crystalguard.arena.SpawnerConfig;
import com.crystalguard.player.PlayerProfile;
import com.crystalguard.player.PlayerRole;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuBuilder {
    private MenuBuilder() {
    }

    public static Inventory createPlayerMain(CrystalGuardPlugin plugin, Player player) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.PLAYER_MAIN, null, -1), 27, "CrystalGuard");
        PlayerProfile profile = plugin.getPlayerManager().getProfile(player);
        inventory.setItem(11, createItem(Material.EMERALD, "§aОбрати арену", "§7Поточна: " + nameOrDash(profile.getSelectedArena())));
        inventory.setItem(13, createItem(Material.NETHER_STAR, "§bОбрати клас", "§7" + roleOrDash(profile.getRole())));
        inventory.setItem(15, createItem(Material.ARMOR_STAND, "§eОбрати позицію", "§7Позиція: " + positionOrDash(profile.getPositionIndex())));
        inventory.setItem(22, createItem(Material.DIAMOND_BLOCK, "§aГотовий/Почати матч", "§7Потрібні: арена, клас, позиція"));
        return inventory;
    }

    public static Inventory createArenaSelect(CrystalGuardPlugin plugin) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.ARENA_SELECT, null, -1), 54, "Вибір арени");
        int slot = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            ItemStack item = createItem(Material.PLAYER_HEAD, "§a" + arena.getName(), "§7Клікіть для вибору");
            inventory.setItem(slot++, item);
            if (slot >= inventory.getSize()) {
                break;
            }
        }
        return inventory;
    }

    public static Inventory createPositionSelect(CrystalGuardPlugin plugin, Player viewer) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.POSITION_SELECT, null, -1), 27, "Вибір позиції");
        PlayerProfile profile = plugin.getPlayerManager().getProfile(viewer);
        Arena arena = profile.getSelectedArena();
        for (int i = 0; i < 5; i++) {
            boolean occupied = isPositionOccupied(plugin, arena, i + 1);
            Material material = occupied ? Material.RED_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE;
            String name = (occupied ? "§c" : "§a") + "Позиція " + (i + 1);
            inventory.setItem(10 + i, createItem(material, name, occupied ? "§7Зайнято" : "§7Вільно"));
        }
        return inventory;
    }

    public static Inventory createAdminArenaList(CrystalGuardPlugin plugin) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.ADMIN_ARENA_LIST, null, -1), 54, "Арени (Адмін)");
        int slot = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            inventory.setItem(slot++, createItem(Material.BEACON, "§b" + arena.getName(), "§7Редагувати арену"));
        }
        inventory.setItem(53, createItem(Material.ANVIL, "§aДодати арену", "§7Створити нову"));
        return inventory;
    }

    public static Inventory createAdminArenaEditor(Arena arena) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.ADMIN_ARENA_EDITOR, arena.getName(), -1), 54, "Редактор арени");
        inventory.setItem(10, createItem(Material.NAME_TAG, "§eНазва: " + arena.getName(), "§7Незмінна"));
        for (int i = 0; i < 5; i++) {
            inventory.setItem(12 + i, createItem(Material.LODESTONE, "§bWarp позиції " + (i + 1), "§7Клік: встановити"));
        }
        inventory.setItem(19, createItem(Material.SKELETON_SKULL, "§cwarp_dead", "§7Клік: встановити"));
        inventory.setItem(21, createItem(Material.END_CRYSTAL, "§dКристал", "§7Клік: встановити"));
        inventory.setItem(23, createItem(Material.ENDER_PEARL, "§aТочки спавну", "§7Клік: додати поточну"));
        inventory.setItem(24, createItem(Material.BARRIER, "§cОчистити точки", "§7Видалити всі"));
        inventory.setItem(31, createItem(Material.SPAWNER, "§bСпавнери", "§7Відкрити меню"));
        inventory.setItem(39, createItem(Material.LIME_DYE, "§aТестовий запуск", "§7Запустити/зупинити"));
        inventory.setItem(49, createItem(Material.EMERALD_BLOCK, "§aЗберегти", "§7Зберегти арену"));
        inventory.setItem(50, createItem(Material.REDSTONE_BLOCK, "§cВидалити", "§7Видалити арену"));
        return inventory;
    }

    public static Inventory createSpawnerMenu(Arena arena) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.ADMIN_SPAWNER_MENU, arena.getName(), -1), 54, "Спавнери арени");
        for (int i = 0; i < arena.getSpawners().size(); i++) {
            SpawnerConfig spawner = arena.getSpawners().get(i);
            String status = spawner.isEnabled() ? "§aУвімкнено" : "§cВимкнено";
            ItemStack item = createItem(Material.SPAWNER, "§eСпавнер " + (i + 1),
                    status,
                    "§7Хвилі: " + spawner.getStartWave() + "-" + spawner.getEndWave(),
                    "§7Мобів/хвиля: " + spawner.getMobsPerWave(),
                    "§7Період: " + spawner.getSpawnPeriodSeconds() + "с",
                    "§7Точка: " + spawner.getSpawnPointIndex(),
                    "§7ПКМ: відкрити редактор",
                    "§7ЛКМ: старт хвилі -1",
                    "§7Shift+ЛКМ: кінець хвилі -1",
                    "§7Shift+ПКМ: кінець хвилі +1",
                    "§7NUM: мобів +1, DROP: мобів -1",
                    "§7CTRL+DROP: період +1, DOUBLE: період -1");
            tagAction(item, "spawner_entry");
            inventory.setItem(i, item);
        }
        ItemStack info = createItem(Material.ANVIL, "§aНалаштування спавнерів",
                "§7Використовуйте кліки для зміни параметрів.");
        tagAction(info, "spawner_info");
        inventory.setItem(53, info);
        return inventory;
    }

    public static Inventory createMobEditor(Arena arena, int spawnerIndex) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.ADMIN_MOB_EDITOR, arena.getName(), spawnerIndex), 54, "Редактор моба");
        SpawnerConfig spawner = arena.getSpawners().get(spawnerIndex);
        MobConfig mobConfig = spawner.getMobConfig();
        inventory.setItem(36, actionItem(Material.NAME_TAG, "§eТип моба",
                "§7Поточний: " + mobConfig.getType().name(),
                "§7Клік: змінити тип"));
        inventory.setItem(28, namedStack(mobConfig.getHelmet(), "§bШолом"));
        inventory.setItem(29, namedStack(mobConfig.getChestplate(), "§bНагрудник"));
        inventory.setItem(30, namedStack(mobConfig.getLeggings(), "§bПоножі"));
        inventory.setItem(31, namedStack(mobConfig.getBoots(), "§bЧеревики"));
        inventory.setItem(33, namedStack(mobConfig.getMainHand(), "§bОсновна рука"));
        inventory.setItem(34, namedStack(mobConfig.getOffHand(), "§bДодаткова рука"));
        inventory.setItem(37, actionItem(Material.HEART_OF_THE_SEA, "§dHP множник",
                "§7Значення: " + format(mobConfig.getHpMultiplier()),
                "§7ЛКМ +0.1, ПКМ -0.1"));
        inventory.setItem(38, actionItem(Material.IRON_SWORD, "§dDamage множник",
                "§7Значення: " + format(mobConfig.getDamageMultiplier()),
                "§7ЛКМ +0.1, ПКМ -0.1"));
        inventory.setItem(39, actionItem(Material.SUGAR, "§dSpeed множник",
                "§7Значення: " + format(mobConfig.getSpeedMultiplier()),
                "§7ЛКМ +0.1, ПКМ -0.1"));
        inventory.setItem(40, actionItem(Material.NETHER_STAR, "§dElite шанс",
                "§7Значення: " + format(mobConfig.getEliteChance()),
                "§7ЛКМ +0.05, ПКМ -0.05"));
        inventory.setItem(42, actionItem(Material.PAPER, "§eAggro на гравців",
                "§7Поточний: " + (mobConfig.isAggroPlayers() ? "Так" : "Ні"),
                "§7Клік: змінити"));
        inventory.setItem(43, actionItem(Material.END_CRYSTAL, "§eПріоритет кристалу",
                "§7Поточний: " + (mobConfig.isPreferCrystalWhenIdle() ? "Так" : "Ні"),
                "§7Клік: змінити"));
        inventory.setItem(45, actionItem(Material.ARROW, "§aНазад",
                "§7Повернутися до меню спавнерів"));
        inventory.setItem(49, actionItem(Material.SPAWNER, "§aСтворити спавнер",
                "§7Матеріал: " + spawner.getSpawnerBlockMaterial().name(),
                "§7Клік: створити блок спавнера",
                "§7Буде поставлено на вашій позиції"));
        inventory.setItem(53, actionItem(Material.RABBIT_FOOT, "§aRandom",
                "§7Зрандомити моба",
                "§7Змінює лише конфіг моба"));
        tagAction(inventory.getItem(36), "mob_type");
        tagAction(inventory.getItem(37), "stat_hp");
        tagAction(inventory.getItem(38), "stat_damage");
        tagAction(inventory.getItem(39), "stat_speed");
        tagAction(inventory.getItem(40), "stat_elite");
        tagAction(inventory.getItem(42), "toggle_aggro");
        tagAction(inventory.getItem(43), "toggle_crystal");
        tagAction(inventory.getItem(45), "back");
        tagAction(inventory.getItem(49), "create_spawner");
        tagAction(inventory.getItem(53), "random");
        fillPalette(inventory);
        return inventory;
    }

    public static Inventory createMobTypeSelect(Arena arena, int spawnerIndex) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.ADMIN_MOB_TYPE_SELECT, arena.getName(), spawnerIndex), 54, "Тип моба");
        int slot = 0;
        for (EntityType type : MobRandomizer.HOSTILE_TYPES) {
            ItemStack item = new ItemStack(mobEgg(type));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + type.name());
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
            if (slot >= inventory.getSize()) {
                break;
            }
        }
        return inventory;
    }

    public static Inventory createClassSelect() {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.CLASS_SELECT, null, -1), 54, "Вибір класу");
        int slot = 0;
        for (PlayerRole role : PlayerRole.values()) {
            ItemStack item = createItem(role.getIcon(), role.getColoredName(), "§7" + role.getDescription());
            inventory.setItem(slot++, item);
        }
        return inventory;
    }

    private static String nameOrDash(Arena arena) {
        return arena == null ? "-" : arena.getName();
    }

    private static String roleOrDash(PlayerRole role) {
        return role == null ? "-" : role.getDisplayName();
    }

    private static String positionOrDash(int position) {
        return position <= 0 ? "-" : String.valueOf(position);
    }

    private static boolean isPositionOccupied(CrystalGuardPlugin plugin, Arena arena, int position) {
        if (arena == null) {
            return false;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.getPlayerManager().getProfile(player);
            if (arena.equals(profile.getSelectedArena()) && profile.getPositionIndex() == position) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (loreLines.length > 0) {
            meta.setLore(List.of(loreLines));
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack namedStack(ItemStack stack, String name) {
        if (stack == null) {
            ItemStack placeholder = createItem(Material.BARRIER, name, "§7Порожньо", "§7Клікніть, щоб встановити предмет");
            tagAction(placeholder, "equipment_placeholder");
            return placeholder;
        }
        ItemStack clone = stack.clone();
        ItemMeta meta = clone.getItemMeta();
        meta.setDisplayName(name);
        clone.setItemMeta(meta);
        return clone;
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }

    private static Material mobEgg(EntityType type) {
        String name = type.name() + "_SPAWN_EGG";
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return Material.ZOMBIE_SPAWN_EGG;
        }
    }

    private static ItemStack actionItem(Material material, String name, String... loreLines) {
        ItemStack item = createItem(material, name, loreLines);
        tagAction(item, "control");
        return item;
    }

    private static void fillPalette(Inventory inventory) {
        List<Material> palette = List.of(
                Material.PURPLE_GLAZED_TERRACOTTA,
                Material.MAGENTA_GLAZED_TERRACOTTA,
                Material.BLUE_GLAZED_TERRACOTTA,
                Material.PURPLE_TERRACOTTA,
                Material.MAGENTA_TERRACOTTA,
                Material.BLUE_TERRACOTTA,
                Material.PURPLE_CONCRETE,
                Material.MAGENTA_CONCRETE,
                Material.BLUE_CONCRETE,
                Material.PURPLE_CONCRETE_POWDER,
                Material.MAGENTA_CONCRETE_POWDER,
                Material.BLUE_CONCRETE_POWDER,
                Material.PURPLE_STAINED_GLASS,
                Material.MAGENTA_STAINED_GLASS,
                Material.BLUE_STAINED_GLASS,
                Material.PURPLE_STAINED_GLASS_PANE,
                Material.MAGENTA_STAINED_GLASS_PANE,
                Material.BLUE_STAINED_GLASS_PANE,
                Material.PURPUR_BLOCK,
                Material.PURPUR_PILLAR,
                Material.END_STONE_BRICKS,
                Material.AMETHYST_BLOCK,
                Material.CRYING_OBSIDIAN,
                Material.OBSIDIAN,
                Material.RESPAWN_ANCHOR,
                Material.SHROOMLIGHT,
                Material.ENCHANTING_TABLE
        );
        int index = 0;
        for (int slot = 0; slot < 27; slot++) {
            Material material = palette.get(index++);
            ItemStack item = createItem(material, "§dПалітра: " + material.name(),
                    "§7Клік: обрати матеріал",
                    "§7Для блоку спавнера");
            tagPalette(item, material);
            inventory.setItem(slot, item);
        }
    }

    private static void tagAction(ItemStack item, String action) {
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key("cg_action"), PersistentDataType.STRING, action);
        item.setItemMeta(meta);
    }

    private static void tagPalette(ItemStack item, Material material) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key("cg_palette"), PersistentDataType.STRING, material.name());
        item.setItemMeta(meta);
    }

    private static NamespacedKey key(String value) {
        return new NamespacedKey(JavaPlugin.getPlugin(com.crystalguard.CrystalGuardPlugin.class), value);
    }
}
