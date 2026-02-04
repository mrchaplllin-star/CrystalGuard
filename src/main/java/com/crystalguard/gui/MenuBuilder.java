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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
                    "§7Клік: редагувати");
            inventory.setItem(i, item);
        }
        return inventory;
    }

    public static Inventory createMobEditor(Arena arena, int spawnerIndex) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.ADMIN_MOB_EDITOR, arena.getName(), spawnerIndex), 54, "Редактор моба");
        SpawnerConfig spawner = arena.getSpawners().get(spawnerIndex);
        MobConfig mobConfig = spawner.getMobConfig();
        inventory.setItem(4, createItem(Material.NAME_TAG, "§eТип моба", "§7" + mobConfig.getType().name(), "§7Клік: змінити"));
        inventory.setItem(10, namedStack(mobConfig.getHelmet(), "§bШолом"));
        inventory.setItem(19, namedStack(mobConfig.getChestplate(), "§bНагрудник"));
        inventory.setItem(28, namedStack(mobConfig.getLeggings(), "§bПоножі"));
        inventory.setItem(37, namedStack(mobConfig.getBoots(), "§bЧеревики"));
        inventory.setItem(25, namedStack(mobConfig.getMainHand(), "§bОсновна рука"));
        inventory.setItem(34, namedStack(mobConfig.getOffHand(), "§bДодаткова рука"));
        inventory.setItem(13, createItem(Material.HEART_OF_THE_SEA, "§dHP множник", "§7" + format(mobConfig.getHpMultiplier()), "§7ЛКМ +0.1, ПКМ -0.1"));
        inventory.setItem(14, createItem(Material.IRON_SWORD, "§dDamage множник", "§7" + format(mobConfig.getDamageMultiplier()), "§7ЛКМ +0.1, ПКМ -0.1"));
        inventory.setItem(15, createItem(Material.SUGAR, "§dSpeed множник", "§7" + format(mobConfig.getSpeedMultiplier()), "§7ЛКМ +0.1, ПКМ -0.1"));
        inventory.setItem(16, createItem(Material.NETHER_STAR, "§dElite шанс", "§7" + format(mobConfig.getEliteChance()), "§7ЛКМ +0.05, ПКМ -0.05"));
        inventory.setItem(31, createItem(Material.PAPER, "§eAggro на гравців", "§7" + (mobConfig.isAggroPlayers() ? "Так" : "Ні"), "§7Клік: змінити"));
        inventory.setItem(32, createItem(Material.END_CRYSTAL, "§eПріоритет кристалу", "§7" + (mobConfig.isPreferCrystalWhenIdle() ? "Так" : "Ні"), "§7Клік: змінити"));
        inventory.setItem(45, createItem(Material.GRAY_STAINED_GLASS_PANE, "§8Майбутній слот"));
        inventory.setItem(46, createItem(Material.GRAY_STAINED_GLASS_PANE, "§8Майбутній слот"));
        inventory.setItem(47, createItem(Material.GRAY_STAINED_GLASS_PANE, "§8Майбутній слот"));
        inventory.setItem(53, createItem(Material.RABBIT_FOOT, "§aRandom", "§7Зрандомити моба"));
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
            return createItem(Material.BARRIER, name, "§7Порожньо");
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
}
