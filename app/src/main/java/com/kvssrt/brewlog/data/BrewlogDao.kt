package com.kvssrt.brewlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CoffeeBagSummary(
    val id: Long,
    val name: String,
    val roaster: String,
    val roastDate: String,
    val beanDetails: String,
    val imagePath: String,
    val logCount: Long,
    val latestBrewedOn: String?,
)

data class BrewSegmentSummary(
    val id: Long,
    val coffeeBagId: Long,
    val brewStyle: String,
    val brewer: String,
    val logCount: Long,
    val latestBrewedOn: String?,
) {
    val label: String
        get() = "$brewStyle - $brewer"
}

@Dao
interface BrewlogDao {
    @Query(
        """
        SELECT coffee_bags.id,
               coffee_bags.name,
               coffee_bags.roaster,
               coffee_bags.roastDate,
               coffee_bags.beanDetails,
               coffee_bags.imagePath,
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

    @Query(
        """
        SELECT brew_segments.id,
               brew_segments.coffeeBagId,
               brew_segments.brewStyle,
               brew_segments.brewer,
               COUNT(pour_logs.id) AS logCount,
               MAX(pour_logs.brewedOn) AS latestBrewedOn
        FROM brew_segments
        INNER JOIN pour_logs ON pour_logs.segmentId = brew_segments.id
        WHERE brew_segments.coffeeBagId = :coffeeBagId
        GROUP BY brew_segments.id
        ORDER BY MAX(pour_logs.brewedOn) DESC, brew_segments.brewStyle, brew_segments.brewer
        """,
    )
    fun observeSegmentSummaries(coffeeBagId: Long): Flow<List<BrewSegmentSummary>>

    @Query("SELECT * FROM brew_segments WHERE coffeeBagId = :coffeeBagId ORDER BY brewStyle, brewer")
    fun observeAllSegments(coffeeBagId: Long): Flow<List<BrewSegmentEntity>>

    @Query(
        """
        SELECT * FROM pour_logs
        WHERE coffeeBagId = :coffeeBagId AND segmentId = :segmentId
        ORDER BY brewedOn DESC, createdAtMillis DESC
        """,
    )
    fun observeLogs(coffeeBagId: Long, segmentId: Long): Flow<List<PourLogEntity>>

    @Query("SELECT * FROM pour_logs WHERE id = :id")
    fun observeLog(id: Long): Flow<PourLogEntity?>

    @Query(
        """
        SELECT * FROM brew_segments
        WHERE coffeeBagId = :coffeeBagId AND brewStyle = :brewStyle AND brewer = :brewer
        LIMIT 1
        """,
    )
    suspend fun findSegment(coffeeBagId: Long, brewStyle: String, brewer: String): BrewSegmentEntity?

    @Insert
    suspend fun insertCoffeeBag(coffeeBag: CoffeeBagEntity): Long

    @Update
    suspend fun updateCoffeeBag(coffeeBag: CoffeeBagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSegment(segment: BrewSegmentEntity): Long

    @Insert
    suspend fun insertLog(log: PourLogEntity): Long

    @Update
    suspend fun updateLog(log: PourLogEntity)

    @Delete
    suspend fun deleteLog(log: PourLogEntity)

    @Delete
    suspend fun deleteSegment(segment: BrewSegmentEntity)
}
