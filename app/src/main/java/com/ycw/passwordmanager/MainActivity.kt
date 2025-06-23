package com.ycw.passwordmanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.ycw.passwordmanager.data.PasswordDatabase
import com.ycw.passwordmanager.repository.PasswordRepository
import com.ycw.passwordmanager.ui.auth.AuthScreen
import com.ycw.passwordmanager.ui.screens.PasswordListScreen
import com.ycw.passwordmanager.ui.theme.PasswordManagerTheme
import com.ycw.passwordmanager.utils.AppPasswordManager
import com.ycw.passwordmanager.viewmodel.PasswordViewModel
import com.ycw.passwordmanager.viewmodel.PasswordViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appPasswordManager: AppPasswordManager
    
    // 双击退出相关变量
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 2000 // 2秒内双击退出
    
    /**
     * 处理返回键按下事件，实现双击退出功能
     */
    private fun handleBackPress() {
        if (backPressedTime + backPressInterval > System.currentTimeMillis()) {
            // 两秒内第二次点击，退出应用
            finish()
        } else {
            // 第一次点击，显示提示
            Toast.makeText(this, "再次返回退出应用", Toast.LENGTH_SHORT).show()
            backPressedTime = System.currentTimeMillis()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化应用密码管理器
        appPasswordManager = AppPasswordManager(this)
        
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
                    var isAuthenticated by remember { mutableStateOf(false) }
                    val isFirstTime = !appPasswordManager.isPasswordSet()
                    
                    // 双击退出处理
                    BackHandler(enabled = isAuthenticated) {
                        handleBackPress()
                    }
                    
                    if (!isAuthenticated) {
                        AuthScreen(
                            onAuthSuccess = { inputPassword ->
                                if (isFirstTime || appPasswordManager.verifyPassword(inputPassword)) {
                                    isAuthenticated = true
                                }
                            },
                            onSetupPassword = { password, hint ->
                                appPasswordManager.setPassword(password, hint)
                                isAuthenticated = true
                            },
                            passwordManager = appPasswordManager,
                            isFirstTime = isFirstTime
                        )
                    } else {
                        val viewModel: PasswordViewModel = viewModel(factory = viewModelFactory)
                        PasswordListScreen(
                            viewModel = viewModel,
                            passwordManager = appPasswordManager
                        )
                    }
                }
            }
        }
    }
}