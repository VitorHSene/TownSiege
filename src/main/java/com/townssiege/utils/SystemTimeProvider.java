package com.townssiege.utils;

public class SystemTimeProvider implements TimeProvider {
    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
