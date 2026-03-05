package com.firsov.substation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.firsov.substation.ui.editor.EditorScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Запускаем навигацию, а не EditorScreen напрямую
                AppNavigation(viewModel = hiltViewModel())
            }
        }
    }
}
