package com.example.myapplication02.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication02.data.FotoRepository
import com.example.myapplication02.model.AppDatabase
import com.example.myapplication02.model.FotoEntity
import kotlinx.coroutines.launch

class FotoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FotoRepository
    val fotos: LiveData<List<FotoEntity>>

    init {
        val dao = AppDatabase.getInstance(application).fotoDao()
        repository = FotoRepository(dao)
        fotos = repository.getAllFotos()
    }

    fun guardarFoto(foto: FotoEntity) {
        viewModelScope.launch {
            repository.insertFoto(foto)
        }
    }
}

class FotoViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FotoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FotoViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
