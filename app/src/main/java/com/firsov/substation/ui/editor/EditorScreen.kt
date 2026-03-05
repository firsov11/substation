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
fun EditorScreen(
    viewModel: EditorViewModel,
    onEditCell: (Cell) -> Unit // Используем этот колбэк для перехода
) {
    val cells by viewModel.cells.collectAsState()

    val gridSize = 40f
    val gridColor = Color.LightGray.copy(alpha = 0.5f)

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
            // Сетка
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                for (x in 0..size.width.toInt() step gridSize.toInt()) {
                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(x.toFloat(), 0f), androidx.compose.ui.geometry.Offset(x.toFloat(), size.height), 1f)
                }
                for (y in 0..size.height.toInt() step gridSize.toInt()) {
                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y.toFloat()), androidx.compose.ui.geometry.Offset(size.width, y.toFloat()), 1f)
                }
            }

            // Ячейки
            cells.forEach { cell ->
                key(cell.id) {
                    CellView(
                        cell = cell,
                        onMove = { movedCell, newX, newY ->
                            val snappedX = Math.round(newX / gridSize) * gridSize
                            val snappedY = Math.round(newY / gridSize) * gridSize
                            viewModel.moveCell(movedCell, snappedX, snappedY)
                        },
                        onEdit = { onEditCell(it) } // Вызываем навигацию вместо локального стейта
                    )
                }
            }
        }
    }
}

