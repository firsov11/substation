package com.firsov.substation.data.model

import java.util.UUID

// --------------------
// Типы оборудования
// --------------------
enum class EquipmentType {
    BREAKER,
    DISCONNECTOR,
    TRANSFORMER,
    BUSBAR // Секция шин
}

// --------------------
// Состояние выключателей
// --------------------
enum class SwitchState {
    ON, OFF
}

// --------------------
// Сторона порта (вместо index)
// --------------------
enum class PortSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM
}

// --------------------
// Описание порта (НЕ состояние)
// --------------------
data class PortDescriptor(
    val side: PortSide,
    val voltage: Float
)

// --------------------
// Базовый класс оборудования
// --------------------
sealed class Equipment {
    abstract val id: String
    abstract val type: EquipmentType
    abstract val name: String
    abstract val dispatcherName: String

    // КЛЮЧЕВОЕ: описание портов
    abstract val portDescriptors: List<PortDescriptor>
}

// --------------------
// Выключатель
// --------------------
data class Breaker(
    override val id: String = UUID.randomUUID().toString(),
    val voltage: Float,
    val state: SwitchState = SwitchState.OFF,
    override val dispatcherName: String = ""
) : Equipment() {

    override val type = EquipmentType.BREAKER
    override val name = "Выключатель ${voltage.toInt()} кВ"

    override val portDescriptors = listOf(
        PortDescriptor(PortSide.LEFT, voltage),
        PortDescriptor(PortSide.RIGHT, voltage)
    )
}

// --------------------
// Разъединитель
// --------------------
data class Disconnector(
    override val id: String = UUID.randomUUID().toString(),
    val voltage: Float,
    val state: SwitchState = SwitchState.OFF,
    override val dispatcherName: String = ""
) : Equipment() {

    override val type = EquipmentType.DISCONNECTOR
    override val name = "Разъединитель ${voltage.toInt()} кВ"

    override val portDescriptors = listOf(
        PortDescriptor(PortSide.LEFT, voltage),
        PortDescriptor(PortSide.RIGHT, voltage)
    )
}

// --------------------
// Трансформатор
// --------------------
data class Transformer(
    override val id: String = UUID.randomUUID().toString(),
    val windings: List<Float>, // например [110f, 10f]
    override val dispatcherName: String = ""
) : Equipment() {

    override val type = EquipmentType.TRANSFORMER
    override val name = "Трансформатор ${windings.joinToString("/") { it.toInt().toString() }} кВ"

    override val portDescriptors = windings.mapIndexed { index, voltage ->
        val side = when (index) {
            0 -> PortSide.LEFT
            1 -> PortSide.RIGHT
            2 -> PortSide.TOP
            else -> PortSide.BOTTOM
        }
        PortDescriptor(side, voltage)
    }
}

// --------------------
// Секция шин
// --------------------
data class Busbar(
    override val id: String = UUID.randomUUID().toString(),
    val voltage: Float,
    override val dispatcherName: String = ""
) : Equipment() {

    override val type = EquipmentType.BUSBAR
    override val name = "Секция шин ${voltage.toInt()} кВ"

    override val portDescriptors = listOf(
        PortDescriptor(PortSide.LEFT, voltage),
        PortDescriptor(PortSide.RIGHT, voltage),
        PortDescriptor(PortSide.TOP, voltage)
    )
}