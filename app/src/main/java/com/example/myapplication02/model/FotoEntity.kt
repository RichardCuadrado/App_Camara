package com.example.myapplication02.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fotos")
data class FotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val latitude: Double?,
    val longitude: Double?,
    val fechaHora: String
)
