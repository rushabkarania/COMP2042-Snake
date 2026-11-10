package com.comp2042;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Chooses coin types for one round when power-ups are enabled. */
final class CoinSequence {
    enum Type { RED, GOLD, BLUE, RAINBOW, SILVER }

    private final List<Type> remaining = new ArrayList<>();

    /** Returns the next type. Each group of eight ends with a silver coin. */
    Type next() {
        if (remaining.isEmpty()) {
            remaining.add(Type.SILVER);
            List<Type> others = new ArrayList<>(List.of(Type.RED, Type.RED, Type.RED,
                    Type.RED, Type.GOLD, Type.BLUE, Type.RAINBOW));
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
