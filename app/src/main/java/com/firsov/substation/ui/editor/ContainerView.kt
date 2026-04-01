package com.firsov.substation.ui.editor

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.firsov.substation.data.model.Breaker
import com.firsov.substation.data.model.Busbar
import com.firsov.substation.data.model.Container
import com.firsov.substation.data.model.Disconnector
import com.firsov.substation.data.model.Transformer
import com.firsov.substation.utils.PortUtils
import kotlin.math.roundToInt

@Composable
fun ContainerView(
    container: Container,
    viewModel: EditorViewModel,
    onEdit: (Container) -> Unit
) {
    var offsetX by remember { mutableStateOf(container.x) }
    var offsetY by remember { mutableStateOf(container.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(container.width.dp)
            .height(container.height.dp)
            .pointerInput(container.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    viewModel.moveContainer(container, offsetX, offsetY)
                }
            }
    ) {
        // --- Контейнер ---
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onEdit(container) },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val equipment = container.equipment
                val color = equipment?.portDescriptors?.firstOrNull()?.voltage?.let { getVoltageColor(it) } ?: Color.Gray

                Canvas(modifier = Modifier.fillMaxSize()) {
                    rotate(container.rotation.toFloat()) {
                        val center = Offset(size.width / 2, size.height / 2)

                        val equipment = container.equipment
                        val color = equipment?.portDescriptors?.firstOrNull()?.voltage
                            ?.let { getVoltageColor(it) } ?: Color.Gray

                        equipment?.let {
                            when (it) {
                                is Breaker -> drawRect(
                                    color,
                                    topLeft = center - Offset(20f, 20f),
                                    size = androidx.compose.ui.geometry.Size(40f, 40f)
                                )
                                is Disconnector -> drawLine(
                                    color,
                                    Offset(0f, center.y),
                                    Offset(size.width, center.y)
                                )
                                is Transformer -> {
                                    drawCircle(color, 25f, center - Offset(20f, 0f))
                                    drawCircle(color, 25f, center + Offset(20f, 0f))
                                }
                                is Busbar -> drawLine(
                                    color,
                                    Offset(0f, center.y),
                                    Offset(size.width, center.y),
                                    strokeWidth = 16f
                                )
                            }
                        }
                    }
                }

                Text(
                    text = equipment?.dispatcherName ?: "Пусто",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp),
                    color = color,
                    style = MaterialTheme.typography.labelLarge
                )

                // --- Вращение ---
                IconButton(
                    onClick = { viewModel.rotateContainer(container) },
                    modifier = Modifier.align(Alignment.TopEnd).size(36.dp).padding(4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = "Rotate",
                        tint = color.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // --- Порты ---
        container.equipment?.portDescriptors?.forEach { descriptor ->
            val density = LocalDensity.current
            val localOffset = PortUtils.calculateLocalPortOffset(
                container,
                descriptor.side,
                density
            )
            val port = viewModel.ports.collectAsState().value.find { it.containerId == container.id && it.side == descriptor.side }

            val isDragging = viewModel.draggingPort.collectAsState().value?.second == port?.id
            val sizeDp by animateDpAsState(if (isDragging) 24.dp else 16.dp)

            val sizePx = with(density) { sizeDp.toPx() }

            Surface(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (localOffset.x - sizePx / 2).roundToInt(),
                            (localOffset.y - sizePx / 2).roundToInt()
                        )
                    }
                    .size(sizeDp)
                    .pointerInput(port?.id) {
                        detectDragGestures(
                            onDragStart = { viewModel.startDragging(container.id, port?.id ?: "") },
                            onDrag = { change, _ ->
                                change.consume()
                                val fingerGlobal = Offset(container.x + change.position.x, container.y + change.position.y)
                                viewModel.updateDragPoint(fingerGlobal)
                            },
                            onDragEnd = { viewModel.stopDraggingWithSearch() },
                            onDragCancel = { viewModel.stopDragging() }
                        )
                    },
                shape = CircleShape,
                color = if (port?.connectedToPortId != null) Color.Green else Color.Gray.copy(alpha = 0.7f),
                border = BorderStroke(width = if (isDragging) 4.dp else 2.dp, color = Color.White),
                tonalElevation = if (isDragging) 8.dp else 0.dp
            ) {}
        }
    }
}