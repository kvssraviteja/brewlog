package com.kvssrt.brewlog.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BrewlogRepositoryTest {
    @Test
    fun pourLogRatioUsesWaterDividedByDose() {
        val log = PourLogEntity(
            coffeeBagId = 1,
            segmentId = 1,
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
                roastDate = "",
                beanDetails = "",
                imagePath = "",
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun coffeeBagValidationRejectsInvalidRoastDate() {
        validateCoffeeBagDraft(
            CoffeeBagDraft(
                name = "Test coffee",
                roaster = "",
                roastDate = "May 1",
                beanDetails = "",
                imagePath = "",
            ),
        )
    }

    @Test
    fun coffeeBagValidationAllowsIsoRoastDate() {
        validateCoffeeBagDraft(
            CoffeeBagDraft(
                name = "Test coffee",
                roaster = "",
                roastDate = "2026-05-01",
                beanDetails = "",
                imagePath = "",
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun pourLogValidationRequiresBrewStyle() {
        validatePourLogDraft(validPourLogDraft(brewStyle = " "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun pourLogValidationRequiresBrewer() {
        validatePourLogDraft(validPourLogDraft(brewer = " "))
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
        brewStyle: String = "Pour over",
        brewer: String = "B75",
        doseGrams: Double = 20.0,
        waterGrams: Double = 320.0,
    ) = PourLogDraft(
        coffeeBagId = 1,
        brewStyle = brewStyle,
        brewer = brewer,
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
