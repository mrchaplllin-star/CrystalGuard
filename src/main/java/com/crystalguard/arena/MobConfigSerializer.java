package com.crystalguard.arena;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MobConfigSerializer {
    public static MobConfig read(ConfigurationSection section) {
        MobConfig config = new MobConfig();
        String typeName = section.getString("type", EntityType.ZOMBIE.name());
        try {
            config.setType(EntityType.valueOf(typeName));
        } catch (IllegalArgumentException ignored) {
            config.setType(EntityType.ZOMBIE);
        }
        config.setHelmet(section.getItemStack("helmet"));
        config.setChestplate(section.getItemStack("chestplate"));
        config.setLeggings(section.getItemStack("leggings"));
        config.setBoots(section.getItemStack("boots"));
        config.setMainHand(section.getItemStack("mainHand"));
        config.setOffHand(section.getItemStack("offHand"));
        config.setHpMultiplier(section.getDouble("hpMultiplier", 1.0));
        config.setDamageMultiplier(section.getDouble("damageMultiplier", 1.0));
        config.setSpeedMultiplier(section.getDouble("speedMultiplier", 1.0));
        config.setEliteChance(section.getDouble("eliteChance", 0.0));
        config.setAggroPlayers(section.getBoolean("aggroPlayers", true));
        config.setPreferCrystalWhenIdle(section.getBoolean("preferCrystalWhenIdle", true));
        return config;
    }

    public static void write(ConfigurationSection section, MobConfig config) {
        section.set("type", config.getType().name());
        section.set("helmet", safeItem(config.getHelmet()));
        section.set("chestplate", safeItem(config.getChestplate()));
        section.set("leggings", safeItem(config.getLeggings()));
        section.set("boots", safeItem(config.getBoots()));
        section.set("mainHand", safeItem(config.getMainHand()));
        section.set("offHand", safeItem(config.getOffHand()));
        section.set("hpMultiplier", config.getHpMultiplier());
        section.set("damageMultiplier", config.getDamageMultiplier());
        section.set("speedMultiplier", config.getSpeedMultiplier());
        section.set("eliteChance", config.getEliteChance());
        section.set("aggroPlayers", config.isAggroPlayers());
        section.set("preferCrystalWhenIdle", config.isPreferCrystalWhenIdle());
    }

    private static ItemStack safeItem(ItemStack stack) {
        return stack == null ? null : stack.clone();
    }
}
