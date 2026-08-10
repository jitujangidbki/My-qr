package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {
    @Query("SELECT * FROM qr_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<QrItemEntity>>

    @Query("SELECT * FROM qr_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<QrItemEntity>>

    @Query("SELECT * FROM qr_history WHERE id = :id")
    suspend fun getById(id: Long): QrItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QrItemEntity): Long

    @Update
    suspend fun update(item: QrItemEntity)

    @Delete
    suspend fun delete(item: QrItemEntity)

    @Query("DELETE FROM qr_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM qr_history")
    suspend fun clearHistory()

    @Query("UPDATE qr_history SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)
}
