package com.townssiege;

import com.townssiege.models.Siege;
import com.townssiege.models.Territory;
import com.townssiege.utils.FakeTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TownsSiegeTest {

    private static final long HOUR = 3600000;
    private static final long MINUTE = 60000;

    private static final long BANNER_INTERVAL = 3 * HOUR;
    private static final long BANNER_DURATION = 1 * HOUR;
    private static final long SIEGE_DURATION = 24 * HOUR;

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

    @Test
    void startSiege() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        assertTrue(siegeManager.isUnderSiege(territory.getId()));
    }

    @Test
    void endSiege() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        siegeManager.endSiege(territory.getId());
        assertFalse(siegeManager.isUnderSiege(territory.getId()));
    }

    @Test
    void getSiege() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());
        assertNotNull(siege);
    }

    @Test
    void addAttacker() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);

        Siege siege = siegeManager.getSiege(territory.getId());

        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);

        assertTrue(siege.isAttacker(attackerId));
    }

    @Test
    void add2Attackers() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);

        Siege siege = siegeManager.getSiege(territory.getId());

        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);

        UUID attacker2Id = UUID.randomUUID();
        siege.addAttacker(attacker2Id);

        assertTrue(siege.isAttacker(attackerId));
        assertTrue(siege.isAttacker(attacker2Id));
    }

    @Test
    void addDefender() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());
        UUID defenderId = UUID.randomUUID();
        siege.addDefender(defenderId);

        assertTrue(siege.isDefender(defenderId));
    }

    @Test
    void add2Defenders() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);

        Siege siege = siegeManager.getSiege(territory.getId());

        UUID defenderId = UUID.randomUUID();
        siege.addDefender(defenderId);

        UUID defender2Id = UUID.randomUUID();
        siege.addDefender(defender2Id);

        assertTrue(siege.isDefender(defenderId));
        assertTrue(siege.isDefender(defender2Id));
    }

    @Test
    void isSameTeam() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());
        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);
        UUID attacker2Id = UUID.randomUUID();
        siege.addAttacker(attacker2Id);

        assertTrue(siege.isSameTeam(attackerId, attackerId));
    }

    @Test
    void isAtSiege() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());
        UUID attackerId = UUID.randomUUID();
        siege.addAttacker(attackerId);
        UUID attacker2Id = UUID.randomUUID();

        assertTrue(siege.isAtSiege(attackerId));
        assertFalse(siege.isAtSiege(attacker2Id));
    }

    @Test
    void addPointsToTeam() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

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
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

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
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        assertTrue(siege.isBannerActive());
    }

    @Test
    void bannerInactiveAfterDuration() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(BANNER_DURATION);

        assertFalse(siege.isBannerActive());
    }

    @Test
    void bannerActiveAgainAfterInterval() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(BANNER_INTERVAL);

        assertTrue(siege.isBannerActive());
    }

    @Test
    void bannerInactiveBetweenIntervals() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(BANNER_DURATION + MINUTE);

        assertFalse(siege.isBannerActive());
    }

    @Test
    void siegeNotExpiredBeforeDuration() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(SIEGE_DURATION - MINUTE);

        assertFalse(siege.isSiegeExpired());
    }

    @Test
    void siegeExpiredAfterDuration() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(SIEGE_DURATION);

        assertTrue(siege.isSiegeExpired());
    }

    @Test
    void bannerInactiveWhenSiegeExpired() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(SIEGE_DURATION);

        assertFalse(siege.isBannerActive());
    }

    @Test
    void timeUntilBannerActiveWhenInactive() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(BANNER_DURATION);

        long expected = BANNER_INTERVAL - BANNER_DURATION;
        assertEquals(expected, siege.getTimeUntilBannerActive());
    }

    @Test
    void timeUntilBannerActiveWhenActive() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        assertEquals(0, siege.getTimeUntilBannerActive());
    }

    @Test
    void remainingBannerTimeWhenActive() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(10 * MINUTE);

        long expected = BANNER_DURATION - (10 * MINUTE);
        assertEquals(expected, siege.getRemainingBannerTime());
    }

    @Test
    void remainingBannerTimeWhenInactive() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(BANNER_DURATION);

        assertEquals(0, siege.getRemainingBannerTime());
    }

    @Test
    void remainingSiegeTime() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

        timeProvider.advance(6 * HOUR);

        long expected = SIEGE_DURATION - (6 * HOUR);
        assertEquals(expected, siege.getRemainingSiegeTime());
    }

    @Test
    void multipleBannerCycles() {
        Territory territory = new Territory();
        siegeManager.startSiege(territory.getId(), attackerId, defenderId);
        Siege siege = siegeManager.getSiege(territory.getId());

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