package com.firsov.substation.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.firsov.substation.config.ContainerConfig
import com.firsov.substation.data.model.Container
import com.firsov.substation.data.model.PortSide

object PortUtils {

    /**
     * Рассчитать экранный Offset порта с учётом позиции контейнера, размера и rotation
     */
    fun calculateLocalPortOffset(
        container: Container,
        side: PortSide,
        density: Density
    ): Offset {
        val w = with(density) { container.width.dp.toPx() }
        val h = with(density) { container.height.dp.toPx() }

        val baseOffset = when (side) {
            PortSide.LEFT -> Offset(0f, h / 2)
            PortSide.RIGHT -> Offset(w, h / 2)
            PortSide.TOP -> Offset(w / 2, 0f)
            PortSide.BOTTOM -> Offset(w / 2, h)
        }

        val cx = w / 2
        val cy = h / 2

        return when (container.rotation % 360) {
            90 -> Offset(cx + (cy - baseOffset.y), cy + (baseOffset.x - cx))
            180 -> Offset(cx + (cx - baseOffset.x), cy + (cy - baseOffset.y))
            270 -> Offset(cx + (baseOffset.y - cy), cy + (cx - baseOffset.x))
            else -> baseOffset
        }
    }
}