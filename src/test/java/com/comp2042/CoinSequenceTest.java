package com.comp2042;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import static com.comp2042.CoinSequence.Type.*;
import static org.junit.jupiter.api.Assertions.*;

class CoinSequenceTest {
    @Test
    void everyGroupIncludesEachPowerUpAndEndsWithSilver() {
        var sequence = new CoinSequence();
        for (int group = 0; group < 20; group++) {
            var draws = new ArrayList<CoinSequence.Type>();
            for (int i = 0; i < 7; i++) draws.add(sequence.next());
            assertEquals(4, Collections.frequency(draws, RED));
            for (var special : new CoinSequence.Type[]{GOLD, BLUE, RAINBOW}) {
                assertEquals(1, Collections.frequency(draws, special));
            }
            assertEquals(SILVER, sequence.next());
        }
    }

    @Test
    void resetRestartsSilverCountdown() {
        var sequence = new CoinSequence();
        for (int i = 0; i < 7; i++) sequence.next();
        sequence.reset();
        for (int i = 0; i < 7; i++) assertNotEquals(SILVER, sequence.next());
        assertEquals(SILVER, sequence.next());
    }
}
