package com.firsov.substation.data.local

import androidx.room.TypeConverter
import com.firsov.substation.data.model.PortSide

class PortSideConverter {

    @TypeConverter
    fun fromPortSide(side: PortSide): String = side.name

    @TypeConverter
    fun toPortSide(value: String): PortSide = PortSide.valueOf(value)
}