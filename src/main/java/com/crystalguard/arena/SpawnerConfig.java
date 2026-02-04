package com.crystalguard.arena;

public class SpawnerConfig {
    private boolean enabled;
    private int startWave = 1;
    private int endWave = 5;
    private int mobsPerWave = 5;
    private int spawnPeriodSeconds = 5;
    private int spawnPointIndex = 1;
    private MobConfig mobConfig = new MobConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getStartWave() {
        return startWave;
    }

    public void setStartWave(int startWave) {
        this.startWave = startWave;
    }

    public int getEndWave() {
        return endWave;
    }

    public void setEndWave(int endWave) {
        this.endWave = endWave;
    }

    public int getMobsPerWave() {
        return mobsPerWave;
    }

    public void setMobsPerWave(int mobsPerWave) {
        this.mobsPerWave = mobsPerWave;
    }

    public int getSpawnPeriodSeconds() {
        return spawnPeriodSeconds;
    }

    public void setSpawnPeriodSeconds(int spawnPeriodSeconds) {
        this.spawnPeriodSeconds = spawnPeriodSeconds;
    }

    public int getSpawnPointIndex() {
        return spawnPointIndex;
    }

    public void setSpawnPointIndex(int spawnPointIndex) {
        this.spawnPointIndex = spawnPointIndex;
    }

    public MobConfig getMobConfig() {
        return mobConfig;
    }

    public void setMobConfig(MobConfig mobConfig) {
        this.mobConfig = mobConfig;
    }
}
