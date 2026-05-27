package com.kvssrt.brewlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffee_bags")
data class CoffeeBagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val roaster: String = "",
    val beanDetails: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)
