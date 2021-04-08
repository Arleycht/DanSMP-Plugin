package io.github.arleycht.SMP.util;

public class Cooldown {
    private final long durationMilliseconds;

    private long cooldownEndTime;

    public Cooldown(double seconds) {
        durationMilliseconds = (long) (seconds * 1000.0);
    }

    public void reset() {
        cooldownEndTime = System.currentTimeMillis() + durationMilliseconds;
    }

    public boolean isNotReady() {
        return System.currentTimeMillis() < cooldownEndTime;
    }

    public boolean isReady() {
        return !isNotReady();
    }

    public double getDurationSeconds() {
        return durationMilliseconds / 1000.0;
    }

    public long getDurationTicks() {
        return (long) (durationMilliseconds * 20.0 / 1000.0);
    }
}
