package com.ycw.passwordmanager.utils

import android.content.Context
import android.net.Uri
import com.ycw.passwordmanager.data.ExportData
import com.ycw.passwordmanager.data.ImportResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件管理工具类，处理密码数据的导出和导入
 */
object FileManager {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * 生成导出文件名
     */
    fun generateExportFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "passwords_backup_$timestamp.json"
    }
    
    /**
     * 将导出数据转换为JSON字符串
     */
    fun exportDataToJson(exportData: ExportData): String {
        return json.encodeToString(exportData)
    }
    
    /**
     * 写入数据到指定URI
     */
    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                outputStream.flush()
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 从URI读取数据并解析为导入结果
     */
    fun readFromUri(context: Context, uri: Uri): ImportResult {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return ImportResult(
                success = false,
                importedCount = 0,
                totalCount = 0,
                errorMessage = "无法读取文件"
            )
            
            parseImportData(content)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                importedCount = 0,
                totalCount = 0,
                errorMessage = "文件读取失败: ${e.message}"
            )
        }
    }
    
    /**
     * 解析导入数据
     */
    private fun parseImportData(content: String): ImportResult {
        return try {
            val exportData = json.decodeFromString<ExportData>(content)
            ImportResult(
                success = true,
                importedCount = exportData.passwords.size,
                totalCount = exportData.passwords.size,
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                importedCount = 0,
                totalCount = 0,
                errorMessage = "文件格式错误: ${e.message}"
            )
        }
    }
    
    /**
     * 从JSON字符串解析导出数据
     */
    fun parseExportData(content: String): ExportData? {
        return try {
            json.decodeFromString<ExportData>(content)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}