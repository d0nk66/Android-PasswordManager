package com.ycw.passwordmanager.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ycw.passwordmanager.data.ExportData
import com.ycw.passwordmanager.data.ExportPasswordItem
import com.ycw.passwordmanager.data.ImportResult
import com.ycw.passwordmanager.data.PasswordEntity
import com.ycw.passwordmanager.repository.PasswordRepository
import com.ycw.passwordmanager.utils.FileManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

class PasswordViewModel(private val repository: PasswordRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    val passwords: StateFlow<List<PasswordEntity>> = searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllPasswords()
            } else {
                repository.searchPasswords(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * 更新搜索查询字符串
     * @param query 搜索查询字符串
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _isSearching.value = query.isNotBlank()
    }
    
    /**
     * 清除搜索状态，重置搜索查询和搜索状态
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
    }
    
    fun addPassword(name: String, username: String, password: String, note: String) {
        viewModelScope.launch {
            val passwordEntity = PasswordEntity.createWithEncryptedPassword(
                name = name,
                username = username,
                plainPassword = password,
                note = note
            )
            repository.insertPassword(passwordEntity)
        }
    }
    
    fun deletePassword(password: PasswordEntity) {
        viewModelScope.launch {
            repository.deletePassword(password)
        }
    }
    
    fun updatePassword(password: PasswordEntity) {
        viewModelScope.launch {
            repository.updatePassword(password)
        }
    }
    
    fun updatePasswordWithEncryption(id: Long, name: String, username: String, plainPassword: String, note: String) {
        viewModelScope.launch {
            val passwordEntity = PasswordEntity.createWithEncryptedPassword(
                id = id,
                name = name,
                username = username,
                plainPassword = plainPassword,
                note = note
            )
            repository.updatePassword(passwordEntity)
        }
    }
    
    // 获取按首字母分组的密码列表
    val groupedPasswords: StateFlow<Map<String, List<PasswordEntity>>> = passwords
        .map { passwordList ->
            passwordList.groupBy { password ->
                com.ycw.passwordmanager.utils.PinyinUtils.getFirstLetter(password.name)
            }.toSortedMap()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
    
    /**
     * 导出密码数据到指定URI
     */
    suspend fun exportPasswords(context: Context, uri: Uri): Boolean {
        return try {
            val currentPasswords = passwords.value
            val exportItems = currentPasswords.map { ExportPasswordItem.fromPasswordEntity(it) }
            val exportData = ExportData(passwords = exportItems)
            val jsonContent = FileManager.exportDataToJson(exportData)
            FileManager.writeToUri(context, uri, jsonContent)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 从指定URI导入密码数据
     */
    suspend fun importPasswords(context: Context, uri: Uri): ImportResult {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return ImportResult(
                success = false,
                importedCount = 0,
                totalCount = 0,
                errorMessage = "无法读取文件"
            )
            
            val exportData = FileManager.parseExportData(content)
            if (exportData == null) {
                return ImportResult(
                    success = false,
                    importedCount = 0,
                    totalCount = 0,
                    errorMessage = "文件格式错误"
                )
            }
            
            var importedCount = 0
            exportData.passwords.forEach { exportItem ->
                try {
                    val passwordEntity = exportItem.toPasswordEntity()
                    repository.insertPassword(passwordEntity)
                    importedCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            ImportResult(
                success = true,
                importedCount = importedCount,
                totalCount = exportData.passwords.size,
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                importedCount = 0,
                totalCount = 0,
                errorMessage = "导入失败: ${e.message}"
            )
        }
    }
}