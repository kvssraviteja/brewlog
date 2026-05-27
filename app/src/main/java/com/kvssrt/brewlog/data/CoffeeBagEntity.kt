package com.kvssrt.brewlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffee_bags")
data class CoffeeBagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val roaster: String = "",
    val roastDate: String = "",
    val beanDetails: String = "",
    val imagePath: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)
