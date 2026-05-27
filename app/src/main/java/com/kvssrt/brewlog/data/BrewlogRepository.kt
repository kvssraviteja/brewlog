package com.kvssrt.brewlog.data

import java.time.LocalDate

val presetPourMethods = listOf(
    "V60",
    "Chemex",
    "Kalita Wave",
    "Origami",
    "April brewer",
    "Flat-bottom",
    "Cone brewer",
)

data class CoffeeBagDraft(
    val name: String,
    val roaster: String,
    val beanDetails: String,
)

data class PourLogDraft(
    val coffeeBagId: Long,
    val methodName: String,
    val brewedOn: LocalDate,
    val doseGrams: Double,
    val waterGrams: Double,
    val equipmentDetails: String,
    val grinderDetails: String,
    val grindSize: String,
    val recipe: String,
    val waterDetails: String,
    val tastingNotes: String,
    val nextImprovements: String,
    val rating: Int?,
    val brewTimeSeconds: Int?,
)

class BrewlogRepository(
    private val dao: BrewlogDao,
) {
    fun observeCoffeeBags() = dao.observeCoffeeBags()

    fun observeCoffeeBag(id: Long) = dao.observeCoffeeBag(id)

    fun observeMethods(coffeeBagId: Long) = dao.observeMethods(coffeeBagId)

    fun observeLogs(coffeeBagId: Long, methodId: Long) = dao.observeLogs(coffeeBagId, methodId)

    suspend fun addCoffeeBag(draft: CoffeeBagDraft): Long {
        validateCoffeeBagDraft(draft)

        return dao.insertCoffeeBag(
            CoffeeBagEntity(
                name = draft.name.trim(),
                roaster = draft.roaster.trim(),
                beanDetails = draft.beanDetails.trim(),
            ),
        )
    }

    suspend fun addPourLog(draft: PourLogDraft): Long {
        validatePourLogDraft(draft)

        val method = getOrCreateMethod(draft.coffeeBagId, draft.methodName.trim())
        return dao.insertLog(
            PourLogEntity(
                coffeeBagId = draft.coffeeBagId,
                methodId = method.id,
                brewedOn = draft.brewedOn,
                doseGrams = draft.doseGrams,
                waterGrams = draft.waterGrams,
                equipmentDetails = draft.equipmentDetails.trim(),
                grinderDetails = draft.grinderDetails.trim(),
                grindSize = draft.grindSize.trim(),
                recipe = draft.recipe.trim(),
                waterDetails = draft.waterDetails.trim(),
                tastingNotes = draft.tastingNotes.trim(),
                nextImprovements = draft.nextImprovements.trim(),
                rating = draft.rating,
                brewTimeSeconds = draft.brewTimeSeconds,
            ),
        )
    }

    private suspend fun getOrCreateMethod(coffeeBagId: Long, name: String): PourMethodEntity {
        dao.findMethod(coffeeBagId, name)?.let { return it }

        val id = dao.insertMethod(PourMethodEntity(coffeeBagId = coffeeBagId, name = name))
        if (id > 0) {
            return PourMethodEntity(id = id, coffeeBagId = coffeeBagId, name = name)
        }

        return dao.findMethod(coffeeBagId, name)
            ?: error("Unable to create pour method.")
    }
}

internal fun validateCoffeeBagDraft(draft: CoffeeBagDraft) {
    require(draft.name.trim().isNotEmpty()) { "Coffee name is required." }
}

internal fun validatePourLogDraft(draft: PourLogDraft) {
    require(draft.methodName.trim().isNotEmpty()) { "Pour method is required." }
    require(draft.doseGrams > 0) { "Coffee dose must be greater than 0." }
    require(draft.waterGrams > 0) { "Water amount must be greater than 0." }
}
