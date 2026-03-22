package com.ink.recode.gui

import com.ink.recode.Category
import com.ink.recode.ColorManager
import com.ink.recode.Module
import com.ink.recode.ModuleManager
import com.ink.recode.render.FontManager
import com.ink.recode.render.Skia
import com.ink.recode.render.SkiaRenderer
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.NumberValue
import com.ink.recode.value.ModeValue
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Font
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

val Category.displayName: String
    get() = when (this) {
        Category.COMBAT -> "战斗"
        Category.MOVEMENT -> "移动"
        Category.RENDER -> "渲染"
        Category.PLAYER -> "玩家"
    }

class ClickGUIScreen : Screen(Text.literal("ClickGUI")) {
    
    private val font: Font by lazy { FontManager.getBold(14f) }
    private val titleFont: Font by lazy { FontManager.getMedium(20f) }
    
    private val themeColor = ColorManager.primary
    private val hoverColor = ColorManager.primaryContainer
    private val backgroundColor = ColorManager.surfaceContainer
    private val cardColor = ColorManager.surfaceContainerHigh
    private val textColor = ColorManager.onSurface
    private val disabledColor = ColorManager.onSurfaceVariant
    
    private var selectedCategory: Category = Category.COMBAT
    private var selectedModule: Module? = null
    
    private var windowX = 100f
    private var windowY = 100f
    private var windowWidth = 700f
    private var windowHeight = 500f
    
    private var isDragging = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    
    private var scrollOffset = 0f
    private var maxScroll = 0f
    
    override fun init() {
        super.init()
        if (!SkiaRenderer.isInitialized()) {
            SkiaRenderer.init()
        }
    }
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        
        if (SkiaRenderer.isInitialized()) {
            Skia.draw { canvas ->
                drawWindow(canvas, mouseX.toFloat(), mouseY.toFloat())
            }
        }
        
        super.render(context, mouseX, mouseY, delta)
    }
    
    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0x90000000.toInt())
    }
    
    private fun drawWindow(canvas: Canvas, mouseX: Float, mouseY: Float) {
        val scale = MinecraftClient.getInstance().window.scaleFactor.toFloat()
        val scaledX = mouseX / scale
        val scaledY = mouseY / scale
        
        Skia.drawRoundedRect(windowX, windowY, windowWidth, windowHeight, 12f, backgroundColor)
        Skia.drawOutline(windowX, windowY, windowWidth, windowHeight, 12f, 1f, ColorManager.outline)
        
        drawTitleBar(canvas, scaledX, scaledY)
        drawCategories(canvas, scaledX, scaledY)
        drawModuleList(canvas, scaledX, scaledY)
        
        if (selectedModule != null) {
            drawSettingsPanel(canvas, scaledX, scaledY)
        }
    }
    
    private fun drawTitleBar(canvas: Canvas, mouseX: Float, mouseY: Float) {
        Skia.drawRoundedRect(windowX, windowY, windowWidth, 40f, 12f, themeColor)
        Skia.drawText("ClickGUI", windowX + 15f, windowY + 27f, textColor, titleFont)
        
        val closeX = windowX + windowWidth - 35f
        val closeY = windowY + 10f
        val isHovered = isMouseOver(closeX, closeY, 25f, 20f, mouseX, mouseY)
        
        Skia.drawRoundedRect(closeX, closeY, 25f, 20f, 5f, if (isHovered) ColorManager.error else ColorManager.errorContainer)
        Skia.drawText("X", closeX + 8f, closeY + 15f, ColorManager.onError, font)
    }
    
    private fun drawCategories(canvas: Canvas, mouseX: Float, mouseY: Float) {
        val categories = Category.values()
        val categoryWidth = 100f
        val categoryHeight = 30f
        val categoryGap = 8f
        val startX = windowX + 15f
        val startY = windowY + 50f
        
        categories.forEachIndexed { index, category ->
            val x = startX + index * (categoryWidth + categoryGap)
            val y = startY
            
            val isHovered = isMouseOver(x, y, categoryWidth, categoryHeight, mouseX, mouseY)
            val color = when {
                selectedCategory == category -> themeColor
                isHovered -> hoverColor
                else -> cardColor
            }
            
            Skia.drawRoundedRect(x, y, categoryWidth, categoryHeight, 6f, color)
            Skia.drawText(category.displayName, x + 10f, y + 21f, textColor, font)
        }
    }
    
    private fun drawModuleList(canvas: Canvas, mouseX: Float, mouseY: Float) {
        val modules = ModuleManager.modules.filter { it.category == selectedCategory }
        if (modules.isEmpty()) {
            val emptyText = "No modules in ${selectedCategory.displayName}"
            val textBounds = Skia.getTextBounds(emptyText, font)
            val textWidth = textBounds.width
            Skia.drawText(emptyText, windowX + windowWidth / 2f - textWidth / 2f, windowY + 100f, disabledColor, font)
            return
        }
        
        val moduleWidth = 280f
        val moduleHeight = 35f
        val moduleGap = 6f
        val startX = windowX + 15f
        val startY = windowY + 90f
        
        val visibleModules = modules.take(12)
        val totalHeight = visibleModules.size * (moduleHeight + moduleGap)
        maxScroll = (modules.size - visibleModules.size) * (moduleHeight + moduleGap)
        
        visibleModules.forEachIndexed { index, module ->
            val y = startY + index * (moduleHeight + moduleGap) - scrollOffset
            
            if (y < windowY + 40f) return@forEachIndexed
            if (y + moduleHeight > windowY + windowHeight - 40f) return@forEachIndexed
            
            val isHovered = isMouseOver(startX, y, moduleWidth, moduleHeight, mouseX, mouseY)
            val isSelected = selectedModule == module
            val color = when {
                isSelected -> themeColor
                isHovered -> hoverColor
                else -> cardColor
            }
            
            Skia.drawRoundedRect(startX, y, moduleWidth, moduleHeight, 6f, color)
            Skia.drawText(module.name, startX + 12f, y + 23f, textColor, font)
            
            val statusX = startX + moduleWidth - 50f
            val statusColor = if (module.enabled) ColorManager.primary else ColorManager.error
            val statusText = if (module.enabled) "ON" else "OFF"
            Skia.drawText(statusText, statusX, y + 23f, statusColor, font)
        }
        
        if (modules.size > 12) {
            drawScrollBar(canvas, mouseX, mouseY)
        }
    }
    
    private fun drawScrollBar(canvas: Canvas, mouseX: Float, mouseY: Float) {
        val barWidth = 8f
        val barHeight = windowHeight - 140f
        val barX = windowX + windowWidth - 25f
        val barY = windowY + 90f
        
        Skia.drawRoundedRect(barX, barY, barWidth, barHeight, 4f, cardColor)
        
        val scrollRatio = scrollOffset / maxScroll.coerceAtLeast(1f)
        val handleHeight = (barHeight * 12f / ModuleManager.modules.size).coerceAtLeast(20f)
        val handleY = barY + scrollRatio * (barHeight - handleHeight)
        
        Skia.drawRoundedRect(barX, handleY, barWidth, handleHeight, 4f, themeColor)
    }
    
    private fun drawSettingsPanel(canvas: Canvas, mouseX: Float, mouseY: Float) {
        val module = selectedModule ?: return
        val panelWidth = 320f
        val panelHeight = windowHeight - 100f
        val panelX = windowX + windowWidth - panelWidth - 15f
        val panelY = windowY + 50f
        
        Skia.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 8f, backgroundColor)
        Skia.drawOutline(panelX, panelY, panelWidth, panelHeight, 8f, 1f, ColorManager.outline)
        
        Skia.drawText("${module.name} Settings", panelX + 15f, panelY + 25f, textColor, titleFont)
        
        val values = module.values
        if (values.isEmpty()) {
            Skia.drawText("No settings available", panelX + 15f, panelY + 60f, disabledColor, font)
            return
        }
        
        var valueY = panelY + 50f
        values.forEach { value ->
            if (valueY > panelY + panelHeight - 20f) return@forEach
            
            when (value) {
                is BooleanValue -> drawBooleanValue(canvas, value, panelX + 15f, valueY, mouseX, mouseY)
                is NumberValue -> drawNumberValue(canvas, value, panelX + 15f, valueY, mouseX, mouseY)
                is ModeValue -> drawModeValue(canvas, value, panelX + 15f, valueY, mouseX, mouseY)
            }
            valueY += 45f
        }
    }
    
    private fun drawBooleanValue(canvas: Canvas, value: BooleanValue, x: Float, y: Float, mouseX: Float, mouseY: Float) {
        val switchWidth = 40f
        val switchHeight = 20f
        val isHovered = isMouseOver(x, y, switchWidth, switchHeight, mouseX, mouseY)
        
        Skia.drawText(value.name, x, y + 15f, textColor, font)
        
        val switchColor = if (value.get()) ColorManager.primary else ColorManager.surfaceVariant
        Skia.drawRoundedRect(x + 200f, y, switchWidth, switchHeight, 10f, switchColor)
        
        val indicatorX = if (value.get()) x + 220f else x + 205f
        Skia.drawRoundedRect(indicatorX, y + 2f, 16f, 16f, 8f, ColorManager.onPrimary)
    }
    
    private fun drawNumberValue(canvas: Canvas, value: NumberValue, x: Float, y: Float, mouseX: Float, mouseY: Float) {
        val sliderWidth = 180f
        val sliderHeight = 8f
        
        Skia.drawText(value.name, x, y + 15f, textColor, font)
        Skia.drawText(String.format("%.1f", value.get()), x + 240f, y + 15f, themeColor, font)
        
        val progress = (value.get() - value.min) / (value.max - value.min).coerceAtLeast(0.01f)
        val filledWidth = sliderWidth * progress.coerceIn(0f, 1f)
        
        Skia.drawRoundedRect(x + 200f, y + 10f, sliderWidth, sliderHeight, 4f, ColorManager.surfaceVariant)
        Skia.drawRoundedRect(x + 200f, y + 10f, filledWidth, sliderHeight, 4f, themeColor)
    }
    
    private fun drawModeValue(canvas: Canvas, value: ModeValue, x: Float, y: Float, mouseX: Float, mouseY: Float) {
        val dropdownWidth = 150f
        val dropdownHeight = 25f
        
        Skia.drawText(value.name, x, y + 15f, textColor, font)
        
        Skia.drawRoundedRect(x + 200f, y, dropdownWidth, dropdownHeight, 5f, cardColor)
        Skia.drawOutline(x + 200f, y, dropdownWidth, dropdownHeight, 5f, 1f, ColorManager.outline)
        
        val currentMode = value.current
        Skia.drawText(currentMode, x + 210f, y + 17f, textColor, font)
    }
    
    private fun isMouseOver(x: Float, y: Float, width: Float, height: Float, mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }
    
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val scale = MinecraftClient.getInstance().window.scaleFactor.toFloat()
        val scaledX = mouseX.toFloat() / scale
        val scaledY = mouseY.toFloat() / scale
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (handleTitleBarClick(scaledX, scaledY)) return true
            if (handleCategoryClick(scaledX, scaledY)) return true
            if (handleModuleClick(scaledX, scaledY)) return true
            if (handleSettingsClick(scaledX, scaledY)) return true
        }
        
        return super.mouseClicked(mouseX, mouseY, button)
    }
    
    private fun handleTitleBarClick(mouseX: Float, mouseY: Float): Boolean {
        if (isMouseOver(windowX, windowY, windowWidth, 40f, mouseX, mouseY)) {
            val closeX = windowX + windowWidth - 35f
            val closeY = windowY + 10f
            
            if (isMouseOver(closeX, closeY, 25f, 20f, mouseX, mouseY)) {
                close()
                return true
            }
            
            isDragging = true
            dragOffsetX = mouseX - windowX
            dragOffsetY = mouseY - windowY
            return true
        }
        return false
    }
    
    private fun handleCategoryClick(mouseX: Float, mouseY: Float): Boolean {
        val categories = Category.values()
        val categoryWidth = 100f
        val categoryHeight = 30f
        val categoryGap = 8f
        val startX = windowX + 15f
        val startY = windowY + 50f
        
        categories.forEachIndexed { index, category ->
            val x = startX + index * (categoryWidth + categoryGap)
            val y = startY
            
            if (isMouseOver(x, y, categoryWidth, categoryHeight, mouseX, mouseY)) {
                selectedCategory = category
                selectedModule = null
                scrollOffset = 0f
                return true
            }
        }
        return false
    }
    
    private fun handleModuleClick(mouseX: Float, mouseY: Float): Boolean {
        val modules = ModuleManager.modules.filter { it.category == selectedCategory }
        if (modules.isEmpty()) return false
        
        val moduleWidth = 280f
        val moduleHeight = 35f
        val moduleGap = 6f
        val startX = windowX + 15f
        val startY = windowY + 90f
        
        val visibleModules = modules.take(12)
        visibleModules.forEachIndexed { index, module ->
            val y = startY + index * (moduleHeight + moduleGap) - scrollOffset
            
            if (y < windowY + 40f) return@forEachIndexed
            if (y + moduleHeight > windowY + windowHeight - 40f) return@forEachIndexed
            
            if (isMouseOver(startX, y, moduleWidth, moduleHeight, mouseX, mouseY)) {
                selectedModule = module
                return true
            }
        }
        
        return false
    }
    
    private fun handleSettingsClick(mouseX: Float, mouseY: Float): Boolean {
        val module = selectedModule ?: return false
        val panelWidth = 320f
        val panelHeight = windowHeight - 100f
        val panelX = windowX + windowWidth - panelWidth - 15f
        val panelY = windowY + 50f
        
        val values = module.values
        if (values.isEmpty()) return false
        
        var valueY = panelY + 50f
        values.forEach { value ->
            if (valueY > panelY + panelHeight - 20f) return@forEach
            
            when (value) {
                is BooleanValue -> {
                    val switchWidth = 40f
                    val switchHeight = 20f
                    if (isMouseOver(panelX + 200f, valueY, switchWidth, switchHeight, mouseX, mouseY)) {
                        value.set(!value.get())
                        return true
                    }
                }
                is NumberValue -> {
                    val sliderWidth = 180f
                    val sliderHeight = 8f
                    if (isMouseOver(panelX + 200f, valueY + 10f, sliderWidth, sliderHeight, mouseX, mouseY)) {
                        val progress = (mouseX - (panelX + 200f)) / sliderWidth
                        val newValue = value.min + progress * (value.max - value.min)
                        value.set(newValue.coerceIn(value.min, value.max))
                        return true
                    }
                }
                is ModeValue -> {
                    val dropdownWidth = 150f
                    val dropdownHeight = 25f
                    if (isMouseOver(panelX + 200f, valueY, dropdownWidth, dropdownHeight, mouseX, mouseY)) {
                        val currentIndex = value.get()
                        val nextIndex = (currentIndex + 1) % value.options.size
                        value.set(nextIndex)
                        return true
                    }
                }
            }
            valueY += 45f
        }
        
        return false
    }
    
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isDragging = false
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }
    
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (isDragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            val scale = MinecraftClient.getInstance().window.scaleFactor.toFloat()
            windowX = (mouseX / scale).toFloat() - dragOffsetX
            windowY = (mouseY / scale).toFloat() - dragOffsetY
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }
    
    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val scale = MinecraftClient.getInstance().window.scaleFactor.toFloat()
        val scaledX = mouseX / scale
        val scaledY = mouseY / scale
        
        if (scaledX > windowX + windowWidth - 50f && scaledX < windowX + windowWidth) {
            if (scaledY > windowY + 90f && scaledY < windowY + windowHeight - 50f) {
                scrollOffset = (scrollOffset - verticalAmount.toFloat() * 20f).coerceIn(0f, maxScroll)
                return true
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }
    
    override fun close() {
        val mc = MinecraftClient.getInstance()
        if (com.ink.recode.modules.impl.render.ClickGUI.enabled) {
            com.ink.recode.modules.impl.render.ClickGUI.enabled = false
        }
        mc.setScreen(null)
    }
    
    override fun shouldCloseOnEsc(): Boolean {
        return true
    }
}
