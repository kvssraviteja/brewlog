package com.kvssrt.brewlog.data

import java.time.LocalDate

val brewStyles = listOf(
    "Pour over",
    "Espresso",
    "Other",
)

val presetBrewers = listOf(
    "B75",
    "V60",
    "Chemex",
    "Kalita Wave",
    "Origami",
    "Espresso machine",
    "Other",
)

data class CoffeeBagDraft(
    val id: Long = 0,
    val name: String,
    val roaster: String,
    val roastDate: String,
    val beanDetails: String,
    val imagePath: String,
)

data class PourLogDraft(
    val id: Long = 0,
    val coffeeBagId: Long,
    val brewStyle: String,
    val brewer: String,
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

    fun observeSegmentSummaries(coffeeBagId: Long) = dao.observeSegmentSummaries(coffeeBagId)

    fun observeAllSegments(coffeeBagId: Long) = dao.observeAllSegments(coffeeBagId)

    fun observeLogs(coffeeBagId: Long, segmentId: Long) = dao.observeLogs(coffeeBagId, segmentId)

    fun observeLog(id: Long) = dao.observeLog(id)

    suspend fun addCoffeeBag(draft: CoffeeBagDraft): Long {
        validateCoffeeBagDraft(draft)

        return dao.insertCoffeeBag(
            CoffeeBagEntity(
                name = draft.name.trim(),
                roaster = draft.roaster.trim(),
                roastDate = draft.roastDate.trim(),
                beanDetails = draft.beanDetails.trim(),
                imagePath = draft.imagePath.trim(),
            ),
        )
    }

    suspend fun updateCoffeeBag(existing: CoffeeBagEntity, draft: CoffeeBagDraft) {
        validateCoffeeBagDraft(draft)

        dao.updateCoffeeBag(
            existing.copy(
                name = draft.name.trim(),
                roaster = draft.roaster.trim(),
                roastDate = draft.roastDate.trim(),
                beanDetails = draft.beanDetails.trim(),
                imagePath = draft.imagePath.trim(),
            ),
        )
    }

    suspend fun addPourLog(draft: PourLogDraft): Long {
        validatePourLogDraft(draft)

        val segment = getOrCreateSegment(
            coffeeBagId = draft.coffeeBagId,
            brewStyle = draft.brewStyle.trim(),
            brewer = draft.brewer.trim(),
        )
        return dao.insertLog(
            PourLogEntity(
                coffeeBagId = draft.coffeeBagId,
                segmentId = segment.id,
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

    suspend fun updatePourLog(existing: PourLogEntity, draft: PourLogDraft) {
        validatePourLogDraft(draft)

        val segment = getOrCreateSegment(
            coffeeBagId = draft.coffeeBagId,
            brewStyle = draft.brewStyle.trim(),
            brewer = draft.brewer.trim(),
        )
        dao.updateLog(
            existing.copy(
                coffeeBagId = draft.coffeeBagId,
                segmentId = segment.id,
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

    suspend fun deletePourLog(log: PourLogEntity) {
        dao.deleteLog(log)
    }

    suspend fun deleteBrewSegment(segment: BrewSegmentEntity) {
        dao.deleteSegment(segment)
    }

    private suspend fun getOrCreateSegment(
        coffeeBagId: Long,
        brewStyle: String,
        brewer: String,
    ): BrewSegmentEntity {
        dao.findSegment(coffeeBagId, brewStyle, brewer)?.let { return it }

        val id = dao.insertSegment(
            BrewSegmentEntity(
                coffeeBagId = coffeeBagId,
                brewStyle = brewStyle,
                brewer = brewer,
            ),
        )
        if (id > 0) {
            return BrewSegmentEntity(
                id = id,
                coffeeBagId = coffeeBagId,
                brewStyle = brewStyle,
                brewer = brewer,
            )
        }

        return dao.findSegment(coffeeBagId, brewStyle, brewer)
            ?: error("Unable to create brew segment.")
    }
}

internal fun validateCoffeeBagDraft(draft: CoffeeBagDraft) {
    require(draft.name.trim().isNotEmpty()) { "Coffee name is required." }
    require(draft.roastDate.isBlank() || runCatching { LocalDate.parse(draft.roastDate.trim()) }.isSuccess) {
        "Roast date must use YYYY-MM-DD."
    }
}

internal fun validatePourLogDraft(draft: PourLogDraft) {
    require(draft.brewStyle.trim().isNotEmpty()) { "Brew style is required." }
    require(draft.brewer.trim().isNotEmpty()) { "Brewer is required." }
    require(draft.doseGrams > 0) { "Coffee dose must be greater than 0." }
    require(draft.waterGrams > 0) { "Water amount must be greater than 0." }
}
