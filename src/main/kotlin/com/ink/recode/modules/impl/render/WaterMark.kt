package com.ink.recode.modules.impl.render

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.event.Listener
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.render.Skia
import com.ink.recode.render.SkiaRenderer
import com.ink.recode.render.FontManager
import org.lwjgl.glfw.GLFW
import java.awt.Color

object WaterMark : Module("WaterMark", "Display client watermark on screen", Category.RENDER) {

    private val fontRegular by lazy { FontManager.getBorel(80f) }
    private val fontMedium by lazy { FontManager.getMedium(24f) }

    init {
        this.enabled = true
        this.key = GLFW.GLFW_KEY_B
    }

    @Listener
    fun onRenderEvent(event: RenderEvent) {
        if (!enabled) return
        if (!SkiaRenderer.isInitialized()) {
            println("[WaterMark] SkiaRenderer not initialized")
            return
        }

        val mc = net.minecraft.client.MinecraftClient.getInstance()

        if (mc.currentScreen != null) return

        try {
            Skia.draw { canvas ->
                val x = 5f
                val y = 5f

                val clientName = "InkRecode"
                val version = "Dev?"
                val fps = mc.currentFps
                val userName = mc.session?.username ?: "Unknown"

                Skia.drawText("ink recode", x, y + 60f, Color(255, 255, 255), fontRegular, 80f)
            }
        } catch (e: Exception) {
            println("[WaterMark] Error rendering: ${e.message}")
            e.printStackTrace()
        }
    }
}