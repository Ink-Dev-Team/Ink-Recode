package com.ink.recode.modules.impl.render

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.event.Listener
import com.ink.recode.event.events.KeyboardEvent
import com.ink.recode.gui.clickgui.ClickGuiScreen
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.ColorValue
import com.ink.recode.value.NumberValue
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import java.awt.Color

object ClickGUI : Module("ClickGUI", "Click GUI for module management", Category.RENDER) {

    // ClickGUI settings
    @JvmField val backgroundColor = ColorValue("BackgroundColor", Color(40, 40, 40), "Background color of the ClickGUI")
    @JvmField val expandedBackgroundColor = ColorValue("ExpandedBackgroundColor", Color(30, 30, 30), "Background color of expanded modules")
    @JvmField val guiScale = NumberValue("GuiScale", 1.0, 0.5, 2.0, 0.1, "Scale of the ClickGUI")
    @JvmField val fontSize = NumberValue("FontSize", 12.0, 8.0, 20.0, 1.0, "Font size of the ClickGUI")
    @JvmField val backgroundBlur = BooleanValue("BackgroundBlur", false, "Enable background blur")
    @JvmField val blurStrength = NumberValue("BlurStrength", 5.0, 1.0, 10.0, 0.5, "Strength of the background blur")

    init {
        this.enabled = false
        this.key = GLFW.GLFW_KEY_O
        
        // Register settings
        values.add(backgroundColor)
        values.add(expandedBackgroundColor)
        values.add(guiScale)
        values.add(fontSize)
        values.add(backgroundBlur)
        values.add(blurStrength)
    }

    @Listener
    fun onKey(event: KeyboardEvent) {
        if (event.key == key) {
            val mc = MinecraftClient.getInstance()
            println("[ClickGUI] Key pressed, toggling...")
            toggle()
        }
    }

    override fun onEnable() {
        println("[ClickGUI] onEnable called")
        val mc = MinecraftClient.getInstance()
        println("[ClickGUI] MinecraftClient: $mc")
        val screen = ClickGuiScreen()
        println("[ClickGUI] ClickGuiScreen created: $screen")
        mc.setScreen(screen)
        println("[ClickGUI] Screen set to ClickGuiScreen")
        println("[ClickGUI] Current screen: ${mc.currentScreen}")
    }

    override fun onDisable() {
        println("[ClickGUI] onDisable called")
        val mc = MinecraftClient.getInstance()
        println("[ClickGUI] Current screen: ${mc.currentScreen}")
        if (mc.currentScreen is ClickGuiScreen) {
            println("[ClickGUI] Closing ClickGuiScreen")
            mc.setScreen(null)
        } else {
            println("[ClickGUI] Current screen is not ClickGuiScreen")
        }
    }

    // Helper methods for settings
    @JvmStatic
    fun getGuiScale(): Double {
        return guiScale.get()
    }

    @JvmStatic
    fun getFontSize(): Double {
        return fontSize.get()
    }

    @JvmStatic
    fun color(alpha: Int): Color {
        val c = backgroundColor.get()
        return Color(c.red, c.green, c.blue, alpha)
    }

    @JvmStatic
    fun color2(alpha: Int): Color {
        return Color(100, 100, 255, alpha)
    }

    // Getter for enabled property
    @JvmStatic
    fun getEnabled(): Boolean {
        return enabled
    }

    @JvmStatic
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}