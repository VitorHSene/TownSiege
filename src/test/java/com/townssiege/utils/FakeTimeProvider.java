package com.townssiege.utils;

public class FakeTimeProvider implements TimeProvider {

    private long currentTime;

    public FakeTimeProvider() {
        this.currentTime = 0;
    }

    public FakeTimeProvider(long initialTime) {
        this.currentTime = initialTime;
    }

    @Override
    public long now() {
        return currentTime;
    }

    public void setTime(long time) {
        this.currentTime = time;
    }

    public void advance(long millis) {
        this.currentTime += millis;
    }
}