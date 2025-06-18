package com.ycw.passwordmanager.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    
    @Query("SELECT * FROM passwords ORDER BY name COLLATE NOCASE ASC")
    fun getAllPasswords(): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY name COLLATE NOCASE ASC")
    fun searchPasswords(query: String): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Long): PasswordEntity?
    
    @Insert
    suspend fun insertPassword(password: PasswordEntity)
    
    @Update
    suspend fun updatePassword(password: PasswordEntity)
    
    @Delete
    suspend fun deletePassword(password: PasswordEntity)
    
    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Long)
}