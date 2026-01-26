package com.townssiege.hytale.integration;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.buuz135.simpleclaims.claim.party.PartyInfo;
import com.buuz135.simpleclaims.files.DatabaseManager;
import com.hypixel.hytale.math.util.ChunkUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class SimpleClaimsIntegration {

    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_CENTER_OFFSET = CHUNK_SIZE / 2;
    private static final int SEARCH_RADIUS_BLOCKS = 40;
    private static final int SEARCH_RADIUS_CHUNKS = (int) Math.ceil((double) SEARCH_RADIUS_BLOCKS / CHUNK_SIZE);

    public static ChunkInfo findNearestClaim(String dimension, int blockX, int blockZ) {
        int centerChunkX = ChunkUtil.chunkCoordinate(blockX);
        int centerChunkZ = ChunkUtil.chunkCoordinate(blockZ);

        ChunkInfo nearestClaim = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int cx = centerChunkX - SEARCH_RADIUS_CHUNKS; cx <= centerChunkX + SEARCH_RADIUS_CHUNKS; cx++) {
            for (int cz = centerChunkZ - SEARCH_RADIUS_CHUNKS; cz <= centerChunkZ + SEARCH_RADIUS_CHUNKS; cz++) {
                ChunkInfo claim = ClaimManager.getInstance().getChunk(dimension, cx, cz);
                if (claim == null) {
                    continue;
                }

                double distance = distanceToChunkCenter(blockX, blockZ, claim);
                if (distance <= SEARCH_RADIUS_BLOCKS && distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestClaim = claim;
                }
            }
        }

        return nearestClaim;
    }

    private static double distanceToChunkCenter(int blockX, int blockZ, ChunkInfo chunk) {
        int chunkCenterX = ChunkUtil.minBlock(chunk.getChunkX()) + CHUNK_CENTER_OFFSET;
        int chunkCenterZ = ChunkUtil.minBlock(chunk.getChunkZ()) + CHUNK_CENTER_OFFSET;
        double dx = blockX - chunkCenterX;
        double dz = blockZ - chunkCenterZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static UUID getClaimOwner(ChunkInfo claim) {
        if (claim == null) {
            return null;
        }
        PartyInfo party = ClaimManager.getInstance().getPartyById(claim.getPartyOwner());
        return party != null ? party.getOwner() : null;
    }

    public static UUID getPlayerParty(UUID playerId) {
        return ClaimManager.getInstance().getPlayerToParty().get(playerId);
    }

    @SuppressWarnings("unchecked")
    public static boolean transferClaim(String dimension, ChunkInfo claim, UUID newOwnerPartyId) {
        if (claim == null || newOwnerPartyId == null) {
            return false;
        }

        ClaimManager manager = ClaimManager.getInstance();

        PartyInfo targetParty = manager.getPartyById(newOwnerPartyId);
        if (targetParty == null) {
            return false;
        }

        UUID oldPartyId = claim.getPartyOwner();
        if (oldPartyId.equals(newOwnerPartyId)) {
            return false;
        }

        try {
            Field dbField = ClaimManager.class.getDeclaredField("databaseManager");
            dbField.setAccessible(true);
            DatabaseManager databaseManager = (DatabaseManager) dbField.get(manager);

            Field countsField = ClaimManager.class.getDeclaredField("partyClaimCounts");
            countsField.setAccessible(true);
            Map<UUID, Integer> partyClaimCounts = (Map<UUID, Integer>) countsField.get(manager);

            claim.setPartyOwner(newOwnerPartyId);
            databaseManager.saveClaim(dimension, claim);

            partyClaimCounts.computeIfPresent(oldPartyId, (k, v) -> v > 1 ? v - 1 : null);
            partyClaimCounts.merge(newOwnerPartyId, 1, Integer::sum);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}