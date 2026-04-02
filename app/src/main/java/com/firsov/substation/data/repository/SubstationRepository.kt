package com.firsov.substation.data.repository

import com.firsov.substation.data.local.ContainerDao
import com.firsov.substation.data.local.PortDao
import com.firsov.substation.data.model.Container
import com.firsov.substation.data.model.Port
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubstationRepository @Inject constructor(
    private val containerDao: ContainerDao,
    private val portDao: PortDao,
) {

    // ---------------- CONTAINERS ----------------
    val container: StateFlow<List<Container>> =
        containerDao.getAllCells()
            .stateIn(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    suspend fun addContainer(container: Container) {
        containerDao.insertOrUpdate(container)
        createPortsForContainer(container)
    }

    suspend fun updateContainer(container: Container) {
        containerDao.insertOrUpdate(container)
    }

    suspend fun updateContainerWithEquipment(container: Container) {
        containerDao.insertOrUpdate(container)
        createPortsForContainer(container)
    }

    suspend fun deleteContainer(container: Container) {
        containerDao.delete(container)
        deletePortsOfContainer(container.id)
    }

    // ---------------- PORTS ----------------
    val ports: StateFlow<List<Port>> =
        portDao.getAll()
            .stateIn(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    suspend fun createPortsForContainer(container: Container) {
        val equipment = container.equipment ?: return

        // Удаляем старые порты, если они есть
        portDao.deleteByContainer(container.id)

        // Создаём новые
        val ports = equipment.portDescriptors.map {
            Port(
                id = UUID.randomUUID().toString(),
                containerId = container.id,
                side = it.side,
                voltage = it.voltage,
                connectedToPortId = null
            )
        }
        portDao.insertAll(ports)
    }

    suspend fun connectPorts(p1: String, p2: String) {
        // Обновляем в транзакции
        val port1 = ports.value.find { it.id == p1 } ?: return
        val port2 = ports.value.find { it.id == p2 } ?: return

        portDao.update(port1.copy(connectedToPortId = p2))
        portDao.update(port2.copy(connectedToPortId = p1))
    }

    suspend fun disconnect(portId: String) {
        val port = ports.value.find { it.id == portId } ?: return
        val target = ports.value.find { it.id == port.connectedToPortId }

        portDao.update(port.copy(connectedToPortId = null))
        target?.let { portDao.update(it.copy(connectedToPortId = null)) }
    }

    suspend fun deletePortsOfContainer(containerId: String) {
        portDao.deleteByContainer(containerId)
    }
}
