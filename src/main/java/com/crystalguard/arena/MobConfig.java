package com.crystalguard.arena;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MobConfig {
    private EntityType type = EntityType.ZOMBIE;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack mainHand;
    private ItemStack offHand;
    private double hpMultiplier = 1.0;
    private double damageMultiplier = 1.0;
    private double speedMultiplier = 1.0;
    private double eliteChance = 0.0;
    private boolean aggroPlayers = true;
    private boolean preferCrystalWhenIdle = true;

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public ItemStack getHelmet() {
        return cloneStack(helmet);
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = cloneStack(helmet);
    }

    public ItemStack getChestplate() {
        return cloneStack(chestplate);
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = cloneStack(chestplate);
    }

    public ItemStack getLeggings() {
        return cloneStack(leggings);
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = cloneStack(leggings);
    }

    public ItemStack getBoots() {
        return cloneStack(boots);
    }

    public void setBoots(ItemStack boots) {
        this.boots = cloneStack(boots);
    }

    public ItemStack getMainHand() {
        return cloneStack(mainHand);
    }

    public void setMainHand(ItemStack mainHand) {
        this.mainHand = cloneStack(mainHand);
    }

    public ItemStack getOffHand() {
        return cloneStack(offHand);
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = cloneStack(offHand);
    }

    public double getHpMultiplier() {
        return hpMultiplier;
    }

    public void setHpMultiplier(double hpMultiplier) {
        this.hpMultiplier = hpMultiplier;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getEliteChance() {
        return eliteChance;
    }

    public void setEliteChance(double eliteChance) {
        this.eliteChance = eliteChance;
    }

    public boolean isAggroPlayers() {
        return aggroPlayers;
    }

    public void setAggroPlayers(boolean aggroPlayers) {
        this.aggroPlayers = aggroPlayers;
    }

    public boolean isPreferCrystalWhenIdle() {
        return preferCrystalWhenIdle;
    }

    public void setPreferCrystalWhenIdle(boolean preferCrystalWhenIdle) {
        this.preferCrystalWhenIdle = preferCrystalWhenIdle;
    }

    public ItemStack getTypeIcon() {
        Material spawnEgg = Material.ZOMBIE_SPAWN_EGG;
        String name = type.name() + "_SPAWN_EGG";
        try {
            spawnEgg = Material.valueOf(name);
        } catch (IllegalArgumentException ignored) {
        }
        return new ItemStack(spawnEgg);
    }

    private ItemStack cloneStack(ItemStack stack) {
        return stack == null ? null : stack.clone();
    }
}
