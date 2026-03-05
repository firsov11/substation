package com.firsov.substation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cells")
data class Cell(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(), // ОБЯЗАТЕЛЬНО добавить это
    val index: Int,
    val x: Float,
    val y: Float,
    val equipment: Equipment? = null
)

