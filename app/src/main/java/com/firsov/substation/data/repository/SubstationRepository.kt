package com.firsov.substation.data.repository

import com.firsov.substation.data.local.CellDao
import com.firsov.substation.data.model.Cell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubstationRepository @Inject constructor(
    private val cellDao: CellDao // Инжектится через Hilt
) {
    // Room сам возвращает Flow, который обновляется при изменениях в БД
    val cells: StateFlow<List<Cell>> = cellDao.getAllCells()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun addCell(cell: Cell) = cellDao.insertOrUpdate(cell)

    suspend fun updateCell(cell: Cell) = cellDao.insertOrUpdate(cell)

    suspend fun deleteCell(cell: Cell) = cellDao.delete(cell)
}
