package com.kvssrt.brewlog.data

import java.time.LocalDate
import java.util.UUID

data class BrewLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val brewedOn: LocalDate = LocalDate.now(),
    val coffeeName: String,
    val roaster: String? = null,
    val method: BrewMethod,
    val doseGrams: Double,
    val waterGrams: Double? = null,
    val grindSetting: String? = null,
    val brewTimeSeconds: Int? = null,
    val rating: Int? = null,
    val tastingNotes: String = "",
) {
    val ratio: Double?
        get() = waterGrams?.takeIf { doseGrams > 0 }?.div(doseGrams)
}
