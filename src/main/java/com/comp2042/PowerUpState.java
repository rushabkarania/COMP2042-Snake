package com.comp2042;

import java.util.EnumMap;
import java.util.Map;

/** Independent effect lifetimes measured in successful movement steps, frozen while paused. */
public final class PowerUpState {
    /** Creates a timer collection with no active effects. */
    public PowerUpState() { }

    /**
     * Independent timed effects available during a run.
     */
    public enum Effect {
        /** Allows travel across board edges. */ WRAP,
        /** Adds a temporary speed increase. */ RUSH,
        /** Requests an opening in the hedge layout. */ GAP
    }
    private final EnumMap<Effect, Integer> remaining = new EnumMap<>(Effect.class);
    /**
     * Activates or refreshes one effect without cancelling any other effect.
     *
     * @param effect effect to activate
     * @param steps positive lifetime measured in successful movement steps
     * @throws IllegalArgumentException if steps is not positive
     */
    public void activate(Effect effect, int steps) {
        if (steps <= 0) throw new IllegalArgumentException("Duration must be positive");
        remaining.put(effect, steps);
    }
    /**
     * Checks whether an effect has movement steps remaining.
     *
     * @param effect effect to query
     * @return true while the effect is active
     */
    public boolean active(Effect effect) { return steps(effect) > 0; }
    /**
     * Returns an effect timer without changing it.
     *
     * @param effect effect to query
     * @return remaining movement steps, or zero if inactive
     */
    public int steps(Effect effect) { return remaining.getOrDefault(effect, 0); }
    /**
     * Copies the active effect timers for display.
     *
     * @return immutable map of effects to remaining steps
     */
    public Map<Effect, Integer> snapshot() { return Map.copyOf(remaining); }
    /** Called after a successful move, before applying any new pickup. */
    public void tick() { remaining.replaceAll((effect, steps) -> steps - 1); remaining.values().removeIf(steps -> steps <= 0); }
    /**
     * Removes all active effects.
     */
    public void clear() { remaining.clear(); }
}
