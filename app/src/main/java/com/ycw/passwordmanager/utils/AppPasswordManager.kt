package com.ycw.passwordmanager.utils

import android.content.Context
import android.content.SharedPreferences

class AppPasswordManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_password_prefs", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_APP_PASSWORD_HASH = "app_password_hash"
        private const val KEY_IS_PASSWORD_SET = "is_password_set"
        private const val KEY_PASSWORD_HINT = "password_hint"
    }
    
    /**
     * 检查是否已设置应用密码
     */
    fun isPasswordSet(): Boolean {
        return prefs.getBoolean(KEY_IS_PASSWORD_SET, false)
    }
    
    /**
     * 设置应用密码
     * @param password 新密码
     * @param hint 密码提示（可选）
     */
    fun setPassword(password: String, hint: String? = null) {
        val hashedPassword = CryptoUtils.hashPassword(password)
        val editor = prefs.edit()
            .putString(KEY_APP_PASSWORD_HASH, hashedPassword)
            .putBoolean(KEY_IS_PASSWORD_SET, true)
        
        if (hint != null) {
            editor.putString(KEY_PASSWORD_HINT, hint)
        }
        
        editor.apply()
    }
    
    /**
     * 验证应用密码
     * @param password 输入的密码
     * @return 是否正确
     */
    fun verifyPassword(password: String): Boolean {
        val storedHash = prefs.getString(KEY_APP_PASSWORD_HASH, "") ?: ""
        return if (storedHash.isNotEmpty()) {
            CryptoUtils.verifyPassword(password, storedHash)
        } else {
            false
        }
    }
    
    /**
     * 获取密码提示
     * @return 密码提示，如果没有设置则返回null
     */
    fun getPasswordHint(): String? {
        return prefs.getString(KEY_PASSWORD_HINT, null)
    }
    
    /**
     * 设置密码提示
     * @param hint 密码提示
     */
    fun setPasswordHint(hint: String?) {
        if (hint != null) {
            prefs.edit().putString(KEY_PASSWORD_HINT, hint).apply()
        } else {
            prefs.edit().remove(KEY_PASSWORD_HINT).apply()
        }
    }
    
    /**
     * 重置应用密码（清除所有数据）
     */
    fun resetPassword() {
        prefs.edit()
            .remove(KEY_APP_PASSWORD_HASH)
            .remove(KEY_PASSWORD_HINT)
            .putBoolean(KEY_IS_PASSWORD_SET, false)
            .apply()
    }
    
    /**
     * 更改应用密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param newHint 新密码提示（可选）
     * @return 是否成功更改
     */
    fun changePassword(oldPassword: String, newPassword: String, newHint: String? = null): Boolean {
        return if (verifyPassword(oldPassword)) {
            setPassword(newPassword, newHint)
            true
        } else {
            false
        }
    }
}