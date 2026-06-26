package com.example.lifelab.feature.discover.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DiscoverDao {
    @Query("SELECT * FROM discover_content ORDER BY sort_order ASC")
    suspend fun getContent(): List<DiscoverContentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: List<DiscoverContentEntity>)

    @Query("SELECT COUNT(*) FROM discover_content")
    suspend fun countContent(): Int
}
