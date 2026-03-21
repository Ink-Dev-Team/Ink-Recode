package com.ink.recode.modules.impl.render

import com.ink.recode.Category
import com.ink.recode.ColorManager
import com.ink.recode.Module
import com.ink.recode.event.Listener
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.modules.impl.combat.KillAura
import com.ink.recode.render.Skia
import com.ink.recode.render.SkiaRenderer
import com.ink.recode.render.FontManager
import io.github.humbleui.skija.Font
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.lang.reflect.Field

// 目标HUD显示模块 - 准心右下角版
object Thud : Module("Thud", "Target HUD display", Category.RENDER) {

    // 加载字体资源（小巧字体）
    private val fontBold: Font by lazy { FontManager.getBold(14f) }
    private val fontMedium: Font by lazy { FontManager.getMedium(10f) }

    // 初始化模块
    init {
        this.enabled = true
        this.key = GLFW.GLFW_KEY_H
    }

    // 通过反射获取KillAura的目标
    private fun getKillAuraTarget(): LivingEntity? {
        return try {
            val targetField: Field = KillAura.javaClass.getDeclaredField("target")
            targetField.isAccessible = true
            targetField.get(KillAura) as? LivingEntity
        } catch (e: Exception) {
            null
        }
    }

    // 根据生命值获取渐变颜色
    private fun getHealthColor(health: Float, maxHealth: Float): Color {
        val percentage = (health / maxHealth).coerceIn(0f, 1f)
        return when {
            percentage <= 0.2 -> ColorManager.error // 低血量 - 红色
            percentage <= 0.5 -> Color(255, 165, 0) // 中等血量 - 橙色
            else -> Color(76, 175, 80) // 高血量 - 绿色
        }
    }

    // 渲染事件监听器
    @Listener
    fun onRenderEvent(event: RenderEvent) {
        // 检查模块是否启用
        if (!enabled) return
        // 检查Skia渲染器是否初始化
        if (!SkiaRenderer.isInitialized()) {
            println("[Thud] SkiaRenderer not initialized")
            return
        }

        // 获取Minecraft客户端实例
        val mc = MinecraftClient.getInstance()

        // 如果打开了菜单，不渲染
        if (mc.currentScreen != null) return

        // 获取KillAura的目标
        val target = getKillAuraTarget()
        if (target == null) return

        try {
            // 开始绘制
            Skia.draw { canvas ->
                // 获取窗口尺寸
                val windowWidth = mc.window.scaledWidth.toFloat()
                val windowHeight = mc.window.scaledHeight.toFloat()
                
                // 计算屏幕中心点（准心位置）
                val centerX = windowWidth / 2f
                val centerY = windowHeight / 2f
                
                // 定义面板位置和尺寸（准心右下角）
                val offsetX = 15f  // 准心右侧偏移
                val offsetY = 15f  // 准心下方偏移
                val boxWidth = 85f  // 面板宽度
                val boxHeight = 40f // 面板高度
                
                // 准心右下角位置计算
                val x = centerX + offsetX
                val y = centerY + offsetY

                // 定义颜色
                val bgColor = Color(ColorManager.surfaceContainerLow.red, ColorManager.surfaceContainerLow.green, ColorManager.surfaceContainerLow.blue, 200)
                val borderColor = ColorManager.outline
                val textColor = ColorManager.onSurface

                // 绘制背景和边框（精致边框）
                Skia.drawRoundedRect(x, y, x + boxWidth, y + boxHeight, 4f, bgColor)
                Skia.drawOutline(x, y, boxWidth, boxHeight, 4f, 0.8f, borderColor)

                // 获取目标信息
                val targetName = target.name?.string ?: "Unknown"
                val health = target.health
                val maxHealth = target.maxHealth
                val distance = mc.player?.distanceTo(target)?.let { String.format("%.1f", it) } ?: "0.0"
                
                // 获取生命值颜色
                val healthColor = getHealthColor(health, maxHealth)

                // 绘制目标名称（缩短过长的名称）
                val displayName = if (targetName.length > 8) targetName.substring(0, 8) + "..." else targetName
                Skia.drawText(displayName, x + 6f, y + 14f, textColor, fontBold)
                
                // 绘制距离
                Skia.drawText("§7${distance}m", x + 6f, y + 26f, textColor, fontMedium)

                // 计算生命条参数
                val healthBarWidth = boxWidth - 12f
                val healthBarHeight = 2.5f
                val healthBarX = x + 6f
                val healthBarY = y + boxHeight - 8f

                // 计算生命条宽度
                val healthPercentage = (health / maxHealth).coerceIn(0f, 1f)
                val currentHealthBarWidth = healthBarWidth * healthPercentage

                // 绘制生命条背景和前景
                Skia.drawRoundedRect(healthBarX, healthBarY, healthBarX + healthBarWidth, healthBarY + healthBarHeight, 2f, ColorManager.surfaceVariant)
                if (currentHealthBarWidth > 1f) {
                    Skia.drawRoundedRect(healthBarX, healthBarY, healthBarX + currentHealthBarWidth, healthBarY + healthBarHeight, 2f, healthColor)
                }
                
                // 在生命条上显示生命值百分比
                val healthText = String.format("%.0f%%", healthPercentage * 100)
                val textBounds = Skia.getTextBounds(healthText, fontMedium)
                val textX = healthBarX + healthBarWidth - textBounds.width - 2f
                Skia.drawText(healthText, textX, healthBarY - 1f, textColor, fontMedium)
            }
        } catch (e: Exception) {
            // 错误处理
            println("[Thud] Error rendering: ${e.message}")
            e.printStackTrace()
        }
    }
}