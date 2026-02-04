package com.crystalguard.player;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum PlayerRole {
    WARRIOR("Воїн", Material.IRON_SWORD, "Фронт/контроль"),
    PALADIN("Паладин", Material.SHIELD, "Захист кристалу"),
    ARCHER("Лучник", Material.BOW, "Дальній DPS"),
    MAGE("Маг", Material.BLAZE_ROD, "Бурст AoE"),
    ALCHEMIST("Алхімік/Інженер", Material.BREWING_STAND, "Контроль/утиліті"),
    ASSASSIN("Асасин", Material.STONE_SWORD, "Мобільність"),
    NECROMANCER("Некромант", Material.WITHER_ROSE, "Контроль/призив"),
    BARD("Бард", Material.NOTE_BLOCK, "Підтримка/бафи"),
    DRUID("Друїд", Material.OAK_SAPLING, "Контроль/хіл");

    private final String displayName;
    private final Material icon;
    private final String description;

    PlayerRole(String displayName, Material icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getColoredName() {
        return ChatColor.GOLD + displayName;
    }
}
