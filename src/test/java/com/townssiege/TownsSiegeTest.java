package com.townssiege;

import com.hypixel.hytale.math.vector.Vector3i;
import com.townssiege.models.Siege;
import com.townssiege.utils.FakeTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TownsSiegeTest {

    private static final long HOUR = 3600000;
    private static final long MINUTE = 60000;

    private static final long BANNER_INTERVAL = 3 * HOUR;
    private static final long BANNER_DURATION = 1 * HOUR;
    private static final long SIEGE_DURATION = 24 * HOUR;

    private static final Random random = new Random();

    SiegeManager siegeManager;
    FakeTimeProvider timeProvider;
    UUID attackerId;
    UUID defenderId;

    @BeforeEach
    void setUp() {
        timeProvider = new FakeTimeProvider();
        siegeManager = new SiegeManager(timeProvider, BANNER_INTERVAL, BANNER_DURATION, SIEGE_DURATION);
        attackerId = UUID.randomUUID();
        defenderId = UUID.randomUUID();
    }

    private Vector3i randomBannerPos() {
        return new Vector3i(random.nextInt(1000), random.nextInt(256), random.nextInt(1000));
    }

    private void startTestSiege(UUID territoryId) {
        siegeManager.startSiege(territoryId, attackerId, defenderId, randomBannerPos(),
                "test_dimension", random.nextInt(100), random.nextInt(100));
    }

    @Test
    void startSiege() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        assertTrue(siegeManager.isUnderSiege(territoryId));
    }

    @Test
    void endSiege() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        siegeManager.endSiege(territoryId);
        assertFalse(siegeManager.isUnderSiege(territoryId));
    }

    @Test
    void getSiege() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);
        assertNotNull(siege);
    }

    @Test
    void addAttacker() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);

        Siege siege = siegeManager.getSiege(territoryId);

        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);

        assertTrue(siege.isAttacker(attackerId));
    }

    @Test
    void add2Attackers() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);

        Siege siege = siegeManager.getSiege(territoryId);

        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);

        UUID attacker2Id = UUID.randomUUID();
        siege.addAttacker(attacker2Id);

        assertTrue(siege.isAttacker(attackerId));
        assertTrue(siege.isAttacker(attacker2Id));
    }

    @Test
    void addDefender() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);
        UUID defenderId = UUID.randomUUID();
        siege.addDefender(defenderId);

        assertTrue(siege.isDefender(defenderId));
    }

    @Test
    void add2Defenders() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);

        Siege siege = siegeManager.getSiege(territoryId);

        UUID defenderId = UUID.randomUUID();
        siege.addDefender(defenderId);

        UUID defender2Id = UUID.randomUUID();
        siege.addDefender(defender2Id);

        assertTrue(siege.isDefender(defenderId));
        assertTrue(siege.isDefender(defender2Id));
    }

    @Test
    void isSameTeam() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);
        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);
        UUID attacker2Id = UUID.randomUUID();
        siege.addAttacker(attacker2Id);

        assertTrue(siege.isSameTeam(attackerId, attackerId));
    }

    @Test
    void isAtSiege() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);
        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);
        UUID attacker2Id = UUID.randomUUID();

        assertTrue(siege.isAtSiege(attackerId));
        assertFalse(siege.isAtSiege(attacker2Id));
    }

    @Test
    void addPointsToTeam() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);
        siege.addPoints(attackerId, 10);

        UUID defenderId = UUID.randomUUID();
        siege.addDefender(defenderId);
        siege.addPoints(defenderId, 5);

        assertEquals(10, siege.getPlayerTeam(attackerId).getPoints());
        assertEquals(5, siege.getPlayerTeam(defenderId).getPoints());
    }

    @Test
    void getWinner() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);
        siege.addPoints(attackerId, 10);

        UUID defenderId = UUID.randomUUID();
        siege.addDefender(defenderId);
        siege.addPoints(defenderId, 5);

        assertEquals(siege.getPlayerTeam(attackerId), siege.getWinner());
    }

    @Test
    void bannerActiveAtStart() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        assertTrue(siege.isBannerActive());
    }

    @Test
    void bannerInactiveAfterDuration() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(BANNER_DURATION);

        assertFalse(siege.isBannerActive());
    }

    @Test
    void bannerActiveAgainAfterInterval() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(BANNER_INTERVAL);

        assertTrue(siege.isBannerActive());
    }

    @Test
    void bannerInactiveBetweenIntervals() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(BANNER_DURATION + MINUTE);

        assertFalse(siege.isBannerActive());
    }

    @Test
    void siegeNotExpiredBeforeDuration() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(SIEGE_DURATION - MINUTE);

        assertFalse(siege.isSiegeExpired());
    }

    @Test
    void siegeExpiredAfterDuration() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(SIEGE_DURATION);

        assertTrue(siege.isSiegeExpired());
    }

    @Test
    void bannerInactiveWhenSiegeExpired() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(SIEGE_DURATION);

        assertFalse(siege.isBannerActive());
    }

    @Test
    void timeUntilBannerActiveWhenInactive() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(BANNER_DURATION);

        long expected = BANNER_INTERVAL - BANNER_DURATION;
        assertEquals(expected, siege.getTimeUntilBannerActive());
    }

    @Test
    void timeUntilBannerActiveWhenActive() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        assertEquals(0, siege.getTimeUntilBannerActive());
    }

    @Test
    void remainingBannerTimeWhenActive() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(10 * MINUTE);

        long expected = BANNER_DURATION - (10 * MINUTE);
        assertEquals(expected, siege.getRemainingBannerTime());
    }

    @Test
    void remainingBannerTimeWhenInactive() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(BANNER_DURATION);

        assertEquals(0, siege.getRemainingBannerTime());
    }

    @Test
    void remainingSiegeTime() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        timeProvider.advance(6 * HOUR);

        long expected = SIEGE_DURATION - (6 * HOUR);
        assertEquals(expected, siege.getRemainingSiegeTime());
    }

    @Test
    void multipleBannerCycles() {
        UUID territoryId = UUID.randomUUID();
        startTestSiege(territoryId);
        Siege siege = siegeManager.getSiege(territoryId);

        assertTrue(siege.isBannerActive());

        timeProvider.advance(BANNER_DURATION);
        assertFalse(siege.isBannerActive());

        timeProvider.advance(BANNER_INTERVAL - BANNER_DURATION);
        assertTrue(siege.isBannerActive());

        timeProvider.advance(BANNER_DURATION);
        assertFalse(siege.isBannerActive());

        timeProvider.advance(BANNER_INTERVAL - BANNER_DURATION);
        assertTrue(siege.isBannerActive());
    }
}