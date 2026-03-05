package com.firsov.substation.data.model

import java.util.UUID

data class Connection(

    val id: String = UUID.randomUUID().toString(),

    val fromCellId: String,
    val toCellId: String
)