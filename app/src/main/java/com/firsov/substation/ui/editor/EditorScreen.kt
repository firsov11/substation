package com.firsov.substation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import com.firsov.substation.data.model.Busbar
import com.firsov.substation.data.model.Container
import com.firsov.substation.utils.PortUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onEditCell: (Container) -> Unit
) {
    val containers by viewModel.containers.collectAsState()
    val ports by viewModel.ports.collectAsState()
    val draggingPortInfo by viewModel.draggingPort.collectAsState()
    val dragPoint by viewModel.dragPoint.collectAsState()

    val density = LocalDensity.current

    val gridSize = 40f
    val gridColor = Color.LightGray.copy(alpha = 0.3f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактор подстанции") },
                actions = {
                    IconButton(onClick = { viewModel.addContainer() }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить контейнер")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addContainer() }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {

                // --- Сетка ---
                for (x in 0..size.width.toInt() step gridSize.toInt()) {
                    drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                }
                for (y in 0..size.height.toInt() step gridSize.toInt()) {
                    drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                }

                // --- Линии соединений ---
                val drawnConnections = mutableSetOf<String>()

                containers.forEach { container ->
                    val containerPorts = ports.filter { it.containerId == container.id }

                    containerPorts.forEach { port ->
                        val targetId = port.connectedToPortId

                        if (targetId != null && !drawnConnections.contains(port.id)) {

                            // --- START ---
                            val startLocal = PortUtils.calculateLocalPortOffset(
                                container,
                                port.side,
                                density
                            )
                            val start = Offset(
                                container.x + startLocal.x,
                                container.y + startLocal.y
                            )

                            // --- END ---
                            val endPort = ports.find { it.id == targetId }
                            val endContainer = containers.find { it.id == endPort?.containerId }

                            val end = if (endPort != null && endContainer != null) {
                                val endLocal = PortUtils.calculateLocalPortOffset(
                                    endContainer,
                                    endPort.side,
                                    density
                                )
                                Offset(
                                    endContainer.x + endLocal.x,
                                    endContainer.y + endLocal.y
                                )
                            } else null

                            if (end != null && endContainer != null) {
                                val isBusToBus =
                                    container.equipment is Busbar &&
                                            endContainer.equipment is Busbar

                                drawLine(
                                    color = getVoltageColor(port.voltage),
                                    start = start,
                                    end = end,
                                    strokeWidth = if (isBusToBus) 14f else 6f
                                )

                                drawnConnections.add(port.id)
                                drawnConnections.add(targetId)
                            }
                        }
                    }
                }

                // --- Резиновая линия ---
                if (draggingPortInfo != null && dragPoint != null) {

                    val (containerId, portId) = draggingPortInfo!!

                    val startContainer = containers.find { it.id == containerId }
                    val startPort = ports.find { it.id == portId }

                    if (startContainer != null && startPort != null) {

                        val startLocal = PortUtils.calculateLocalPortOffset(
                            startContainer,
                            startPort.side,
                            density
                        )

                        val start = Offset(
                            startContainer.x + startLocal.x,
                            startContainer.y + startLocal.y
                        )

                        drawLine(
                            color = Color.Gray,
                            start = start,
                            end = dragPoint!!,
                            strokeWidth = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }
                }
            }

            // --- Контейнеры ---
            containers.forEach { container ->
                key(container.id) {
                    ContainerView(
                        container = container,
                        viewModel = viewModel,
                        onEdit = { onEditCell(it) }
                    )
                }
            }
        }
    }
}

fun getVoltageColor(voltage: Float): Color = when (voltage) {
    330f -> Color(0xFFE91E63)
    110f -> Color(0xFFFFEB3B)
    35f -> Color(0xFF4CAF50)
    10f -> Color(0xFF2196F3)
    else -> Color.Gray
}