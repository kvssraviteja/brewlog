package com.kvssrt.brewlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "pour_logs",
    foreignKeys = [
        ForeignKey(
            entity = CoffeeBagEntity::class,
            parentColumns = ["id"],
            childColumns = ["coffeeBagId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PourMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["methodId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("coffeeBagId"),
        Index("methodId"),
        Index(value = ["coffeeBagId", "methodId", "brewedOn"]),
    ],
)
data class PourLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coffeeBagId: Long,
    val methodId: Long,
    val brewedOn: LocalDate,
    val doseGrams: Double,
    val waterGrams: Double,
    val equipmentDetails: String = "",
    val grinderDetails: String = "",
    val grindSize: String = "",
    val recipe: String = "",
    val waterDetails: String = "",
    val tastingNotes: String = "",
    val nextImprovements: String = "",
    val rating: Int? = null,
    val brewTimeSeconds: Int? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    fun ratio(): Double = waterGrams / doseGrams
}
