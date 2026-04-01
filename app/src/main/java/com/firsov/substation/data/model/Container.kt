package com.firsov.substation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.firsov.substation.config.ContainerConfig

@Entity(tableName = "container")
data class Container(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Int = 0,
    val equipment: Equipment? = null,
    val width: Float = ContainerConfig.WIDTH,
    val height: Float = ContainerConfig.HEIGHT
)

