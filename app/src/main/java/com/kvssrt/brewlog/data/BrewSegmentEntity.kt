package com.kvssrt.brewlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "brew_segments",
    foreignKeys = [
        ForeignKey(
            entity = CoffeeBagEntity::class,
            parentColumns = ["id"],
            childColumns = ["coffeeBagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("coffeeBagId"),
        Index(value = ["coffeeBagId", "brewStyle", "brewer"], unique = true),
    ],
)
data class BrewSegmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coffeeBagId: Long,
    val brewStyle: String,
    val brewer: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
) {
    val label: String
        get() = "$brewStyle - $brewer"
}
