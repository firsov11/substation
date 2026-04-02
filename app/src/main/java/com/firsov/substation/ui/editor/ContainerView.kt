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
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun ContainerView(
    container: Container,
    viewModel: EditorViewModel,
    onEdit: (Container) -> Unit,
    dragPoint: Offset? = null
) {
    var offsetX by remember { mutableStateOf(container.x) }
    var offsetY by remember { mutableStateOf(container.y) }

    val portsState by viewModel.ports.collectAsState()
    val draggingPortState by viewModel.draggingPort.collectAsState()
    val density = LocalDensity.current

    val equipment = container.equipment
    val color = equipment?.portDescriptors
        ?.firstOrNull()
        ?.voltage
        ?.let { getVoltageColor(it) } ?: Color.Gray

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
                    val currentContainer = viewModel.containers.value.find { it.id == container.id }
                    currentContainer?.let { viewModel.moveContainer(it, offsetX, offsetY) }
                }
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onEdit(container) }
                .rotate(container.rotation.toFloat()),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)

                    equipment?.let {
                        when (it) {
                            is Breaker -> {
                                // Draw line with rectangle break
                                drawLine(color, Offset(0f, center.y), Offset(center.x - 20f, center.y), strokeWidth = 6f)
                                drawRect(color, topLeft = center - Offset(20f, 20f), size = androidx.compose.ui.geometry.Size(40f, 40f))
                                drawLine(color, Offset(center.x + 20f, center.y), Offset(size.width, center.y), strokeWidth = 6f)
                            }
                            is Disconnector -> {
                                // Draw line with diagonal break
                                drawLine(color, Offset(0f, center.y), Offset(center.x - 15f, center.y), strokeWidth = 6f)
                                drawLine(color, Offset(center.x - 15f, center.y - 15f), Offset(center.x + 15f, center.y + 15f), strokeWidth = 6f)
                                drawLine(color, Offset(center.x + 15f, center.y), Offset(size.width, center.y), strokeWidth = 6f)
                            }
                            is Transformer -> {
                                if (it.windings.size == 2) {
                                    // 2-winding transformer: two intersecting circles
                                    drawCircle(color, 25f, center - Offset(20f, 0f))
                                    drawCircle(color, 25f, center + Offset(20f, 0f))
                                } else if (it.windings.size == 3) {
                                    // 3-winding transformer: two circles with semicircle to top port
                                    drawCircle(color, 25f, center - Offset(20f, 0f))
                                    drawCircle(color, 25f, center + Offset(20f, 0f))
                                    // Semicircle from right circle to top
                                    val arcRect = androidx.compose.ui.geometry.Rect(
                                        center.x + 20f - 25f,
                                        center.y - 50f,
                                        center.x + 20f + 25f,
                                        center.y
                                    )
                                    drawArc(color, startAngle = 0f, sweepAngle = 180f, useCenter = false, size = arcRect.size, topLeft = arcRect.topLeft, style = Stroke(width = 6f))
                                    // Line from semicircle to top
                                    drawLine(color, Offset(center.x + 20f, center.y - 50f), Offset(center.x + 20f, 0f), strokeWidth = 6f)
                                }
                            }
                            is Busbar -> {
                                // Thick main line
                                drawLine(color, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 16f)
                                // Thin line to third port (top)
                                drawLine(color, Offset(center.x, center.y), Offset(center.x, 0f), strokeWidth = 6f)
                            }
                        }
                    }
                }

                Text(
                    text = equipment?.dispatcherName ?: "Пусто",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(4.dp),
                    color = color,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Кнопка вращения (не вращается вместе с контейнером)
        IconButton(
            onClick = { viewModel.rotateContainer(container) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(36.dp)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Rotate",
                tint = color.copy(alpha = 0.6f)
            )
        }

        // --- Порты ---
        equipment?.portDescriptors?.forEach { descriptor ->

            val localOffset = PortUtils.calculateLocalPortOffset(
                container,
                descriptor.side,
                density
            )

            val port = portsState.find {
                it.containerId == container.id && it.side == descriptor.side
            }

            val isDragging = draggingPortState?.second == port?.id
            
            // Проверяем, находится ли линия рядом с портом (для эффекта наведения)
            val isNearPort = dragPoint?.let { point ->
                val portGlobalOffset = Offset(
                    container.x + localOffset.x,
                    container.y + localOffset.y
                )
                val dx = point.x - portGlobalOffset.x
                val dy = point.y - portGlobalOffset.y

                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                distance < 60f // Пороговое расстояние для "близости"
            } ?: false
            
            val sizeDp by animateDpAsState(if (isDragging || isNearPort) 24.dp else 16.dp)

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
                    .pointerInput(port?.id ?: "no_port") {
                        detectDragGestures(
                            onDragStart = {
                                port?.id?.let {
                                    viewModel.startDragging(container.id, it)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()

                                // Convert local port position to global screen position
                                val portLocalOffset = PortUtils.calculateLocalPortOffset(
                                    container,
                                    descriptor.side,
                                    density
                                )
                                val fingerGlobal = Offset(
                                    container.x + portLocalOffset.x + change.position.x,
                                    container.y + portLocalOffset.y + change.position.y
                                )

                                viewModel.updateDragPoint(fingerGlobal)
                            },
                            onDragEnd = { 
                                // Handle connection logic in UI layer where we have access to density
                                val dragPoint = viewModel.dragPoint.value ?: return@detectDragGestures
                                val draggingPort = viewModel.draggingPort.value ?: return@detectDragGestures
                                val (sourceContainerId, sourcePortId) = draggingPort
                                
                                val allPorts = viewModel.ports.value
                                val allContainers = viewModel.containers.value
                                val sourcePort = allPorts.find { it.id == sourcePortId } ?: return@detectDragGestures
                                
                                // Find closest port with same voltage
                                val closestPort = allPorts
                                    .filter { it.id != sourcePortId && it.connectedToPortId == null }
                                    .filter { it.voltage == sourcePort.voltage }
                                    .minByOrNull { targetPort ->
                                        val targetContainer = allContainers.find { it.id == targetPort.containerId } 
                                            ?: return@minByOrNull Float.MAX_VALUE
                                        val targetPortOffset = PortUtils.calculateLocalPortOffset(
                                            targetContainer, targetPort.side, density
                                        )
                                        val targetPortGlobal = Offset(
                                            targetContainer.x + targetPortOffset.x,
                                            targetContainer.y + targetPortOffset.y
                                        )
                                        val dx = dragPoint.x - targetPortGlobal.x
                                        val dy = dragPoint.y - targetPortGlobal.y
                                        kotlin.math.sqrt(dx * dx + dy * dy)
                                    }
                                
                                // Connect if close enough
                                if (closestPort != null) {
                                    val targetContainer = allContainers.find { it.id == closestPort.containerId } 
                                        ?: return@detectDragGestures
                                    val targetPortOffset = PortUtils.calculateLocalPortOffset(
                                        targetContainer, closestPort.side, density
                                    )
                                    val targetPortGlobal = Offset(
                                        targetContainer.x + targetPortOffset.x,
                                        targetContainer.y + targetPortOffset.y
                                    )
                                    val dx = dragPoint.x - targetPortGlobal.x
                                    val dy = dragPoint.y - targetPortGlobal.y
                                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                    
                                    if (distance < 60f) {
                                        viewModel.connectPorts(sourcePortId, closestPort.id)
                                    }
                                }
                                
                                viewModel.stopDragging()
                            },
                            onDragCancel = { viewModel.stopDragging() }
                        )
                    },
                shape = CircleShape,
                color = if (port?.connectedToPortId != null)
                    Color.Green else Color.Gray.copy(alpha = 0.7f),
                border = BorderStroke(
                    width = if (isDragging) 4.dp else 2.dp,
                    color = Color.White
                ),
                tonalElevation = if (isDragging) 8.dp else 0.dp
            ){}
        }
    }
}
