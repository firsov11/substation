package com.firsov.substation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.firsov.substation.ui.editor.EditorContainerScreen
import com.firsov.substation.ui.editor.EditorScreen
import com.firsov.substation.ui.editor.EditorViewModel

@Composable
fun AppNavigation(viewModel: EditorViewModel) {
    val navController = rememberNavController()
    val containers by viewModel.containers.collectAsState()

    NavHost(navController = navController, startDestination = "editor_screen") {

        // --- Главный экран редактора ---
        composable("editor_screen") {
            EditorScreen(
                viewModel = viewModel,
                onEditCell = { cell ->
                    navController.navigate("container_editor/${cell.id}")
                }
            )
        }

        // --- Экран редактирования конкретного контейнера ---
        composable(
            route = "container_editor/{containerId}",
            arguments = listOf(
                navArgument("containerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val containerId = backStackEntry.arguments?.getString("containerId")
            val cell = remember(containerId) { containers.find { it.id == containerId } }
            val index = remember(containerId) { containers.indexOfFirst { it.id == containerId } }

            if (cell != null) {
                EditorContainerScreen(
                    container = cell,
                    index = index,
                    viewModel = viewModel,
                    onSave = { updatedContainer ->
                        viewModel.updateContainer(updatedContainer)
                        navController.popBackStack()
                    },
                    onDelete = { cellToDelete ->
                        viewModel.deleteContainer(cellToDelete)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("Ошибка: Ячейка не найдена")
            }
        }
    }
}