package com.crystalguard.gui;

import com.crystalguard.arena.MobConfig;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MobRandomizer {
    public static final List<EntityType> HOSTILE_TYPES = List.of(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CREEPER,
            EntityType.HUSK,
            EntityType.STRAY,
            EntityType.DROWNED,
            EntityType.PILLAGER,
            EntityType.VINDICATOR,
            EntityType.ENDERMAN,
            EntityType.SILVERFISH
    );

    private static final List<Material> HELMETS = List.of(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET);
    private static final List<Material> CHESTPLATES = List.of(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE);
    private static final List<Material> LEGGINGS = List.of(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS);
    private static final List<Material> BOOTS = List.of(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS);
    private static final List<Material> WEAPONS = List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.BOW);

    private static final Random RANDOM = new Random();

    private MobRandomizer() {
    }

    public static void randomizeMob(MobConfig config) {
        config.setType(HOSTILE_TYPES.get(RANDOM.nextInt(HOSTILE_TYPES.size())));
        config.setHelmet(randomEquipment(HELMETS));
        config.setChestplate(randomEquipment(CHESTPLATES));
        config.setLeggings(randomEquipment(LEGGINGS));
        config.setBoots(randomEquipment(BOOTS));
        config.setMainHand(randomEquipment(WEAPONS));
        config.setOffHand(null);
        config.setHpMultiplier(randomRange(0.8, 2.5));
        config.setDamageMultiplier(randomRange(0.8, 2.0));
        config.setSpeedMultiplier(randomRange(0.9, 1.6));
        config.setEliteChance(randomRange(0.0, 0.3));
        config.setAggroPlayers(true);
        config.setPreferCrystalWhenIdle(true);
    }

    private static ItemStack randomEquipment(List<Material> list) {
        if (RANDOM.nextDouble() < 0.3) {
            return null;
        }
        return new ItemStack(list.get(RANDOM.nextInt(list.size())));
    }

    private static double randomRange(double min, double max) {
        double value = min + (max - min) * RANDOM.nextDouble();
        return Math.round(value * 100.0) / 100.0;
    }
}
