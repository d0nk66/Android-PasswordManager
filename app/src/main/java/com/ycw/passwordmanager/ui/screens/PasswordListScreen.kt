package com.ycw.passwordmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ycw.passwordmanager.data.PasswordEntity
import com.ycw.passwordmanager.ui.components.AddPasswordDialog
import com.ycw.passwordmanager.ui.components.AlphabetSidebar
import com.ycw.passwordmanager.ui.components.EditPasswordDialog
import com.ycw.passwordmanager.ui.components.PasswordDetailDialog
import com.ycw.passwordmanager.ui.components.PasswordItem
import com.ycw.passwordmanager.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListScreen(
    viewModel: PasswordViewModel
) {
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val groupedPasswords by viewModel.groupedPasswords.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var editingPassword by remember { mutableStateOf<PasswordEntity?>(null) }
    var detailPassword by remember { mutableStateOf<PasswordEntity?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 滚动到指定字母的函数
    val scrollToLetter: (String) -> Unit = { letter ->
        coroutineScope.launch {
            val groups = groupedPasswords.keys.toList().sorted()
            val targetIndex = groups.indexOf(letter)
            if (targetIndex >= 0) {
                var itemIndex = 0
                for (i in 0 until targetIndex) {
                    itemIndex += (groupedPasswords[groups[i]]?.size ?: 0) + 1 // +1 for header
                }
                listState.animateScrollToItem(itemIndex)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("密码管理器") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加密码")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 搜索栏
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // 密码列表
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 主要内容区域
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (passwords.isEmpty()) {
                            // 空状态
                            EmptyState(
                                modifier = Modifier.align(Alignment.Center)
                            )
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
            onConfirm = { updatedPassword ->
                viewModel.updatePassword(updatedPassword)
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
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("搜索密码...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "搜索")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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