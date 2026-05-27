package com.kvssrt.brewlog.data

import java.time.LocalDate

val sampleBrews = listOf(
    BrewLogEntry(
        brewedOn = LocalDate.now(),
        coffeeName = "Ethiopia Guji",
        roaster = "Home shelf",
        method = BrewMethod.PourOver,
        doseGrams = 20.0,
        waterGrams = 320.0,
        grindSetting = "18 clicks",
        brewTimeSeconds = 185,
        rating = 4,
        tastingNotes = "Bright, peachy, light tea finish.",
    ),
    BrewLogEntry(
        brewedOn = LocalDate.now().minusDays(1),
        coffeeName = "House espresso",
        method = BrewMethod.Espresso,
        doseGrams = 18.0,
        waterGrams = 40.0,
        grindSetting = "Fine",
        brewTimeSeconds = 29,
        rating = 5,
        tastingNotes = "Balanced chocolate and orange.",
    ),
)
