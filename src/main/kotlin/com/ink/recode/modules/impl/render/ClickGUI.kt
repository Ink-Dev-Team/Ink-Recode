package com.ink.recode.modules.impl.render

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.event.Listener
import com.ink.recode.event.events.KeyboardEvent
import com.ink.recode.gui.ClickGUIScreen
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

object ClickGUI : Module("ClickGUI", "Click GUI for module management", Category.RENDER) {

    init {
        this.enabled = false
        this.key = GLFW.GLFW_KEY_O
    }

    @Listener
    fun onKey(event: KeyboardEvent) {
        if (event.key == key) {
            val mc = MinecraftClient.getInstance()
            // 如果当前屏幕已经是ClickGUIScreen，不响应按键事件
            if (mc.currentScreen is ClickGUIScreen) {
                println("[ClickGUI] Ignoring key event - ClickGUIScreen already open")
                return
            }
            println("[ClickGUI] Key pressed, toggling...")
            toggle()
        }
    }

    override fun onEnable() {
        println("[ClickGUI] onEnable called")
        val mc = MinecraftClient.getInstance()
        println("[ClickGUI] MinecraftClient: $mc")
        val screen = ClickGUIScreen()
        println("[ClickGUI] ClickGUIScreen created: $screen")
        mc.setScreen(screen)
        println("[ClickGUI] Screen set to ClickGUIScreen")
        println("[ClickGUI] Current screen: ${mc.currentScreen}")
    }

    override fun onDisable() {
        println("[ClickGUI] onDisable called")
        val mc = MinecraftClient.getInstance()
        println("[ClickGUI] Current screen: ${mc.currentScreen}")
        if (mc.currentScreen is ClickGUIScreen) {
            println("[ClickGUI] Closing ClickGUIScreen")
            mc.setScreen(null)
        } else {
            println("[ClickGUI] Current screen is not ClickGUIScreen")
        }
    }
}
