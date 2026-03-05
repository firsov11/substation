package com.firsov.substation.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firsov.substation.data.model.Cell
import com.firsov.substation.data.model.Equipment
import com.firsov.substation.data.repository.SubstationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: SubstationRepository
) : ViewModel() {

    val cells: StateFlow<List<Cell>> = repository.cells

    private val _selectedCell = MutableStateFlow<Cell?>(null)
    val selectedCell: StateFlow<Cell?> = _selectedCell

    private val columns = 4
    private val startX = 80f
    private val startY = 120f
    private val stepX = 170f
    private val stepY = 130f

    fun addCell() {
        // Запускаем корутину для работы с БД
        viewModelScope.launch {
            val currentCells = repository.cells.value
            val index = currentCells.size + 1
            val column = (index - 1) % columns
            val row = (index - 1) / columns

            val x = startX + column * stepX
            val y = startY + row * stepY

            val cell = Cell(
                index = index,
                x = x,
                y = y
            )
            repository.addCell(cell)
        }
    }

    fun moveCell(cell: Cell, x: Float, y: Float) {
        viewModelScope.launch {
            // 1. Берем самый свежий список из репозитория
            val currentCells = repository.cells.value
            // 2. Находим в нем нашу ячейку по ID (там точно актуальное оборудование)
            val latestCell = currentCells.find { it.id == cell.id }

            // 3. Если нашли - обновляем только координаты, если нет - используем ту, что пришла
            val updated = (latestCell ?: cell).copy(x = x, y = y)

            repository.updateCell(updated)
        }
    }



    fun addEquipmentToCell(cellId: String, equipment: Equipment) {
        viewModelScope.launch {
            val cell = repository.cells.value.find { it.id == cellId } ?: return@launch
            val updated = cell.copy(equipment = equipment)
            repository.updateCell(updated)
            clearSelectedCell()
        }
    }

    fun deleteCell(cell: Cell) {
        viewModelScope.launch {
            repository.deleteCell(cell)
        }
    }

    fun onCellClicked(cell: Cell) {
        _selectedCell.value = cell
    }

    fun clearSelectedCell() {
        _selectedCell.value = null
    }
}
