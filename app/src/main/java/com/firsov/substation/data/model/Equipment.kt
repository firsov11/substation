package com.firsov.substation.data.model

import java.util.UUID

// Базовый класс
sealed class Equipment {

    abstract val id: String
    abstract val type: EquipmentType
    abstract val name: String          // техническое наименование
    abstract val dispatcherName: String // Измените на abstract val
}

// Типы оборудования
enum class EquipmentType {
    BREAKER,
    DISCONNECTOR,
    TRANSFORMER
}

// Выключатель
data class Breaker(
    override val id: String = UUID.randomUUID().toString(),
    val voltage: Float,                    // напряжение
    val state: SwitchState = SwitchState.OFF,
    override val dispatcherName: String = "" // Теперь это в конструкторе
) : Equipment() {
    override val type = EquipmentType.BREAKER
    override val name: String
        get() = "Выключатель ${voltage} кВ"
}

// Разъединитель
data class Disconnector(
    override val id: String = UUID.randomUUID().toString(),
    val voltage: Float,                    // напряжение
    val state: SwitchState = SwitchState.OFF,
    override val dispatcherName: String = "" // Теперь это в конструкторе
) : Equipment() {
    override val type = EquipmentType.DISCONNECTOR
    override val name: String
        get() = "Разъединитель ${voltage} кВ"
}

// Трансформатор
data class Transformer(
    override val id: String = UUID.randomUUID().toString(),
    val windings: List<Float>,
    override val dispatcherName: String = "" // Теперь это в конструкторе              // список обмоток: [330,110,10], [110,35,10] и т.д.
) : Equipment() {
    override val type = EquipmentType.TRANSFORMER
    override val name: String
        get() = "Трансформатор ${windings.joinToString(" / ")} кВ"
}

// Состояние выключателей / разъединителей
enum class SwitchState {
    ON,
    OFF
}