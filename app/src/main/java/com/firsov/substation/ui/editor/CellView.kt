package com.firsov.substation.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.firsov.substation.data.model.Cell
import kotlin.math.roundToInt

@Composable
fun CellView(
    cell: Cell,
    onMove: (cell: Cell, newX: Float, newY: Float) -> Unit,
    onEdit: (cell: Cell) -> Unit
) {
    // Локальное состояние для плавности анимации (чтобы не ждать ответа от репозитория)
    var posX by remember { mutableStateOf(cell.x) }
    var posY by remember { mutableStateOf(cell.y) }

    // Синхронизация, если координаты изменились извне
    LaunchedEffect(cell.x, cell.y) {
        posX = cell.x
        posY = cell.y
    }

    Card(
        modifier = Modifier
            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
            .width(140.dp)
            .height(110.dp)
            .pointerInput(cell.id) { // Используйте ID как ключ
                detectDragGestures(
                    onDragEnd = { onMove(cell, posX, posY) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        posX += dragAmount.x
                        posY += dragAmount.y
                    }
                )
            }

            .clickable { onEdit(cell) }, // Клик для редактирования
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Ячейка №${cell.index}", style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(4.dp))

            // Отображение иконки или названия оборудования
            cell.equipment?.let { eq ->
                Text(
                    text = eq.dispatcherName.ifEmpty { eq.name },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Blue
                )
            } ?: Text("Пусто", color = Color.Gray)
        }
    }
}