package io.github.arleycht.SMP.util;

public class Cooldown {
    private final long duration;

    private long cooldownEndTime;

    public Cooldown(double seconds) {
        duration = (long) (seconds * 1000.0);
    }

    public void reset() {
        cooldownEndTime = System.currentTimeMillis() + duration;
    }

    public boolean isNotReady() {
        return System.currentTimeMillis() < cooldownEndTime;
    }

    public boolean isReady() {
        return !isNotReady();
    }

    public double getDuration() {
        return duration / 1000.0;
    }
}
