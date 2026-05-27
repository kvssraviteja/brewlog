package com.kvssrt.brewlog.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BrewlogRepositoryTest {
    @Test
    fun pourLogRatioUsesWaterDividedByDose() {
        val log = PourLogEntity(
            coffeeBagId = 1,
            methodId = 1,
            brewedOn = LocalDate.now(),
            doseGrams = 20.0,
            waterGrams = 320.0,
        )

        assertEquals(16.0, log.ratio(), 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun coffeeBagValidationRequiresName() {
        validateCoffeeBagDraft(
            CoffeeBagDraft(
                name = " ",
                roaster = "",
                beanDetails = "",
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun pourLogValidationRequiresMethodName() {
        validatePourLogDraft(validPourLogDraft(methodName = " "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun pourLogValidationRequiresPositiveDose() {
        validatePourLogDraft(validPourLogDraft(doseGrams = 0.0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun pourLogValidationRequiresPositiveWater() {
        validatePourLogDraft(validPourLogDraft(waterGrams = 0.0))
    }

    private fun validPourLogDraft(
        methodName: String = "V60",
        doseGrams: Double = 20.0,
        waterGrams: Double = 320.0,
    ) = PourLogDraft(
        coffeeBagId = 1,
        methodName = methodName,
        brewedOn = LocalDate.now(),
        doseGrams = doseGrams,
        waterGrams = waterGrams,
        equipmentDetails = "",
        grinderDetails = "",
        grindSize = "",
        recipe = "",
        waterDetails = "",
        tastingNotes = "",
        nextImprovements = "",
        rating = null,
        brewTimeSeconds = null,
    )
}
