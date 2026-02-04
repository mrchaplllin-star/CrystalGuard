package com.crystalguard.arena;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

public class Arena {
    private final String name;
    private final List<Location> playerWarps = new ArrayList<>();
    private Location deadWarp;
    private Location crystalLocation;
    private final List<Location> spawnPoints = new ArrayList<>();
    private final List<SpawnerConfig> spawners = new ArrayList<>();
    private boolean running;

    public Arena(String name) {
        this.name = name;
        for (int i = 0; i < 5; i++) {
            playerWarps.add(null);
        }
        for (int i = 0; i < 15; i++) {
            spawners.add(new SpawnerConfig());
        }
    }

    public String getName() {
        return name;
    }

    public List<Location> getPlayerWarps() {
        return playerWarps;
    }

    public Location getDeadWarp() {
        return deadWarp;
    }

    public void setDeadWarp(Location deadWarp) {
        this.deadWarp = deadWarp;
    }

    public Location getCrystalLocation() {
        return crystalLocation;
    }

    public void setCrystalLocation(Location crystalLocation) {
        this.crystalLocation = crystalLocation;
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    public List<SpawnerConfig> getSpawners() {
        return spawners;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
