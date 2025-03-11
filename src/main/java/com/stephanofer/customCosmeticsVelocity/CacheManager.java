package com.stephanofer.customCosmeticsVelocity;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.UUID;

public class CacheManager {


    private final Cache<UUID, PlayerPrefixData> cache;

    public CacheManager() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(500)
                .build();
    }

    public void updatePlayerPrefix(UUID uuid, PlayerPrefixData playerPrefixData) {
        cache.put(uuid, playerPrefixData);
    }

    public PlayerPrefixData getPlayerPrefix(UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    public void invalidatePlayerPrefix(UUID uuid) {
        cache.invalidate(uuid);
    }

    public boolean hasPlayerPrefixInCache(UUID uuid) {
        return cache.getIfPresent(uuid) != null;
    }
}
