package com.ycw.passwordmanager.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ycw.passwordmanager.data.ImportResult
import com.ycw.passwordmanager.utils.FileManager
import com.ycw.passwordmanager.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    onNavigateBack: () -> Unit,
    passwordViewModel: PasswordViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<Boolean?>(null) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }

    // 导出文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isExporting = true
                val success = passwordViewModel.exportPasswords(context, it)
                exportResult = success
                isExporting = false
                showExportDialog = true
            }
        }
    }

    // 导入文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isImporting = true
                val result = passwordViewModel.importPasswords(context, it)
                importResult = result
                isImporting = false
                showImportDialog = true
            }
        }
    }

    // 覆盖界面布局
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable(enabled = false) { /* 阻止点击穿透 */ }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "导入导出",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                // 导出部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "导出密码",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "将所有密码导出为JSON文件，保存到手机本地存储。导出的文件包含明文密码，请妥善保管。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val fileName = FileManager.generateExportFileName()
                                exportLauncher.launch(fileName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isExporting && !isImporting
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("导出中...")
                            } else {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("选择导出位置")
                            }
                        }
                    }
                }

                // 导入部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "导入密码",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "从JSON文件导入密码数据。支持本应用导出的文件格式，导入的密码将添加到现有密码列表中。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "text/plain"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isExporting && !isImporting
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("导入中...")
                            } else {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("选择导入文件")
                            }
                        }
                    }
                }

                // 注意事项
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "注意事项",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "• 导出的文件包含明文密码，请确保文件安全\n" +
                                    "• 建议将导出文件存储在安全的位置\n" +
                                    "• 导入操作会将密码添加到现有列表中，不会覆盖\n" +
                                    "• 仅支持本应用导出的JSON格式文件",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 导出结果对话框
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = {
                    Text(
                        text = if (exportResult == true) "导出成功" else "导出失败",
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = if (exportResult == true) {
                            "密码数据已成功导出到选定位置。"
                        } else {
                            "导出失败，请检查存储权限和可用空间。"
                        },
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportResult = null
                        }
                    ) {
                        Text("确定")
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (exportResult == true) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (exportResult == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            )
        }

        // 导入结果对话框
        if (showImportDialog && importResult != null) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = {
                    Text(
                        text = if (importResult!!.success) "导入完成" else "导入失败",
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = if (importResult!!.success) {
                            "成功导入 ${importResult!!.importedCount} 个密码（共 ${importResult!!.totalCount} 个）。"
                        } else {
                            importResult!!.errorMessage ?: "导入失败，请检查文件格式。"
                        },
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importResult = null
                        }
                    ) {
                        Text("确定")
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (importResult!!.success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (importResult!!.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            )
        }

    }
}