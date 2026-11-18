package com.comp2042;

import org.junit.jupiter.api.Test;
import static com.comp2042.PowerUpState.Effect.*;
import static org.junit.jupiter.api.Assertions.*;

class PowerUpStateTest {
    @Test void effectsExpireIndependentlyAtTheSpecifiedStep() {
        var effects = new PowerUpState();
        effects.activate(WRAP, 2); effects.activate(GAP, 3);
        effects.tick(); assertEquals(1, effects.steps(WRAP));
        effects.tick(); assertFalse(effects.active(WRAP)); assertTrue(effects.active(GAP));
        effects.tick(); assertTrue(effects.snapshot().isEmpty());
    }
    @Test void refreshingOneEffectPreservesOtherEffects() {
        var effects = new PowerUpState(); effects.activate(WRAP, 2); effects.activate(RUSH, 4);
        effects.tick(); effects.activate(WRAP, 48);
        assertEquals(48, effects.steps(WRAP)); assertEquals(3, effects.steps(RUSH));
        assertThrows(IllegalArgumentException.class, () -> effects.activate(WRAP, 0));
        assertThrows(UnsupportedOperationException.class, () -> effects.snapshot().clear());
        effects.clear(); assertTrue(effects.snapshot().isEmpty());
    }
}
