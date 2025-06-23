package com.ycw.passwordmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * 字母侧边栏组件，支持点击和滑动手势进行字母选择
 * @param availableLetters 可用的字母集合
 * @param onLetterClick 字母点击回调函数
 * @param modifier 修饰符
 */
@Composable
fun AlphabetSidebar(
    availableLetters: Set<String>,
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 字母表，包含A-Z和#号
    val alphabet = ('A'..'Z').map { it.toString() } + "#"
    
    // 获取本地密度和触觉反馈
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    
    // 记录组件的高度和位置信息
    var componentHeight by remember { mutableStateOf(0f) }
    var componentTop by remember { mutableStateOf(0f) }
    
    // 当前选中的字母索引
    var currentSelectedIndex by remember { mutableStateOf(-1) }
    
    // 防抖状态
    var lastTriggerTime by remember { mutableStateOf(0L) }
    
    /**
     * 根据触摸位置计算对应的字母索引
     * @param relativeY 相对于组件顶部的Y坐标
     * @return 字母索引，如果超出范围则返回-1
     */
    fun getLetterIndexFromRelativeY(relativeY: Float): Int {
        if (componentHeight <= 0 || relativeY < 0 || relativeY > componentHeight) return -1
        
        // 计算每个字母项的实际高度和间距
        val spacingInPx = with(density) { 1.dp.toPx() }
        val itemSizeInPx = with(density) { 18.dp.toPx() } // 每个字母项的固定大小
        
        // 计算总的项目高度（包括间距）
        val totalItemsHeight = alphabet.size * itemSizeInPx + (alphabet.size - 1) * spacingInPx
        
        // 如果组件高度小于所需高度，按比例缩放
        val scale = if (totalItemsHeight > componentHeight) {
            componentHeight / totalItemsHeight
        } else {
            1f
        }
        
        val scaledItemSize = itemSizeInPx * scale
        val scaledSpacing = spacingInPx * scale
        
        // 使用数学公式直接计算索引，避免累积误差
        // 每个项目占用的总空间 = 项目大小 + 间距（除了最后一个项目）
        val itemTotalSpace = scaledItemSize + scaledSpacing
        
        // 计算当前位置对应的项目索引
        val rawIndex = relativeY / itemTotalSpace
        val index = rawIndex.toInt().coerceIn(0, alphabet.size - 1)
        
        // 验证计算结果：检查触摸点是否真的在该项目范围内
        val itemStart = index * itemTotalSpace
        val itemEnd = itemStart + scaledItemSize
        
        return if (relativeY >= itemStart && relativeY <= itemEnd) {
            index
        } else {
            // 如果不在项目范围内，可能在间距中，选择最近的项目
            if (relativeY < itemStart) {
                (index - 1).coerceAtLeast(0)
            } else {
                (index + 1).coerceAtMost(alphabet.size - 1)
            }
        }
    }
    
    /**
     * 处理字母选择，实时响应触摸位置变化
     * @param index 字母索引
     */
    fun handleLetterSelection(index: Int) {
        if (index < 0 || index >= alphabet.size) return
        
        val letter = alphabet[index]
        if (!availableLetters.contains(letter)) return
        
        // 只有当选择的字母发生变化时才执行操作
        if (currentSelectedIndex != index) {
            currentSelectedIndex = index
            
            // 优化触觉反馈：使用更轻量的反馈类型，减少延迟
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            
            // 立即执行回调，实现实时跳转
            onLetterClick(letter)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .onGloballyPositioned { coordinates ->
                // 记录组件的位置和尺寸信息
                componentHeight = coordinates.size.height.toFloat()
                componentTop = coordinates.localToRoot(Offset.Zero).y
            }
            .pointerInput(Unit) {
                 // 优化触摸处理：合并拖拽和点击手势，提高响应性
                  detectDragGestures(
                      onDragStart = { offset ->
                          // 拖拽开始时立即处理，offset是相对于组件的坐标
                          val index = getLetterIndexFromRelativeY(offset.y)
                          handleLetterSelection(index)
                      },
                      onDragEnd = {
                          // 拖拽结束时不需要特殊处理，选中状态会自动重置
                      },
                      onDrag = { change, _ ->
                          // 拖拽过程中持续处理，消费触摸事件以提高性能
                          change.consume()
                          val index = getLetterIndexFromRelativeY(change.position.y)
                          handleLetterSelection(index)
                      }
                  )
             }
             .pointerInput(Unit) {
                 // 添加独立的点击手势处理，提高点击响应速度
                 detectTapGestures { offset ->
                     val index = getLetterIndexFromRelativeY(offset.y)
                     if (index >= 0 && index < alphabet.size && availableLetters.contains(alphabet[index])) {
                         handleLetterSelection(index)
                     }
                 }
             },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        alphabet.forEachIndexed { index, letter ->
            val isAvailable = availableLetters.contains(letter)
            val isSelected = currentSelectedIndex == index
            
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected && isAvailable -> {
                                // 选中状态的背景色
                                MaterialTheme.colorScheme.primary
                            }
                            isAvailable -> {
                                // 可用状态的背景色
                                MaterialTheme.colorScheme.primaryContainer
                            }
                            else -> {
                                // 不可用状态的透明背景
                                Color.Transparent
                            }
                        }
                    )
                    .pointerInput(index) {
                        // 处理点击手势
                        detectTapGestures {
                            if (isAvailable) {
                                handleLetterSelection(index)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 8.sp,
                    fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected && isAvailable -> {
                            // 选中状态的文字颜色
                            MaterialTheme.colorScheme.onPrimary
                        }
                        isAvailable -> {
                            // 可用状态的文字颜色
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                        else -> {
                            // 不可用状态的文字颜色
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    }
                )
            }
        }
    }
    
    // 优化选中状态重置：减少延迟时间，提高响应性
    LaunchedEffect(currentSelectedIndex) {
        if (currentSelectedIndex >= 0) {
            delay(200) // 减少到200ms，提供更快的视觉反馈重置
            currentSelectedIndex = -1
        }
    }
}