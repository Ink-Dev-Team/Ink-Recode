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
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.lang.reflect.Field

// 目标HUD显示模块 - 准心右下角版
object Thud : Module("Thud", "Target HUD display", Category.RENDER) {

    // 加载字体资源（小巧字体）
    private val fontBold by lazy { FontManager.getBold(14f) }
    private val fontMedium by lazy { FontManager.getMedium(10f) }

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
        return ColorManager.primary
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

        val windowWidth = mc.window.scaledWidth.toFloat()
        val windowHeight = mc.window.scaledHeight.toFloat()

        try {
            // 开始绘制
            Skia.draw { canvas ->
                // 计算屏幕中心点（准心位置）
                val centerX = windowWidth / 2f
                val centerY = windowHeight / 2f

                val scale=4.5f;

                // 定义面板位置和尺寸（准心右下角）
                val offsetX = 1.5*windowWidth  // 准心右侧偏移
                val offsetY = 1.5*windowHeight  // 准心下方偏移
                val boxWidth = (16*scale) // 面板宽度
                val boxHeight = (9*scale) // 面板高度
                
                // 准心右下角位置计算
                val x: Double = centerX + offsetX
                val y: Double = centerY + offsetY

                // 定义颜色
                val bgColor = ColorManager.secondaryContainer
                val borderColor = ColorManager.outline
                val textColor = ColorManager.onSurface

                // 绘制背景和边框（精致边框）
                Skia.drawRoundRect(x.toFloat(), y.toFloat(), boxWidth, boxHeight, 4f, bgColor)

                // 获取目标信息
                val targetName = target.name?.string ?: "Unknown"
                val health = target.health
                val maxHealth = target.maxHealth
                val distance = mc.player?.distanceTo(target)?.let { String.format("%.1f", it) } ?: "0.0"
                
                //draw the fucking container
                Skia.drawRoundRect(x.toFloat(), y.toFloat(), 120f, 90f, 10f, bgColor)
            }
        } catch (e: Exception) {
            // 错误处理
            println("[Thud] Error rendering: ${e.message}")
            e.printStackTrace()
        }
    }
}