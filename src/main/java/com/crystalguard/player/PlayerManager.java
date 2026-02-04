package com.crystalguard.player;

import com.crystalguard.CrystalGuardPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class PlayerManager {
    private final CrystalGuardPlugin plugin;
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    public PlayerManager(CrystalGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerProfile getProfile(Player player) {
        return profiles.computeIfAbsent(player.getUniqueId(), key -> new PlayerProfile());
    }

    public void clearProfile(Player player) {
        profiles.remove(player.getUniqueId());
    }

    public CrystalGuardPlugin getPlugin() {
        return plugin;
    }
}
