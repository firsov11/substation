package com.firsov.substation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.firsov.substation.data.model.Breaker
import com.firsov.substation.data.model.Cell
import com.firsov.substation.data.model.Disconnector
import com.firsov.substation.data.model.Equipment
import com.firsov.substation.data.model.Transformer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorCellScreen(
    cell: Cell,
    index: Int,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ячейка № ${index + 1}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Используем обычный ArrowBack без AutoMirrored для надежности
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onDelete(cell)
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = Color.Red
                        )
                    }
                }
            )
        }
        ,
        bottomBar = {
            // Кнопка сохранить всегда внизу
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        val finalEquipment = when (val eq = selectedEquipment) {
                            is Breaker -> eq.copy(dispatcherName = dispatcherName)
                            is Disconnector -> eq.copy(dispatcherName = dispatcherName)
                            is Transformer -> eq.copy(dispatcherName = dispatcherName)
                            else -> null
                        }
                        if (finalEquipment != null) {
                            onSave(cell.copy(equipment = finalEquipment))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Сохранить")
                }
            }
        }
    ) { padding ->
        // Светлый непрозрачный фон для всего контента
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle("Выключатели")
                EquipmentChips(
                    voltages = breakerVoltages,
                    selectedVoltage = (selectedEquipment as? Breaker)?.voltage, // Теперь это второй аргумент
                    creator = { voltage -> Breaker(voltage = voltage) },       // Явно указываем имя параметра
                    onSelect = { equipment -> selectedEquipment = equipment }
                )

                SectionTitle("Разъединители")
                EquipmentChips(
                    voltages = disconnectorVoltages,
                    selectedVoltage = (selectedEquipment as? Disconnector)?.voltage,
                    creator = { voltage -> Disconnector(voltage = voltage) },
                    onSelect = { equipment -> selectedEquipment = equipment }
                )


                SectionTitle("Трансформаторы")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    transformerOptions.forEach { windings ->
                        FilterChip(
                            selected = (selectedEquipment as? Transformer)?.windings == windings,
                            onClick = { selectedEquipment = Transformer(windings = windings) },
                            label = { Text(windings.joinToString(" / ")) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                TextField(
                    value = dispatcherName,
                    onValueChange = { dispatcherName = it },
                    label = { Text("Диспетчерское имя") },
                    placeholder = { Text("Введите название...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(80.dp)) // Чтобы контент не перекрывался кнопкой
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun EquipmentChips(
    voltages: List<Float>,
    selectedVoltage: Float?,
    creator: (Float) -> Equipment,
    onSelect: (Equipment) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        voltages.forEach { voltage ->
            val isSelected = (voltage == selectedVoltage)
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(creator(voltage)) },
                label = { Text("${voltage.toInt()} кВ") },
                // Используем стандартную иконку Check
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

