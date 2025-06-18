package com.ycw.passwordmanager.repository

import com.ycw.passwordmanager.data.PasswordDao
import com.ycw.passwordmanager.data.PasswordEntity
import kotlinx.coroutines.flow.Flow

class PasswordRepository(private val passwordDao: PasswordDao) {
    
    fun getAllPasswords(): Flow<List<PasswordEntity>> {
        return passwordDao.getAllPasswords()
    }
    
    fun searchPasswords(query: String): Flow<List<PasswordEntity>> {
        return passwordDao.searchPasswords(query)
    }
    
    suspend fun getPasswordById(id: Long): PasswordEntity? {
        return passwordDao.getPasswordById(id)
    }
    
    suspend fun insertPassword(password: PasswordEntity) {
        passwordDao.insertPassword(password)
    }
    
    suspend fun updatePassword(password: PasswordEntity) {
        passwordDao.updatePassword(password)
    }
    
    suspend fun deletePassword(password: PasswordEntity) {
        passwordDao.deletePassword(password)
    }
    
    suspend fun deletePasswordById(id: Long) {
        passwordDao.deletePasswordById(id)
    }
}