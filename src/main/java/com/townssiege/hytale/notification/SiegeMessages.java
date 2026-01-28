package com.townssiege.hytale.notification;

import com.hypixel.hytale.server.core.Message;

import java.awt.Color;

public class SiegeMessages {

    // Colors
    private static final Color GOLD = new Color(255, 170, 0);
    private static final Color RED = new Color(255, 85, 85);
    private static final Color GREEN = new Color(85, 255, 85);
    private static final Color AQUA = new Color(85, 255, 255);
    private static final Color YELLOW = new Color(255, 255, 85);
    private static final Color GRAY = new Color(170, 170, 170);
    private static final Color WHITE = Color.WHITE;

    // ==================== SIEGE LIFECYCLE ====================

    public static Message siegeStarted() {
        return Message.join(
                prefix(),
                Message.raw("A ").color(WHITE),
                Message.raw("SIEGE").color(RED).bold(true),
                Message.raw(" has begun! Prepare for battle!").color(WHITE)
        );
    }

    public static Message siegeEndDraw() {
        return Message.join(
                prefix(),
                Message.raw("The siege ended in a ").color(WHITE),
                Message.raw("DRAW").color(YELLOW).bold(true),
                Message.raw("! Territory remains with defenders.").color(WHITE)
        );
    }

    public static Message siegeEndAttackersWon() {
        return Message.join(
                prefix(),
                Message.raw("VICTORY").color(RED).bold(true),
                Message.raw("! The attackers have ").color(WHITE),
                Message.raw("conquered").color(RED),
                Message.raw(" the territory!").color(WHITE)
        );
    }

    public static Message siegeEndAttackersWonTransferFailed() {
        return Message.join(
                prefix(),
                Message.raw("Attackers won but territory transfer ").color(WHITE),
                Message.raw("failed").color(RED),
                Message.raw("!").color(WHITE)
        );
    }

    public static Message siegeEndDefendersWon() {
        return Message.join(
                prefix(),
                Message.raw("VICTORY").color(GREEN).bold(true),
                Message.raw("! The defenders have ").color(WHITE),
                Message.raw("protected").color(GREEN),
                Message.raw(" their territory!").color(WHITE)
        );
    }

    // ==================== BANNER STATE ====================

    public static Message bannerActive() {
        return Message.join(
                prefix(),
                Message.raw("The control point is now ").color(WHITE),
                Message.raw("ACTIVE").color(GREEN).bold(true),
                Message.raw("! Contest the territory!").color(WHITE)
        );
    }

    public static Message bannerInactive() {
        return Message.join(
                prefix(),
                Message.raw("The control point is now ").color(WHITE),
                Message.raw("inactive").color(GRAY),
                Message.raw(".").color(WHITE)
        );
    }

    // ==================== TIME WARNINGS ====================

    public static Message bannerActivatesIn(String time) {
        return Message.join(
                prefix(),
                Message.raw("Control point activates in ").color(WHITE),
                Message.raw(time).color(YELLOW).bold(true),
                Message.raw("!").color(WHITE)
        );
    }

    public static Message siegeEndsIn(String time) {
        return Message.join(
                prefix(),
                Message.raw("Siege ends in ").color(WHITE),
                Message.raw(time).color(RED).bold(true),
                Message.raw("!").color(WHITE)
        );
    }

    // ==================== POINTS ====================

    public static Message pointsEarnedTerritory(int points, int totalTeamPoints) {
        return Message.join(
                prefix(),
                Message.raw("+").color(GREEN),
                Message.raw(String.valueOf(points)).color(GREEN).bold(true),
                Message.raw(" points ").color(GREEN),
                Message.raw("(Territory Control) ").color(GRAY),
                Message.raw("[Team: ").color(GRAY),
                Message.raw(String.valueOf(totalTeamPoints)).color(AQUA).bold(true),
                Message.raw("]").color(GRAY)
        );
    }

    public static Message pointsEarnedKill(int points, int totalTeamPoints, String victimName) {
        return Message.join(
                prefix(),
                Message.raw("+").color(GREEN),
                Message.raw(String.valueOf(points)).color(GREEN).bold(true),
                Message.raw(" points ").color(GREEN),
                Message.raw("(Eliminated ").color(GRAY),
                Message.raw(victimName).color(RED),
                Message.raw(") ").color(GRAY),
                Message.raw("[Team: ").color(GRAY),
                Message.raw(String.valueOf(totalTeamPoints)).color(AQUA).bold(true),
                Message.raw("]").color(GRAY)
        );
    }

    // ==================== SCORE UPDATE ====================

    public static Message scoreUpdate(int attackerPoints, int defenderPoints) {
        return Message.join(
                prefix(),
                Message.raw("Score: ").color(WHITE),
                Message.raw("Attackers ").color(RED),
                Message.raw(String.valueOf(attackerPoints)).color(RED).bold(true),
                Message.raw(" - ").color(GRAY),
                Message.raw(String.valueOf(defenderPoints)).color(GREEN).bold(true),
                Message.raw(" Defenders").color(GREEN)
        );
    }

    // ==================== PROTECTION MESSAGES ====================

    public static Message cannotBreakNearBanner() {
        return Message.join(
                prefix(),
                Message.raw("Cannot break blocks near the ").color(RED),
                Message.raw("siege banner").color(YELLOW),
                Message.raw("!").color(RED)
        );
    }

    public static Message alreadyInSiege() {
        return Message.join(
                prefix(),
                Message.raw("You are already participating in a siege!").color(RED)
        );
    }

    public static Message noTerritoryInRange() {
        return Message.join(
                prefix(),
                Message.raw("No territory found within range!").color(RED)
        );
    }

    public static Message couldNotFindOwner() {
        return Message.join(
                prefix(),
                Message.raw("Could not find territory owner!").color(RED)
        );
    }

    public static Message cannotSiegeOwnTerritory() {
        return Message.join(
                prefix(),
                Message.raw("You cannot siege your ").color(RED),
                Message.raw("own").color(YELLOW),
                Message.raw(" territory!").color(RED)
        );
    }

    public static Message siegeStartedAttacker() {
        return Message.join(
                prefix(),
                Message.raw("SIEGE INITIATED").color(RED).bold(true),
                Message.raw("! You are ").color(WHITE),
                Message.raw("attacking").color(RED),
                Message.raw(" this territory!").color(WHITE)
        );
    }

    public static Message siegeAlreadyInProgress() {
        return Message.join(
                prefix(),
                Message.raw("A siege is already in progress for this territory!").color(RED)
        );
    }

    // ==================== HELPER ====================

    private static Message prefix() {
        return Message.join(
                Message.raw("[").color(GRAY),
                Message.raw("Siege").color(GOLD).bold(true),
                Message.raw("] ").color(GRAY)
        );
    }
}
