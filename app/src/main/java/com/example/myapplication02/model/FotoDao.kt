package com.example.myapplication02.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FotoDao {
    @Insert
    suspend fun insert(foto: FotoEntity)

    @Query("SELECT * FROM fotos ORDER BY id DESC")
    fun getAll(): LiveData<List<FotoEntity>>
}
