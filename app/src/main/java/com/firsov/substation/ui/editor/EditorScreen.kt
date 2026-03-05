package com.firsov.substation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.firsov.substation.data.model.Cell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel) {
    val cells by viewModel.cells.collectAsState()
    var editingCell by remember { mutableStateOf<Cell?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Электроподстанция") },
                actions = {
                    IconButton(onClick = { viewModel.addCell() }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить ячейку")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFEFEFEF))
        ) {
            // Отображаем ячейки
            cells.forEach { cell ->
                key(cell.id) { // Это гарантирует, что Card сохранит свое состояние
                    CellView(
                        cell = cell,
                        onMove = { movedCell, newX, newY ->
                            viewModel.moveCell(movedCell, newX, newY)
                        },
                        onEdit = { editingCell = it }
                    )
                }
            }

            // Если есть выбранная ячейка для редактирования — открываем EditorCellScreen
            editingCell?.let { cell ->
                EditorCellScreen(
                    cell = cell,
                    onSave = { updatedCell ->
                        // Обязательно вызываем метод ViewModel, который делает repository.updateCell
                        viewModel.addEquipmentToCell(updatedCell.id, updatedCell.equipment!!)
                        editingCell = null
                    },
                    onDelete = { cellToDelete ->
                        viewModel.deleteCell(cellToDelete)
                        editingCell = null
                    },
                    onBack = { editingCell = null }
                )
            }
        }
    }
}