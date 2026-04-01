package com.firsov.substation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "port")
data class Port(
    @PrimaryKey
    val id: String,
    val containerId: String,
    val side: PortSide,
    val voltage: Float,
    val connectedToPortId: String?
)