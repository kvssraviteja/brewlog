package com.kvssrt.brewlog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class CoffeeBagSummary(
    val id: Long,
    val name: String,
    val roaster: String,
    val beanDetails: String,
    val logCount: Long,
    val latestBrewedOn: String?,
)

@Dao
interface BrewlogDao {
    @Query(
        """
        SELECT coffee_bags.id,
               coffee_bags.name,
               coffee_bags.roaster,
               coffee_bags.beanDetails,
               COUNT(pour_logs.id) AS logCount,
               MAX(pour_logs.brewedOn) AS latestBrewedOn
        FROM coffee_bags
        LEFT JOIN pour_logs ON pour_logs.coffeeBagId = coffee_bags.id
        GROUP BY coffee_bags.id
        ORDER BY MAX(pour_logs.brewedOn) DESC, coffee_bags.createdAtMillis DESC
        """,
    )
    fun observeCoffeeBags(): Flow<List<CoffeeBagSummary>>

    @Query("SELECT * FROM coffee_bags WHERE id = :id")
    fun observeCoffeeBag(id: Long): Flow<CoffeeBagEntity?>

    @Query("SELECT * FROM pour_methods WHERE coffeeBagId = :coffeeBagId ORDER BY name")
    fun observeMethods(coffeeBagId: Long): Flow<List<PourMethodEntity>>

    @Query(
        """
        SELECT * FROM pour_logs
        WHERE coffeeBagId = :coffeeBagId AND methodId = :methodId
        ORDER BY brewedOn DESC, createdAtMillis DESC
        """,
    )
    fun observeLogs(coffeeBagId: Long, methodId: Long): Flow<List<PourLogEntity>>

    @Query("SELECT * FROM pour_methods WHERE coffeeBagId = :coffeeBagId AND name = :name LIMIT 1")
    suspend fun findMethod(coffeeBagId: Long, name: String): PourMethodEntity?

    @Insert
    suspend fun insertCoffeeBag(coffeeBag: CoffeeBagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMethod(method: PourMethodEntity): Long

    @Insert
    suspend fun insertLog(log: PourLogEntity): Long
}
