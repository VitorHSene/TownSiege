package com.townssiege.models;

import com.townssiege.utils.TimeProvider;

import java.util.UUID;

public class Siege {

    private final Team attackers;
    private final Team defenders;
    private final TimeProvider timeProvider;
    private final long startTime;
    private final long bannerInterval;
    private final long bannerDuration;
    private final long siegeDuration;

    public Siege(TimeProvider timeProvider, long bannerInterval, long bannerDuration, long siegeDuration,
                 UUID initialAttackerId, UUID initialDefenderId) {
        this.attackers = new Team(TeamRole.ATTACKER);
        this.defenders = new Team(TeamRole.DEFENDER);
        this.timeProvider = timeProvider;
        this.startTime = timeProvider.now();
        this.bannerInterval = bannerInterval;
        this.bannerDuration = bannerDuration;
        this.siegeDuration = siegeDuration;

        this.attackers.addPlayer(initialAttackerId);
        this.defenders.addPlayer(initialDefenderId);
    }

    public boolean isBannerActive() {
        if (isSiegeExpired())
            return false;
        long elapsed = timeProvider.now() - startTime;
        long positionInCycle = elapsed % bannerInterval;
        return positionInCycle < bannerDuration;
    }

    public boolean isSiegeExpired() {
        return timeProvider.now() - startTime >= siegeDuration;
    }

    public long getTimeUntilBannerActive() {
        if (isBannerActive())
            return 0;
        long elapsed = timeProvider.now() - startTime;
        long positionInCycle = elapsed % bannerInterval;
        return bannerInterval - positionInCycle;
    }

    public long getRemainingBannerTime() {
        if (!isBannerActive())
            return 0;
        long elapsed = timeProvider.now() - startTime;
        long positionInCycle = elapsed % bannerInterval;
        return bannerDuration - positionInCycle;
    }

    public long getRemainingSiegeTime() {
        long remaining = siegeDuration - (timeProvider.now() - startTime);
        return Math.max(0, remaining);
    }

    public boolean addAttacker(UUID playerId) {
        return attackers.addPlayer(playerId);
    }

    public boolean addDefender(UUID playerId) {
        return defenders.addPlayer(playerId);
    }

    public boolean isAttacker(UUID playerId) {
        return attackers.hasPlayer(playerId);
    }

    public boolean isDefender(UUID playerId) {
        return defenders.hasPlayer(playerId);
    }

    public boolean isAtSiege(UUID playerId) {
        return isAttacker(playerId) || isDefender(playerId);
    }

    public boolean isSameTeam(UUID a, UUID b) {
        return (isAttacker(a) && isAttacker(b)) || (isDefender(a) && isDefender(b));
    }

    public Team getPlayerTeam(UUID playerId) {
        if (attackers.hasPlayer(playerId))
            return attackers;
        if (defenders.hasPlayer(playerId))
            return defenders;
        return null;
    }

    public void addPoints(UUID playerId, int amount) {
        Team team = getPlayerTeam(playerId);
        if (team != null)
            team.addPoints(amount);
    }

    public Team getWinner() {
        if (attackers.getPoints() > defenders.getPoints()) {
            return attackers;
        } else if (defenders.getPoints() > attackers.getPoints()) {
            return defenders;
        }
        return null;
    }

    public Team getAttackers() {
        return attackers;
    }

    public Team getDefenders() {
        return defenders;
    }
}