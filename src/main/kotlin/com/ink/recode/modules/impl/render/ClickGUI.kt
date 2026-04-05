package com.ink.recode.modules.impl.render

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.gui.clickgui.ClickGuiScreen
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.ColorValue
import com.ink.recode.value.NumberValue
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import java.awt.Color

object ClickGUI : Module("ClickGUI", "Click GUI for module management", Category.RENDER) {

    @JvmField val backgroundColor = ColorValue("BackgroundColor", Color(35, 35, 45), "Background color of the ClickGUI")
    @JvmField val expandedBackgroundColor = ColorValue("ExpandedBackgroundColor", Color(25, 25, 35), "Background color of expanded modules")
    @JvmField val guiScale = NumberValue("GuiScale", 1.0, 0.5, 2.0, 0.1, "Scale of the ClickGUI")
    @JvmField val fontSize = NumberValue("FontSize", 13.0, 8.0, 20.0, 0.5, "Font size of the ClickGUI")
    @JvmField val backgroundBlur = BooleanValue("BackgroundBlur", false, "Enable background blur")
    @JvmField val blurStrength = NumberValue("BlurStrength", 5.0, 1.0, 10.0, 0.5, "Strength of the background blur")

    init {
        enabled = false
        key = GLFW.GLFW_KEY_O

        values.add(backgroundColor)
        values.add(expandedBackgroundColor)
        values.add(guiScale)
        values.add(fontSize)
        values.add(backgroundBlur)
        values.add(blurStrength)
    }

    override fun onEnable() {
        if(MinecraftClient.getInstance().currentScreen==null){
        val mc = MinecraftClient.getInstance()
        mc.setScreen(ClickGuiScreen())}
    }

    override fun onDisable() {
        val mc = MinecraftClient.getInstance()
        if (mc.currentScreen is ClickGuiScreen) {
            mc.setScreen(null)
        }
    }

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
        return Color(255, 167, 157, alpha)
    }

    @JvmStatic
    fun color2(alpha: Int): Color {
        return Color(64, 198, 255, alpha)
    }
}
//67676767676767767676767676767676767767676767676767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767767