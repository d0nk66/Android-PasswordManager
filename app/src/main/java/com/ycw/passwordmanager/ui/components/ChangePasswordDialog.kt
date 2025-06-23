package com.ycw.passwordmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPassword: String, newPassword: String, newHint: String?) -> Boolean
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newHint by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "更改应用密码",
                    style = MaterialTheme.typography.headlineSmall
                )

                // 旧密码输入框
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { 
                        oldPassword = it
                        errorMessage = ""
                    },
                    label = { Text("当前密码 *") },
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (oldPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    isError = oldPassword.isEmpty() && errorMessage.isNotEmpty()
                )

                // 新密码输入框
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        errorMessage = ""
                    },
                    label = { Text("新密码 *") },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = newPassword.isEmpty() && errorMessage.isNotEmpty()
                )

                // 确认新密码输入框
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        errorMessage = ""
                    },
                    label = { Text("确认新密码 *") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = (confirmPassword.isEmpty() || newPassword != confirmPassword) && errorMessage.isNotEmpty()
                )

                // 新密码提示输入框
                OutlinedTextField(
                    value = newHint,
                    onValueChange = { newHint = it },
                    label = { Text("新密码提示（可选）") },
                    placeholder = { Text("例如：我的生日+宠物名字") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword) {
                                val success = onConfirm(oldPassword, newPassword, newHint.takeIf { it.isNotBlank() })
                                if (!success) {
                                    errorMessage = "当前密码错误"
                                }
                            } else {
                                errorMessage = when {
                                    oldPassword.isBlank() -> "请输入当前密码"
                                    newPassword.isBlank() -> "请输入新密码"
                                    newPassword != confirmPassword -> "新密码不匹配"
                                    else -> "请检查输入"
                                }
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 错误信息
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword) {
                                val success = onConfirm(oldPassword, newPassword, newHint.takeIf { it.isNotBlank() })
                                if (!success) {
                                    errorMessage = "当前密码错误"
                                }
                            } else {
                                errorMessage = when {
                                    oldPassword.isBlank() -> "请输入当前密码"
                                    newPassword.isBlank() -> "请输入新密码"
                                    newPassword != confirmPassword -> "新密码不匹配"
                                    else -> "请检查输入"
                                }
                            }
                        },
                        enabled = oldPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                    ) {
                        Text("确认更改")
                    }
                }
            }
        }
    }
}