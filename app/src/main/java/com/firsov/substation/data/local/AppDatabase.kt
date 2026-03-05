package com.firsov.substation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [com.firsov.substation.data.model.Cell::class], // Прямой путь
    version = 1,
    exportSchema = false
)

@TypeConverters(EquipmentConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cellDao(): CellDao
}