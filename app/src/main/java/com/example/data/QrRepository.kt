package com.example.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val qrDao: QrDao) {
    val allHistory: Flow<List<QrItemEntity>> = qrDao.getAllHistory()
    val favorites: Flow<List<QrItemEntity>> = qrDao.getFavorites()

    suspend fun getById(id: Long): QrItemEntity? = qrDao.getById(id)

    suspend fun insert(item: QrItemEntity): Long = qrDao.insert(item)

    suspend fun update(item: QrItemEntity) = qrDao.update(item)

    suspend fun delete(item: QrItemEntity) = qrDao.delete(item)

    suspend fun deleteById(id: Long) = qrDao.deleteById(id)

    suspend fun clearHistory() = qrDao.clearHistory()

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = qrDao.updateFavorite(id, isFav)
}
