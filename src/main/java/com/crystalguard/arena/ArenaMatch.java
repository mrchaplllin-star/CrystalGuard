package com.crystalguard.arena;

import com.crystalguard.CrystalGuardPlugin;
import com.crystalguard.player.PlayerManager;
import com.crystalguard.player.PlayerProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ArenaMatch {
    public static final String WAVE_TAG = "cg_wave_mob";

    private final CrystalGuardPlugin plugin;
    private final Arena arena;
    private final List<UUID> participants = new ArrayList<>();
    private final Map<Integer, Integer> spawnerSpawned = new HashMap<>();
    private int wave = 0;
    private int crystalHp;
    private BossBar bossBar;
    private BukkitTask waveTimerTask;
    private BukkitTask crystalDamageTask;
    private final List<BukkitTask> spawnerTasks = new ArrayList<>();
    private int aliveWaveMobs;
    private int plannedMobs;
    private int timeRemaining;

    public ArenaMatch(CrystalGuardPlugin plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.crystalHp = plugin.getConfig().getInt("crystal.maxHp", 1000);
    }

    public void addParticipant(Player player) {
        if (!participants.contains(player.getUniqueId())) {
            participants.add(player.getUniqueId());
        }
    }

    public void removeParticipant(Player player) {
        participants.remove(player.getUniqueId());
    }

    public boolean isParticipant(Player player) {
        return participants.contains(player.getUniqueId());
    }

    public void startMatch() {
        arena.setRunning(true);
        wave = 0;
        crystalHp = plugin.getConfig().getInt("crystal.maxHp", 1000);
        startNextWave();
        startCrystalDamageLoop();
    }

    public void stopMatch(boolean victory) {
        arena.setRunning(false);
        stopAllTasks();
        if (bossBar != null) {
            bossBar.removeAll();
        }
        String message = victory ? "§aПеремога! Ви захистили кристал." : "§cПоразка! Кристал знищено.";
        for (UUID uuid : participants) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    private void startCrystalDamageLoop() {
        double radius = plugin.getConfig().getDouble("crystal.hitRadius", 2.5);
        long periodTicks = Math.max(1L, Math.round(plugin.getConfig().getDouble("crystal.hitPeriodSeconds", 1.0) * 20));
        crystalDamageTask = new BukkitRunnable() {
            @Override
            public void run() {
                Location crystal = arena.getCrystalLocation();
                if (crystal == null) {
                    return;
                }
                int attackers = 0;
                for (Entity entity : crystal.getWorld().getNearbyEntities(crystal, radius, radius, radius)) {
                    if (entity instanceof LivingEntity living && living.getScoreboardTags().contains(WAVE_TAG)) {
                        attackers++;
                    }
                }
                if (attackers > 0) {
                    damageCrystal(attackers);
                }
            }
        }.runTaskTimer(plugin, periodTicks, periodTicks);
    }

    private void damageCrystal(int amount) {
        crystalHp = Math.max(0, crystalHp - amount);
        if (crystalHp <= 0) {
            stopMatch(false);
        }
    }

    private void startNextWave() {
        if (wave >= plugin.getConfig().getInt("waves.total", 5)) {
            stopMatch(true);
            return;
        }
        wave++;
        plannedMobs = calculatePlannedMobs();
        timeRemaining = calculateWaveTime(plannedMobs);
        aliveWaveMobs = 0;
        spawnerSpawned.clear();
        startWaveTimer();
        startSpawnerTasks();
        resurrectDownedPlayers();
        if (plugin.getConfig().getBoolean("crystal.regenEnabled", true)) {
            crystalHp = Math.min(plugin.getConfig().getInt("crystal.maxHp", 1000),
                    crystalHp + plugin.getConfig().getInt("crystal.regenPerSecond", 25) * plugin.getConfig().getInt("waves.interWaveDelaySeconds", 60));
        }
        broadcast("§eПочалася хвиля " + wave + "! Мобів: " + plannedMobs + ".");
    }

    private int calculatePlannedMobs() {
        int total = 0;
        for (SpawnerConfig spawner : arena.getSpawners()) {
            if (spawner.isEnabled() && spawner.getStartWave() <= wave && spawner.getEndWave() >= wave) {
                total += spawner.getMobsPerWave();
            }
        }
        return total;
    }

    private int calculateWaveTime(int planned) {
        int time = 120 + planned * 10;
        return ((time + 9) / 10) * 10;
    }

    private void startWaveTimer() {
        if (bossBar != null) {
            bossBar.removeAll();
        }
        bossBar = Bukkit.createBossBar("Хвиля " + wave + " - Час: " + formatTime(timeRemaining), BarColor.BLUE, BarStyle.SOLID);
        for (UUID uuid : participants) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                bossBar.addPlayer(player);
            }
        }
        if (waveTimerTask != null) {
            waveTimerTask.cancel();
        }
        waveTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                timeRemaining--;
                if (timeRemaining <= 0) {
                    handleWaveTimeout();
                    return;
                }
                bossBar.setTitle("Хвиля " + wave + " - Час: " + formatTime(timeRemaining));
                bossBar.setProgress(Math.max(0.0, timeRemaining / (double) calculateWaveTime(plannedMobs)));
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void handleWaveTimeout() {
        if (waveTimerTask != null) {
            waveTimerTask.cancel();
        }
        if (aliveWaveMobs > 0) {
            damageCrystal(aliveWaveMobs * 10);
            if (crystalHp <= 0) {
                return;
            }
            killRemainingWaveMobs();
        }
        completeWave();
    }

    private void startSpawnerTasks() {
        stopSpawnerTasks();
        for (int i = 0; i < arena.getSpawners().size(); i++) {
            SpawnerConfig spawner = arena.getSpawners().get(i);
            if (!spawner.isEnabled() || spawner.getStartWave() > wave || spawner.getEndWave() < wave) {
                continue;
            }
            int index = i;
            spawnerSpawned.put(index, 0);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    int spawned = spawnerSpawned.getOrDefault(index, 0);
                    if (spawned >= spawner.getMobsPerWave()) {
                        cancel();
                        checkWaveCompletion();
                        return;
                    }
                    spawnMob(spawner);
                    spawnerSpawned.put(index, spawned + 1);
                }
            }.runTaskTimer(plugin, 0L, spawner.getSpawnPeriodSeconds() * 20L);
            spawnerTasks.add(task);
        }
    }

    private void spawnMob(SpawnerConfig spawner) {
        if (arena.getSpawnPoints().isEmpty()) {
            return;
        }
        int index = Math.max(1, Math.min(spawner.getSpawnPointIndex(), arena.getSpawnPoints().size())) - 1;
        Location spawn = arena.getSpawnPoints().get(index);
        MobConfig config = spawner.getMobConfig();
        Entity entity = spawn.getWorld().spawnEntity(spawn, config.getType());
        if (!(entity instanceof Mob mob)) {
            entity.remove();
            return;
        }
        mob.addScoreboardTag(WAVE_TAG);
        applyMobConfig(mob, config);
        aliveWaveMobs++;
    }

    private void applyMobConfig(Mob mob, MobConfig config) {
        LivingEntity living = mob;
        var maxHealth = living.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double base = maxHealth.getBaseValue();
            double max = base * config.getHpMultiplier();
            maxHealth.setBaseValue(max);
            living.setHealth(max);
        }
        var attackDamage = living.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double base = attackDamage.getBaseValue();
            attackDamage.setBaseValue(base * config.getDamageMultiplier());
        }
        var moveSpeed = living.getAttribute(Attribute.MOVEMENT_SPEED);
        if (moveSpeed != null) {
            double base = moveSpeed.getBaseValue();
            moveSpeed.setBaseValue(base * config.getSpeedMultiplier());
        }
        EntityEquipment equipment = living.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(config.getHelmet());
            equipment.setChestplate(config.getChestplate());
            equipment.setLeggings(config.getLeggings());
            equipment.setBoots(config.getBoots());
            equipment.setItemInMainHand(config.getMainHand());
            equipment.setItemInOffHand(config.getOffHand());
        }
        if (Math.random() < config.getEliteChance()) {
            living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 60, 0));
        }
    }

    public void handleMobDeath(LivingEntity entity) {
        if (!entity.getScoreboardTags().contains(WAVE_TAG)) {
            return;
        }
        aliveWaveMobs = Math.max(0, aliveWaveMobs - 1);
        checkWaveCompletion();
    }

    private void checkWaveCompletion() {
        if (!allPlannedMobsSpawned()) {
            return;
        }
        if (aliveWaveMobs <= 0) {
            completeWave();
        }
    }

    private boolean allPlannedMobsSpawned() {
        int totalSpawned = 0;
        for (Integer count : spawnerSpawned.values()) {
            totalSpawned += count;
        }
        return totalSpawned >= plannedMobs;
    }

    private void completeWave() {
        stopSpawnerTasks();
        if (waveTimerTask != null) {
            waveTimerTask.cancel();
        }
        broadcast("§aХвиля " + wave + " завершена!");
        if (wave >= plugin.getConfig().getInt("waves.total", 5)) {
            stopMatch(true);
            return;
        }
        int delay = plugin.getConfig().getInt("waves.interWaveDelaySeconds", 60);
        new BukkitRunnable() {
            @Override
            public void run() {
                startNextWave();
            }
        }.runTaskLater(plugin, delay * 20L);
    }

    private void killRemainingWaveMobs() {
        for (UUID uuid : participants) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            for (Entity entity : player.getWorld().getEntities()) {
                if (entity instanceof LivingEntity living && living.getScoreboardTags().contains(WAVE_TAG)) {
                    living.damage(10000);
                }
            }
        }
        aliveWaveMobs = 0;
    }

    private void resurrectDownedPlayers() {
        PlayerManager playerManager = plugin.getPlayerManager();
        for (UUID uuid : participants) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            PlayerProfile profile = playerManager.getProfile(player);
            if (profile.isDowned()) {
                profile.setDowned(false);
                player.setHealth(player.getMaxHealth());
                Location warp = arena.getPlayerWarps().get(Math.max(0, profile.getPositionIndex() - 1));
                if (warp != null) {
                    player.teleport(warp);
                }
                player.sendMessage("§aВас воскресили! Підготуйтеся до хвилі.");
            }
        }
    }

    private void stopSpawnerTasks() {
        for (BukkitTask task : spawnerTasks) {
            task.cancel();
        }
        spawnerTasks.clear();
    }

    private void stopAllTasks() {
        stopSpawnerTasks();
        if (waveTimerTask != null) {
            waveTimerTask.cancel();
        }
        if (crystalDamageTask != null) {
            crystalDamageTask.cancel();
        }
    }

    private void broadcast(String message) {
        for (UUID uuid : participants) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int rest = seconds % 60;
        return String.format("%d:%02d", minutes, rest);
    }

    public Arena getArena() {
        return arena;
    }

    public int getWave() {
        return wave;
    }

    public int getCrystalHp() {
        return crystalHp;
    }
}
