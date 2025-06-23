package com.ycw.passwordmanager.data

import kotlinx.serialization.Serializable

/**
 * 导出数据的模型类
 */
@Serializable
data class ExportData(
    val version: String = "1.0",
    val exportTime: Long = System.currentTimeMillis(),
    val passwords: List<ExportPasswordItem>
)

/**
 * 导出的密码项模型
 */
@Serializable
data class ExportPasswordItem(
    val name: String,
    val username: String,
    val password: String, // 明文密码（导出时解密）
    val note: String,
    val createdAt: Long
) {
    companion object {
        /**
         * 从PasswordEntity转换为ExportPasswordItem
         */
        fun fromPasswordEntity(entity: PasswordEntity): ExportPasswordItem {
            return ExportPasswordItem(
                name = entity.name,
                username = entity.username,
                password = entity.getDecryptedPassword(),
                note = entity.note,
                createdAt = entity.createdAt
            )
        }
    }
    
    /**
     * 转换为PasswordEntity
     */
    fun toPasswordEntity(): PasswordEntity {
        return PasswordEntity.createWithEncryptedPassword(
            name = name,
            username = username,
            plainPassword = password,
            note = note
        )
    }
}

/**
 * 导入结果
 */
data class ImportResult(
    val success: Boolean,
    val importedCount: Int,
    val totalCount: Int,
    val errorMessage: String? = null
)