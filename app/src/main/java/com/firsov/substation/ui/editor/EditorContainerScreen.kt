package com.firsov.substation.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.firsov.substation.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorContainerScreen(
    container: Container,
    index: Int,
    viewModel: EditorViewModel,
    onSave: (Container) -> Unit,
    onDelete: (Container) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var selectedEquipment by remember { mutableStateOf(container.equipment) }
    var dispatcherName by remember { mutableStateOf(container.equipment?.dispatcherName ?: "") }
    var currentRotation by remember { mutableStateOf(container.rotation) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контейнер № ${index + 1}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { onDelete(container) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        // Сохраняем оборудование с новым именем диспетчера
                        val finalEquipment = selectedEquipment?.let { eq ->
                            when (eq) {
                                is Breaker -> eq.copy(dispatcherName = dispatcherName)
                                is Disconnector -> eq.copy(dispatcherName = dispatcherName)
                                is Transformer -> eq.copy(dispatcherName = dispatcherName)
                                is Busbar -> eq.copy(dispatcherName = dispatcherName)
                            }
                        }
                        onSave(container.copy(equipment = finalEquipment, rotation = currentRotation))
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Сохранить изменения")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ---------------- ОРИЕНТАЦИЯ ----------------
            Text("Ориентация оборудования", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 90, 180, 270).forEach { angle ->
                    FilterChip(
                        selected = currentRotation == angle,
                        onClick = { currentRotation = angle },
                        label = { Text("$angle°") },
                        leadingIcon = if (currentRotation == angle) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            Divider()

            // ---------------- ВЫБОР ТИПА ОБОРУДОВАНИЯ ----------------
            Text("Тип оборудования", style = MaterialTheme.typography.titleMedium)

            val breakerVoltages = listOf(330f, 110f, 10f)
            val disconnectorVoltages = listOf(330f, 110f, 10f)
            val busbarVoltages = listOf(330f, 110f, 35f, 10f)
            val transformerOptions = listOf(
                listOf(330f, 110f, 10f),
                listOf(110f, 35f, 10f),
                listOf(10f, 0.4f)
            )

            EquipmentDropdown(
                label = "Выключатель",
                options = breakerVoltages,
                selectedOption = (selectedEquipment as? Breaker)?.voltage,
                optionLabel = { "${it.toInt()} кВ" },
                onSelect = { selectedEquipment = Breaker(voltage = it) }
            )

            EquipmentDropdown(
                label = "Разъединитель",
                options = disconnectorVoltages,
                selectedOption = (selectedEquipment as? Disconnector)?.voltage,
                optionLabel = { "${it.toInt()} кВ" },
                onSelect = { selectedEquipment = Disconnector(voltage = it) }
            )

            EquipmentDropdown(
                label = "Секция шин",
                options = busbarVoltages,
                selectedOption = (selectedEquipment as? Busbar)?.voltage,
                optionLabel = { "${it.toInt()} кВ" },
                onSelect = { selectedEquipment = Busbar(voltage = it) }
            )

            EquipmentDropdown(
                label = "Трансформатор",
                options = transformerOptions,
                selectedOption = (selectedEquipment as? Transformer)?.windings,
                optionLabel = { it.map { w -> w.toInt() }.joinToString("/") + " кВ" },
                onSelect = { selectedEquipment = Transformer(windings = it) }
            )

            Divider()

            // ---------------- ПОРТЫ И СВЯЗИ ----------------
            selectedEquipment?.let { eq ->
                Text("Точки подключения", style = MaterialTheme.typography.titleMedium)
                val containerPorts = viewModel.ports.value.filter { it.containerId == container.id }

                containerPorts.forEachIndexed { idx, port ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(12.dp),
                                shape = CircleShape,
                                color = if (port.connectedToPortId != null) Color.Green else Color.Gray
                            ) {}
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Порт #${idx + 1}", style = MaterialTheme.typography.bodyMedium)
                                Text("${port.voltage.toInt()} кВ", style = MaterialTheme.typography.labelSmall)
                            }
                            if (port.connectedToPortId != null) {
                                TextButton(onClick = { viewModel.disconnectPort(port.id) }) {
                                    Text("Разорвать", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // ---------------- ИМЯ ----------------
            OutlinedTextField(
                value = dispatcherName,
                onValueChange = { dispatcherName = it },
                label = { Text("Диспетчерское имя") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ------------------- EquipmentDropdown -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EquipmentDropdown(
    label: String,
    options: List<T>,
    selectedOption: T?,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption?.let(optionLabel) ?: "Не выбрано",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = optionLabel(option))
                            if (option == selectedOption) {
                                Spacer(Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}