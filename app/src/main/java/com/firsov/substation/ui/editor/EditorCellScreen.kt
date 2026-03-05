package com.firsov.substation.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.firsov.substation.data.model.*

@Composable
fun EditorCellScreen(
    cell: Cell,
    onSave: (Cell) -> Unit,
    onDelete: (Cell) -> Unit,
    onBack: () -> Unit
) {

    val scrollState = rememberScrollState()

    var selectedEquipment by remember { mutableStateOf(cell.equipment) }
    var dispatcherName by remember { mutableStateOf(cell.equipment?.dispatcherName ?: "") }

    val breakerVoltages = listOf(330f, 110f, 10f)
    val disconnectorVoltages = listOf(330f, 110f, 10f)
    val transformerOptions = listOf(
        listOf(330f, 110f, 10f),
        listOf(110f, 35f, 10f),
        listOf(10f, 0.4f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Редактор ячейки: ${cell.id}",
            style = MaterialTheme.typography.titleMedium
        )

        Text("Выключатели")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            breakerVoltages.forEach { voltage ->
                Button(
                    onClick = { selectedEquipment = Breaker(voltage = voltage) }
                ) {
                    Text("${voltage.toInt()} кВ")
                }
            }
        }

        Text("Разъединители")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            disconnectorVoltages.forEach { voltage ->
                Button(
                    onClick = { selectedEquipment = Disconnector(voltage = voltage) }
                ) {
                    Text("${voltage.toInt()} кВ")
                }
            }
        }

        Text("Трансформаторы")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transformerOptions.forEach { windings ->
                Button(
                    onClick = { selectedEquipment = Transformer(windings = windings) }
                ) {
                    Text(windings.joinToString(" / "))
                }
            }
        }

        if (selectedEquipment != null) {

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = dispatcherName,
                onValueChange = { dispatcherName = it },
                label = { Text("Диспетчерское имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            Button(onClick = {
                // Создаем НОВЫЙ объект оборудования с обновленным именем
                val finalEquipment = when (val eq = selectedEquipment) {
                    is Breaker -> eq.copy(dispatcherName = dispatcherName)
                    is Disconnector -> eq.copy(dispatcherName = dispatcherName)
                    is Transformer -> eq.copy(dispatcherName = dispatcherName)
                    else -> null
                }

                if (finalEquipment != null) {
                    // Передаем в onSave ячейку с НОВЫМ объектом оборудования
                    onSave(cell.copy(equipment = finalEquipment))
                    onBack()
                }
            })
            {
                Text("Сохранить")
            }


            Button(
                onClick = {
                    onDelete(cell)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Удалить")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onBack) {
            Text("Назад")
        }
    }
}