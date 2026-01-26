package com.townssiege.hytale.integration;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.buuz135.simpleclaims.claim.party.PartyInfo;
import com.hypixel.hytale.math.util.ChunkUtil;

import java.util.UUID;

public class SimpleClaimsIntegration {

    private static final int SEARCH_RADIUS_BLOCKS = 40;
    private static final int SEARCH_RADIUS_CHUNKS = 3; // ceil(40/16)


    public static ChunkInfo findNearestClaim(String dimension, int blockX, int blockZ) {
        int centerChunkX = ChunkUtil.chunkCoordinate(blockX);
        int centerChunkZ = ChunkUtil.chunkCoordinate(blockZ);

        ChunkInfo nearestClaim = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int cx = centerChunkX - SEARCH_RADIUS_CHUNKS; cx <= centerChunkX + SEARCH_RADIUS_CHUNKS; cx++) {
            for (int cz = centerChunkZ - SEARCH_RADIUS_CHUNKS; cz <= centerChunkZ + SEARCH_RADIUS_CHUNKS; cz++) {
                ChunkInfo claim = ClaimManager.getInstance().getChunk(dimension, cx, cz);
                if (claim != null) {
                    int chunkCenterX = ChunkUtil.minBlock(claim.getChunkX()) + 8;
                    int chunkCenterZ = ChunkUtil.minBlock(claim.getChunkZ()) + 8;

                    double dx = blockX - chunkCenterX;
                    double dz = blockZ - chunkCenterZ;
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    if (distance <= SEARCH_RADIUS_BLOCKS && distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestClaim = claim;
                    }
                }
            }
        }

        return nearestClaim;
    }


    public static UUID getClaimOwner(ChunkInfo claim) {
        if (claim == null) {
            return null;
        }
        PartyInfo party = ClaimManager.getInstance().getPartyById(claim.getPartyOwner());
        return party != null ? party.getOwner() : null;
    }
}