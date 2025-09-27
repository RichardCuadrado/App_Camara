package com.example.myapplication02.data

import androidx.lifecycle.LiveData
import com.example.myapplication02.model.FotoDao
import com.example.myapplication02.model.FotoEntity

class FotoRepository(private val dao: FotoDao) {
    fun getAllFotos(): LiveData<List<FotoEntity>> = dao.getAll()
    suspend fun insertFoto(foto: FotoEntity) = dao.insert(foto)
}
