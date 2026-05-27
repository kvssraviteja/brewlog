package com.kvssrt.brewlog.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BrewLogEntryTest {
    @Test
    fun ratioUsesWaterDividedByDose() {
        val entry = BrewLogEntry(
            coffeeName = "Test coffee",
            method = BrewMethod.PourOver,
            doseGrams = 20.0,
            waterGrams = 320.0,
        )

        assertEquals(16.0, entry.ratio ?: 0.0, 0.0)
    }

    @Test
    fun ratioIsNullWithoutWater() {
        val entry = BrewLogEntry(
            coffeeName = "Test coffee",
            method = BrewMethod.Espresso,
            doseGrams = 18.0,
        )

        assertEquals(null, entry.ratio)
    }
}
