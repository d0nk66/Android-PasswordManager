package com.ycw.passwordmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ycw.passwordmanager.data.PasswordEntity
import com.ycw.passwordmanager.repository.PasswordRepository
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
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _isSearching.value = query.isNotBlank()
    }
    
    fun addPassword(name: String, username: String, password: String, note: String) {
        viewModelScope.launch {
            val passwordEntity = PasswordEntity(
                name = name,
                username = username,
                password = password,
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
}