package com.crystalguard;

import com.crystalguard.arena.ArenaManager;
import com.crystalguard.gui.MenuListener;
import com.crystalguard.player.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CrystalGuardPlugin extends JavaPlugin {
    private ArenaManager arenaManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        arenaManager = new ArenaManager(this);
        arenaManager.loadArenas();
        playerManager = new PlayerManager(this);

        CrystalGuardCommand command = new CrystalGuardCommand(this);
        getCommand("turnire").setExecutor(command);
        getCommand("turnire").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new GameplayListener(this), this);
    }

    @Override
    public void onDisable() {
        arenaManager.saveArenas();
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
