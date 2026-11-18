package com.comp2042;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Chooses coin types for one round when power-ups are enabled. */
final class CoinSequence {
    /** Creates an empty sequence ready to generate its first group. */
    CoinSequence() { }

    /**
     * Collectible types used by the shuffled sequence.
     */
    enum Type {
        /** Ordinary score pickup. */ RED,
        /** Bonus-score pickup. */ GOLD,
        /** Border-wrap pickup. */ BLUE,
        /** Rush pickup. */ RAINBOW,
        /** Hedge-gate pickup. */ SILVER,
        /** Tail-shortening pickup. */ SHRINK
    }

    private final List<Type> remaining = new ArrayList<>();

    /**
     * Returns the next type from shuffled eight-pickup groups, each ending with silver.
     *
     * @return next collectible type
     */
    Type next() {
        if (remaining.isEmpty()) {
            remaining.add(Type.SILVER);
            List<Type> others = new ArrayList<>(List.of(Type.RED, Type.RED, Type.RED,
                    Type.SHRINK, Type.GOLD, Type.BLUE, Type.RAINBOW));
            Collections.shuffle(others);
            remaining.addAll(others);
        }
        return remaining.remove(remaining.size() - 1);
    }

    /** Discards the previous round's partially used group. */
    void reset() {
        remaining.clear();
    }
}
