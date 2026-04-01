package com.firsov.substation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.firsov.substation.data.model.Container
import com.firsov.substation.data.model.Port

@Database(
    entities = [
        Container::class,
        Port::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    EquipmentConverter::class,
    PortSideConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun containerDao(): ContainerDao
    abstract fun portDao(): PortDao
}