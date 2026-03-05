package com.firsov.substation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // ВАЖНО: Добавьте этот импорт
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.firsov.substation.ui.editor.EditorCellScreen
import com.firsov.substation.ui.editor.EditorScreen
import com.firsov.substation.ui.editor.EditorViewModel
import kotlinx.serialization.Serializable

@Serializable object SubstationMap
@Serializable data class CellEditor(val cellId: String)

@Composable
fun AppNavigation(viewModel: EditorViewModel) {
    val navController = rememberNavController()
    // Теперь 'by' будет работать благодаря импорту getValue
    val cells by viewModel.cells.collectAsState()

    NavHost(navController = navController, startDestination = SubstationMap) {

        composable<SubstationMap> {
            EditorScreen(
                viewModel = viewModel,
                onEditCell = { cell ->
                    navController.navigate(CellEditor(cellId = cell.id))
                }
            )
        }

        composable<CellEditor> { backStackEntry ->
            val args = backStackEntry.toRoute<CellEditor>()
            val cell = cells.find { it.id == args.cellId }
            val index = cells.indexOfFirst { it.id == args.cellId }

            if (cell != null) {
                EditorCellScreen(
                    cell = cell,
                    index = index,
                    onSave = { updatedCell ->
                        // Проверка на null для оборудования
                        val equipment = updatedCell.equipment
                        if (equipment != null) {
                            // Передаем id и объект оборудования
                            viewModel.addEquipmentToCell(updatedCell.id, equipment)
                        }
                        navController.popBackStack()
                    },
                    onDelete = { cellToDelete ->
                        viewModel.deleteCell(cellToDelete)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Если мы попали сюда, значит навигация сработала,
                // но ячейка с таким ID не найдена в списке StateFlow
                Text("Ошибка: Ячейка не найдена")
            }
        }
    }
}
