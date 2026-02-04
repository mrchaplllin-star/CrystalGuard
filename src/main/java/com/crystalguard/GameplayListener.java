package com.crystalguard;

import com.crystalguard.arena.Arena;
import com.crystalguard.arena.ArenaMatch;
import com.crystalguard.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GameplayListener implements Listener {
    private final CrystalGuardPlugin plugin;

    public GameplayListener(CrystalGuardPlugin plugin) {
        this.plugin = plugin;
        startManaRegen();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        double health = player.getHealth() - event.getFinalDamage();
        if (health > 0) {
            return;
        }
        PlayerProfile profile = plugin.getPlayerManager().getProfile(player);
        if (profile.getSelectedArena() == null) {
            return;
        }
        event.setCancelled(true);
        Arena arena = profile.getSelectedArena();
        if (arena.getDeadWarp() != null) {
            player.teleport(arena.getDeadWarp());
        }
        profile.setDowned(true);
        player.setHealth(1.0);
        player.sendMessage("§cВи в нокауті. Чекайте воскресіння на початку наступної хвилі.");
    }

    @EventHandler
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        Entity damager = event.getDamager();
        Player attacker = null;
        if (damager instanceof Player player) {
            attacker = player;
        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }
        if (attacker == null) {
            return;
        }
        PlayerProfile attackerProfile = plugin.getPlayerManager().getProfile(attacker);
        PlayerProfile targetProfile = plugin.getPlayerManager().getProfile(target);
        if (attackerProfile.getSelectedArena() != null
                && attackerProfile.getSelectedArena().equals(targetProfile.getSelectedArena())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!event.getEntity().getScoreboardTags().contains(ArenaMatch.WAVE_TAG)) {
            return;
        }
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            plugin.getArenaManager().getMatch(arena).ifPresent(match -> match.handleMobDeath(event.getEntity()));
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSkill(PlayerInteractEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!event.getAction().isRightClick()) {
            return;
        }
        PlayerProfile profile = plugin.getPlayerManager().getProfile(player);
        if (profile.getSelectedArena() == null) {
            return;
        }
        int manaCost = event.getPlayer().isSneaking() ?
                plugin.getConfig().getInt("skills.manaCostSkill2", 7) :
                plugin.getConfig().getInt("skills.manaCostSkill1", 4);
        if (player.getFoodLevel() < manaCost) {
            player.sendMessage("§cНедостатньо мани!");
            return;
        }
        player.setFoodLevel(Math.max(0, player.getFoodLevel() - manaCost));
        if (player.isSneaking()) {
            castSkillTwo(player);
        } else {
            castSkillOne(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerProfile profile = plugin.getPlayerManager().getProfile(event.getPlayer());
        if (profile.getSelectedArena() != null) {
            plugin.getArenaManager().getMatch(profile.getSelectedArena()).ifPresent(match -> match.removeParticipant(event.getPlayer()));
        }
        plugin.getPlayerManager().clearProfile(event.getPlayer());
    }

    private void castSkillOne(Player player) {
        double radius = 6.0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld() != player.getWorld()) {
                continue;
            }
            if (target.getLocation().distance(player.getLocation()) <= radius) {
                var maxHealth = target.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null) {
                    double max = maxHealth.getBaseValue();
                    target.setHealth(Math.min(max, target.getHealth() + 6));
                }
            }
        }
        player.sendMessage("§aВи застосували зцілення!");
    }

    private void castSkillTwo(Player player) {
        Location location = player.getEyeLocation();
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setVelocity(location.getDirection().multiply(1.2));
        fireball.setYield(1.5f);
        player.sendMessage("§bВи випустили магічний заряд!");
        spawnArrowRain(player.getLocation());
    }

    private void spawnArrowRain(Location center) {
        for (int i = 0; i < 8; i++) {
            Location spawn = center.clone().add(randomOffset(), 8, randomOffset());
            spawn.getWorld().spawnArrow(spawn, new Vector(0, -1, 0), 1.8f, 0).setCritical(true);
        }
    }

    private double randomOffset() {
        return (Math.random() - 0.5) * 6;
    }

    private void startManaRegen() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int regen = plugin.getConfig().getInt("mana.regenPerSecond", 1);
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + regen));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
