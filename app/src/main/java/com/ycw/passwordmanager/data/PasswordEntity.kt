package com.ycw.passwordmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ycw.passwordmanager.utils.CryptoUtils

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val username: String,
    val password: String, // 存储加密后的密码
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取解密后的密码
     */
    fun getDecryptedPassword(): String {
        return try {
            CryptoUtils.decryptPassword(password)
        } catch (e: Exception) {
            // 如果解密失败，可能是旧数据（未加密），直接返回
            password
        }
    }
    
    companion object {
        /**
         * 创建带加密密码的实体
         */
        fun createWithEncryptedPassword(
            name: String,
            username: String,
            plainPassword: String,
            note: String = "",
            id: Long = 0
        ): PasswordEntity {
            val encryptedPassword = CryptoUtils.encryptPassword(plainPassword)
            return PasswordEntity(
                id = id,
                name = name,
                username = username,
                password = encryptedPassword,
                note = note
            )
        }
    }
}