package com.ycw.passwordmanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ycw.passwordmanager.data.PasswordEntity
import com.ycw.passwordmanager.ui.components.AddPasswordDialog
import com.ycw.passwordmanager.ui.components.AlphabetSidebar
import com.ycw.passwordmanager.ui.components.ChangePasswordDialog
import com.ycw.passwordmanager.ui.components.EditPasswordDialog
import com.ycw.passwordmanager.ui.components.PasswordDetailDialog
import com.ycw.passwordmanager.ui.components.PasswordItem
import com.ycw.passwordmanager.utils.AppPasswordManager
import com.ycw.passwordmanager.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListScreen(viewModel: PasswordViewModel, passwordManager: AppPasswordManager) {
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val groupedPasswords by viewModel.groupedPasswords.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showImportExportScreen by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var editingPassword by remember { mutableStateOf<PasswordEntity?>(null) }
    var detailPassword by remember { mutableStateOf<PasswordEntity?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 滚动任务管理
    var scrollJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // 返回键处理逻辑
    BackHandler(enabled = isSearching || showImportExportScreen) {
        when {
            showImportExportScreen -> {
                // 从导入导出界面返回
                showImportExportScreen = false
            }
            isSearching -> {
                // 从搜索界面返回
                viewModel.clearSearch()
            }
        }
    }

    /**
     * 滚动到指定字母的函数，实现实时跳转 直接跳转版本：无动画，立即到位
     * @param letter 目标字母
     */
    val scrollToLetter: (String) -> Unit = { letter ->
        // 取消之前的滚动任务，确保只有最新的滚动请求生效
        scrollJob?.cancel()

        scrollJob =
                coroutineScope.launch {
                    try {
                        val groups = groupedPasswords.keys.toList().sorted()
                        val targetIndex = groups.indexOf(letter)

                        if (targetIndex >= 0) {
                            var itemIndex = 0
                            // 优化计算：使用更高效的索引计算方式
                            for (i in 0 until targetIndex) {
                                itemIndex +=
                                        (groupedPasswords[groups[i]]?.size
                                                ?: 0) + 1 // +1 for header
                            }

                            // 直接跳转到目标位置，无动画
                            listState.scrollToItem(index = itemIndex, scrollOffset = 0)
                        }
                    } catch (e: Exception) {
                        // 忽略滚动过程中的异常（如组件已销毁）
                    }
                }
    }

    Scaffold(
            // … topBar, content …
            floatingActionButton = {
                Box {
                    // 1. 主 FAB 的旋转动画
                    val rotation by
                            animateFloatAsState(
                                    targetValue = if (showFabMenu) 45f else 0f,
                                    animationSpec =
                                            spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                            ),
                                    label = "fab_rotation"
                            )

                    FloatingActionButton(
                            onClick = { showFabMenu = !showFabMenu },
                            modifier =
                                    Modifier.align(Alignment.BottomEnd).graphicsLayer {
                                        rotationZ = rotation
                                    }
                    ) {
                        Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (showFabMenu) "关闭菜单" else "打开菜单"
                        )
                    }

                    // 2. 子按钮的容器
                    AnimatedVisibility(
                            visible = showFabMenu,
                            enter =
                                    slideInVertically(
                                            // 从自身高度的下方开始
                                            initialOffsetY = { fullHeight -> fullHeight },
                                            animationSpec =
                                                    tween(
                                                            durationMillis = 200,
                                                            easing = FastOutSlowInEasing
                                                    )
                                    ) +
                                            fadeIn(
                                                    animationSpec =
                                                            tween(
                                                                    durationMillis = 200,
                                                                    easing = FastOutSlowInEasing
                                                            )
                                            ),
                            exit =
                                    slideOutVertically(
                                            targetOffsetY = { fullHeight -> fullHeight },
                                            animationSpec =
                                                    tween(
                                                            durationMillis = 150,
                                                            easing = FastOutLinearInEasing
                                                    )
                                    ) +
                                            fadeOut(
                                                    animationSpec =
                                                            tween(
                                                                    durationMillis = 150,
                                                                    easing = FastOutLinearInEasing
                                                            )
                                            ),
                            modifier =
                                    Modifier
                                            // 跟主按钮同一锚点：右下角
                                            .align(Alignment.BottomEnd)
                    ) {
                        Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.End,
                                // bottom 提升主 FAB 高度 + 16dp，end 内缩 (56dp-48dp)/2 = 4dp
                                modifier =
                                        Modifier.padding(
                                                bottom = 56.dp + 16.dp,
                                                end = (56.dp - 48.dp) / 2
                                        )
                        ) {
                            // 导入导出
                            FloatingActionButton(
                                    onClick = {
                                        showImportExportScreen = true
                                        showFabMenu = false
                                    },
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                        Icons.Default.ImportExport,
                                        contentDescription = "导入导出",
                                        modifier = Modifier.size(20.dp)
                                )
                            }

                            // 更改应用密码
                            FloatingActionButton(
                                    onClick = {
                                        showChangePasswordDialog = true
                                        showFabMenu = false
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "更改应用密码",
                                        modifier = Modifier.size(20.dp)
                                )
                            }

                            // 添加密码
                            FloatingActionButton(
                                    onClick = {
                                        showAddDialog = true
                                        showFabMenu = false
                                    },
                                    modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                        Icons.Default.Add,
                                        contentDescription = "添加密码",
                                        modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 搜索栏
                SearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        modifier =
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 密码列表
                Row(modifier = Modifier.fillMaxSize()) {
                    // 主要内容区域
                    Box(modifier = Modifier.weight(1f)) {
                        if (passwords.isEmpty()) {
                            // 空状态
                            EmptyState(modifier = Modifier.align(Alignment.Center))
                        } else {
                            if (isSearching) {
                                // 搜索结果列表
                                SearchResultsList(
                                        passwords = passwords,
                                        onDeletePassword = viewModel::deletePassword,
                                        onEditPassword = { password ->
                                            editingPassword = password
                                            showEditDialog = true
                                        },
                                        onClickPassword = { password ->
                                            detailPassword = password
                                            showDetailDialog = true
                                        },
                                        listState = listState
                                )
                            } else {
                                // 分组列表
                                GroupedPasswordsList(
                                        groupedPasswords = groupedPasswords,
                                        onDeletePassword = viewModel::deletePassword,
                                        onEditPassword = { password ->
                                            editingPassword = password
                                            showEditDialog = true
                                        },
                                        onClickPassword = { password ->
                                            detailPassword = password
                                            showDetailDialog = true
                                        },
                                        listState = listState
                                )
                            }
                        }
                    }

                    // 字母导航栏（仅在非搜索状态下显示）
                    if (!isSearching && passwords.isNotEmpty()) {
                        AlphabetSidebar(
                                availableLetters = groupedPasswords.keys.toSet(),
                                onLetterClick = scrollToLetter,
                                modifier = Modifier.padding(end = 4.dp, bottom = 80.dp)
                        )
                    }
                }
            }
        }
    }

    // 添加密码对话框
    if (showAddDialog) {
        AddPasswordDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, username, password, note ->
                    viewModel.addPassword(name, username, password, note)
                }
        )
    }

    // 编辑密码对话框
    if (showEditDialog && editingPassword != null) {
        EditPasswordDialog(
                password = editingPassword!!,
                onDismiss = {
                    showEditDialog = false
                    editingPassword = null
                },
                onConfirm = { id, name, username, password, note ->
                    viewModel.updatePasswordWithEncryption(id, name, username, password, note)
                }
        )
    }

    // 密码详情对话框
    if (showDetailDialog && detailPassword != null) {
        PasswordDetailDialog(
                password = detailPassword!!,
                onDismiss = {
                    showDetailDialog = false
                    detailPassword = null
                }
        )
    }

    // 更改应用密码对话框
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
                onDismiss = { showChangePasswordDialog = false },
                onConfirm = { oldPassword, newPassword, newHint ->
                    // 验证旧密码
                    if (passwordManager.verifyPassword(oldPassword)) {
                        // 更改密码和提示
                        passwordManager.changePassword(oldPassword, newPassword, newHint)
                        showChangePasswordDialog = false
                        true // 返回成功
                    } else {
                        false // 返回失败，旧密码错误
                    }
                }
        )
    }

    // 导入导出界面
    if (showImportExportScreen) {
        ImportExportScreen(
                onNavigateBack = { showImportExportScreen = false },
                passwordViewModel = viewModel
        )
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier,
            placeholder = { Text("搜索密码...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "清除")
                    }
                }
            },
            singleLine = true
    )
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
                text = "还没有保存的密码",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = "点击右下角的 + 按钮添加第一个密码",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SearchResultsList(
        passwords: List<PasswordEntity>,
        onDeletePassword: (PasswordEntity) -> Unit,
        onEditPassword: (PasswordEntity) -> Unit,
        onClickPassword: (PasswordEntity) -> Unit,
        listState: LazyListState
) {
    LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(passwords, key = { it.id }) { password ->
            PasswordItem(
                    password = password,
                    onDelete = { onDeletePassword(password) },
                    onEdit = { onEditPassword(password) },
                    onClick = { onClickPassword(password) }
            )
        }
    }
}

@Composable
fun GroupedPasswordsList(
        groupedPasswords: Map<String, List<PasswordEntity>>,
        onDeletePassword: (PasswordEntity) -> Unit,
        onEditPassword: (PasswordEntity) -> Unit,
        onClickPassword: (PasswordEntity) -> Unit,
        listState: LazyListState
) {
    LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedPasswords.forEach { (letter, passwordsInGroup) ->
            // 分组标题
            item(key = "header_$letter") {
                Text(
                        text = letter,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 分组内的密码项
            items(passwordsInGroup, key = { it.id }) { password ->
                PasswordItem(
                        password = password,
                        onDelete = { onDeletePassword(password) },
                        onEdit = { onEditPassword(password) },
                        onClick = { onClickPassword(password) }
                )
            }
        }
    }
}
