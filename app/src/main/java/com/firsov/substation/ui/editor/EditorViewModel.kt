package com.firsov.substation.ui.editor

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firsov.substation.config.ContainerConfig
import com.firsov.substation.data.model.Container
import com.firsov.substation.data.model.Port
import com.firsov.substation.data.repository.SubstationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: SubstationRepository
) : ViewModel() {

    val containers: StateFlow<List<Container>> = repository.container
    val ports: StateFlow<List<Port>> = repository.ports

    private val _draggingPort = MutableStateFlow<Pair<String, String>?>(null)
    val draggingPort: StateFlow<Pair<String, String>?> get() = _draggingPort

    private val _dragPoint = MutableStateFlow<Offset?>(null)
    val dragPoint: StateFlow<Offset?> get() = _dragPoint

    fun addContainer() {
        viewModelScope.launch {
            val offset = repository.container.value.size * 20f
            val newContainer = Container(
                x = 50f + offset,
                y = 50f + offset,
                width = ContainerConfig.WIDTH,
                height = ContainerConfig.HEIGHT
            )
            repository.addContainer(newContainer)
        }
    }

    fun updateContainer(container: Container) {
        viewModelScope.launch { repository.updateContainer(container) }
    }

    fun deleteContainer(container: Container) {
        viewModelScope.launch { repository.deleteContainer(container) }
    }

    fun moveContainer(container: Container, newX: Float, newY: Float) {
        updateContainer(container.copy(x = newX, y = newY))
    }

    fun rotateContainer(container: Container) {
        val newRotation = (container.rotation + 90) % 360
        updateContainer(container.copy(rotation = newRotation))
    }

    fun startDragging(containerId: String, portId: String) {
        _draggingPort.value = containerId to portId
    }

    fun updateDragPoint(offset: Offset) {
        _dragPoint.value = offset
    }

    fun stopDragging() {
        _draggingPort.value = null
        _dragPoint.value = null
    }

    fun connectPorts(portId1: String, portId2: String) {
        viewModelScope.launch {
            repository.connectPorts(portId1, portId2)
        }
    }

    fun disconnectPort(portId: String) {
        viewModelScope.launch { repository.disconnect(portId) }
    }

    fun updateContainerWithEquipment(container: Container) {
        viewModelScope.launch { repository.updateContainerWithEquipment(container) }
    }
}