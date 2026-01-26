package com.townssiege.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {

    private final TeamRole role;
    private final Set<UUID> players;
    private int points;

    public Team(TeamRole role) {
        this.role = role;
        this.players = new HashSet<>();
        this.points = 0;
    }

    public TeamRole getRole() {
        return role;
    }

    public boolean addPlayer(UUID playerId) {
        if (players.contains(playerId))
            return false;
        players.add(playerId);
        return true;
    }

    public boolean removePlayer(UUID playerId) {
        return players.remove(playerId);
    }

    public boolean hasPlayer(UUID playerId) {
        return players.contains(playerId);
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}