package org.pipeman.bb.utils;

public class Interval {
    private final int INTERVAL;
    private int cooldown;

    public Interval(int interval) {
        this.INTERVAL = interval;
        cooldown = interval;
    }

    public boolean tick() {
        if (cooldown-- <= 0) {
            cooldown = INTERVAL;
            return true;
        }
        return false;
    }
}
