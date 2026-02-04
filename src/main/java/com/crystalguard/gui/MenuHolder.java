package com.crystalguard.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {
    private final MenuType type;
    private final String arenaName;
    private final int spawnerIndex;

    public MenuHolder(MenuType type, String arenaName, int spawnerIndex) {
        this.type = type;
        this.arenaName = arenaName;
        this.spawnerIndex = spawnerIndex;
    }

    public MenuType getType() {
        return type;
    }

    public String getArenaName() {
        return arenaName;
    }

    public int getSpawnerIndex() {
        return spawnerIndex;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
