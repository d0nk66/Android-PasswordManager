package com.ycw.passwordmanager.utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

object PinyinUtils {
    
    // 初始化拼音输出格式
    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.UPPERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
        vCharType = HanyuPinyinVCharType.WITH_V
    }
    
    /**
     * 获取字符串的拼音首字母
     */
    fun getFirstLetter(text: String): String {
        if (text.isEmpty()) return "#"
        
        val firstChar = text.first()
        return getPinyinFirstLetter(firstChar)
    }
    
    /**
     * 获取单个字符的拼音首字母
     */
    private fun getPinyinFirstLetter(char: Char): String {
        // 如果是英文字母，直接返回大写
        if (char.isLetter() && char.code <= 127) {
            return char.uppercaseChar().toString()
        }
        
        // 如果是数字，返回 "#"
        if (char.isDigit()) {
            return "#"
        }
        
        // 使用Pinyin4j库获取中文字符的拼音首字母
        try {
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
            if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                return pinyinArray[0].first().toString()
            }
        } catch (e: Exception) {
            // 如果转换失败，返回 "#"
        }
        
        // 其他字符返回 "#"
        return "#"
    }
}