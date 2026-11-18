package com.comp2042;

import java.util.EnumMap;
import java.util.Map;

/** Independent effect lifetimes measured in successful movement steps, frozen while paused. */
public final class PowerUpState {
    public enum Effect { WRAP, RUSH, GAP }
    private final EnumMap<Effect, Integer> remaining = new EnumMap<>(Effect.class);
    /** Activates or refreshes one effect without cancelling the others. */
    public void activate(Effect effect, int steps) {
        if (steps <= 0) throw new IllegalArgumentException("Duration must be positive");
        remaining.put(effect, steps);
    }
    public boolean active(Effect effect) { return steps(effect) > 0; }
    public int steps(Effect effect) { return remaining.getOrDefault(effect, 0); }
    public Map<Effect, Integer> snapshot() { return Map.copyOf(remaining); }
    /** Called after a successful move, before applying any new pickup. */
    public void tick() { remaining.replaceAll((effect, steps) -> steps - 1); remaining.values().removeIf(steps -> steps <= 0); }
    public void clear() { remaining.clear(); }
}
