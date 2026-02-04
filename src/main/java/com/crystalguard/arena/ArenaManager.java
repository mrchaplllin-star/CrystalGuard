package com.crystalguard.arena;

import com.crystalguard.CrystalGuardPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaManager {
    private final CrystalGuardPlugin plugin;
    private final List<Arena> arenas = new ArrayList<>();
    private final Map<String, ArenaMatch> matches = new ConcurrentHashMap<>();
    private final File arenaFile;

    public ArenaManager(CrystalGuardPlugin plugin) {
        this.plugin = plugin;
        this.arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public Optional<Arena> getArena(String name) {
        return arenas.stream().filter(arena -> arena.getName().equalsIgnoreCase(name)).findFirst();
    }

    public Arena createArena(String name) {
        Arena arena = new Arena(name);
        arenas.add(arena);
        return arena;
    }

    public void deleteArena(Arena arena) {
        arenas.remove(arena);
        matches.remove(arena.getName().toLowerCase());
    }

    public ArenaMatch getOrCreateMatch(Arena arena) {
        return matches.computeIfAbsent(arena.getName().toLowerCase(), key -> new ArenaMatch(plugin, arena));
    }

    public Optional<ArenaMatch> getMatch(Arena arena) {
        return Optional.ofNullable(matches.get(arena.getName().toLowerCase()));
    }

    public void loadArenas() {
        if (!arenaFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
        ConfigurationSection root = config.getConfigurationSection("arenas");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            Arena arena = new Arena(key);
            for (int i = 0; i < 5; i++) {
                arena.getPlayerWarps().set(i, readLocation(section.getConfigurationSection("playerWarps." + (i + 1))));
            }
            arena.setDeadWarp(readLocation(section.getConfigurationSection("warpDead")));
            arena.setCrystalLocation(readLocation(section.getConfigurationSection("crystal")));
            ConfigurationSection spawnSection = section.getConfigurationSection("spawnPoints");
            if (spawnSection != null) {
                for (String spawnKey : spawnSection.getKeys(false)) {
                    Location loc = readLocation(spawnSection.getConfigurationSection(spawnKey));
                    if (loc != null) {
                        arena.getSpawnPoints().add(loc);
                    }
                }
            }
            ConfigurationSection spawners = section.getConfigurationSection("spawners");
            if (spawners != null) {
                for (int i = 0; i < 15; i++) {
                    ConfigurationSection spawnerSection = spawners.getConfigurationSection(String.valueOf(i + 1));
                    if (spawnerSection == null) {
                        continue;
                    }
                    SpawnerConfig spawner = arena.getSpawners().get(i);
                    spawner.setEnabled(spawnerSection.getBoolean("enabled", false));
                    spawner.setStartWave(spawnerSection.getInt("startWave", 1));
                    spawner.setEndWave(spawnerSection.getInt("endWave", 5));
                    spawner.setMobsPerWave(spawnerSection.getInt("mobsPerWave", 5));
                    spawner.setSpawnPeriodSeconds(spawnerSection.getInt("spawnPeriodSeconds", 5));
                    spawner.setSpawnPointIndex(spawnerSection.getInt("spawnPointIndex", 1));
                    ConfigurationSection mobSection = spawnerSection.getConfigurationSection("mob");
                    if (mobSection != null) {
                        MobConfig mobConfig = MobConfigSerializer.read(mobSection);
                        spawner.setMobConfig(mobConfig);
                    }
                }
            }
            arenas.add(arena);
        }
    }

    public void saveArenas() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection root = config.createSection("arenas");
        for (Arena arena : arenas) {
            ConfigurationSection section = root.createSection(arena.getName());
            for (int i = 0; i < 5; i++) {
                writeLocation(section.createSection("playerWarps." + (i + 1)), arena.getPlayerWarps().get(i));
            }
            writeLocation(section.createSection("warpDead"), arena.getDeadWarp());
            writeLocation(section.createSection("crystal"), arena.getCrystalLocation());
            ConfigurationSection spawnSection = section.createSection("spawnPoints");
            for (int i = 0; i < arena.getSpawnPoints().size(); i++) {
                writeLocation(spawnSection.createSection(String.valueOf(i + 1)), arena.getSpawnPoints().get(i));
            }
            ConfigurationSection spawners = section.createSection("spawners");
            for (int i = 0; i < arena.getSpawners().size(); i++) {
                SpawnerConfig spawner = arena.getSpawners().get(i);
                ConfigurationSection spawnerSection = spawners.createSection(String.valueOf(i + 1));
                spawnerSection.set("enabled", spawner.isEnabled());
                spawnerSection.set("startWave", spawner.getStartWave());
                spawnerSection.set("endWave", spawner.getEndWave());
                spawnerSection.set("mobsPerWave", spawner.getMobsPerWave());
                spawnerSection.set("spawnPeriodSeconds", spawner.getSpawnPeriodSeconds());
                spawnerSection.set("spawnPointIndex", spawner.getSpawnPointIndex());
                MobConfigSerializer.write(spawnerSection.createSection("mob"), spawner.getMobConfig());
            }
        }
        try {
            config.save(arenaFile);
        } catch (IOException exception) {
            Bukkit.getLogger().warning("Не вдалося зберегти arenas.yml: " + exception.getMessage());
        }
    }

    private Location readLocation(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String worldName = section.getString("world");
        if (worldName == null) {
            return null;
        }
        return new Location(
                Bukkit.getWorld(worldName),
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch")
        );
    }

    private void writeLocation(ConfigurationSection section, Location location) {
        if (location == null) {
            return;
        }
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    public CrystalGuardPlugin getPlugin() {
        return plugin;
    }
}
