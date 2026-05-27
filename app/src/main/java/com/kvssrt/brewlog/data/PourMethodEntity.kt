package com.kvssrt.brewlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pour_methods",
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
        Index(value = ["coffeeBagId", "name"], unique = true),
    ],
)
data class PourMethodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coffeeBagId: Long,
    val name: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
