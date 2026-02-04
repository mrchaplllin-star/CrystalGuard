package com.crystalguard.player;

import com.crystalguard.arena.Arena;
import com.crystalguard.player.PlayerRole;

public class PlayerProfile {
    private Arena selectedArena;
    private PlayerRole role;
    private int positionIndex;
    private boolean downed;

    public Arena getSelectedArena() {
        return selectedArena;
    }

    public void setSelectedArena(Arena selectedArena) {
        this.selectedArena = selectedArena;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public boolean isDowned() {
        return downed;
    }

    public void setDowned(boolean downed) {
        this.downed = downed;
    }
}
