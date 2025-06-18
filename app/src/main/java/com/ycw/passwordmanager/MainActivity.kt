package com.ycw.passwordmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.ycw.passwordmanager.data.PasswordDatabase

import com.ycw.passwordmanager.repository.PasswordRepository
import com.ycw.passwordmanager.ui.screens.PasswordListScreen
import com.ycw.passwordmanager.ui.theme.PasswordManagerTheme
import com.ycw.passwordmanager.viewmodel.PasswordViewModel
import com.ycw.passwordmanager.viewmodel.PasswordViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化数据库和Repository
        val database = PasswordDatabase.getDatabase(this)
        val repository = PasswordRepository(database.passwordDao())
        val viewModelFactory = PasswordViewModelFactory(repository)
        

        
        setContent {
            PasswordManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: PasswordViewModel = viewModel(factory = viewModelFactory)
                    PasswordListScreen(viewModel = viewModel)
                }
            }
        }
    }
    

}