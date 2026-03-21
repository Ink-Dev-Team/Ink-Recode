package com.ink.recode.gui

import com.ink.recode.Category
import com.ink.recode.ColorManager
import com.ink.recode.Module
import com.ink.recode.ModuleManager
import com.ink.recode.render.FontManager
import com.ink.recode.render.Skia
import com.ink.recode.render.SkiaRenderer
import io.github.humbleui.skija.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class ClickGUIScreen : Screen(Text.literal("ClickGUI")) {
    
    private val font: Font by lazy { FontManager.getBold(16f) }
    private val titleFont: Font by lazy { FontManager.getMedium(24f) }
    
    private val themeColor = ColorManager.primary
    private val hoverColor = ColorManager.primaryContainer
    private val backgroundColor = ColorManager.surfaceContainer
    private val cardColor = ColorManager.surfaceContainerHigh
    
    private var selectedCategory: Category? = null
    private var selectedModule: Module? = null
    private var windowX = 100f          // 调整初始位置，避免超出屏幕
    private var windowY = 100f
    private var windowWidth = 600f     // 加宽窗口，容纳所有内容
    private var windowHeight = 400f    // 加高窗口
    private var isDragging = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    
    override fun init() {
        println("[ClickGUIScreen] init() called")
        super.init()
        // 确保 Skia 已初始化
        if (!SkiaRenderer.isInitialized()) {
            println("[ClickGUIScreen] Skia not initialized, initializing...")
            SkiaRenderer.init()
        }
        println("[ClickGUIScreen] Skia initialized: ${SkiaRenderer.isInitialized()}")
    }
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        println("[ClickGUI] ========== RENDER START ==========")
        println("[ClickGUI] Screen size: ${width}x${height}")
        println("[ClickGUI] Mouse: $mouseX,$mouseY")
        
        // 1. 先绘制 Minecraft 背景（半透明）
        println("[ClickGUI] Step 1: Rendering background")
        renderBackground(context, mouseX, mouseY, delta)
        
        // 2. 获取窗口缩放因子，适配不同分辨率
        val scale = MinecraftClient.getInstance().window.scaleFactor.toFloat()
        println("[ClickGUI] Step 2: Scale factor: $scale")
        
        // 3. 调试信息
        println("[ClickGUI] Step 3: Drawing debug text")
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("ClickGUI Rendering - Skia: ${SkiaRenderer.isInitialized()}"),
            10, 10, 0xFFFFFF
        )
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("Window: ${windowX},${windowY} ${windowWidth}x${windowHeight}"),
            10, 30, 0xFFFFFF
        )
        context.drawTextWithShadow(
            textRenderer,
            Text.literal("Screen: ${width}x${height} Scale: $scale"),
            10, 50, 0xFFFFFF
        )
        
        // 4. 正确设置 Skia 画布变换
        println("[ClickGUI] Step 4: Skia initialized: ${SkiaRenderer.isInitialized()}")
        if (SkiaRenderer.isInitialized()) {
            try {
                println("[ClickGUI] Step 4a: Drawing Minecraft fill rect")
                // 先使用 Minecraft 原生渲染绘制一个红色矩形，确认 GUI 可见
                context.fill(windowX.toInt(), windowY.toInt(), windowWidth.toInt(), windowHeight.toInt(), 0xFFFF0000.toInt())
                println("[ClickGUI] Step 4b: Minecraft fill rect completed")
                
                println("[ClickGUI] Step 4c: Starting Skia.draw")
                Skia.draw { canvas ->
                    println("[ClickGUI] Step 4d: Inside Skia.draw block")
                    // 绘制一个简单的红色矩形，确认 Skia 渲染工作
                    Skia.drawRoundedRect(100f, 100f, 200f, 100f, 10f, java.awt.Color.RED)
                    println("[ClickGUI] Step 4e: Skia rounded rect drawn")
                    Skia.drawText("Skia Test", 110f, 160f, java.awt.Color.WHITE, font)
                    println("[ClickGUI] Step 4f: Skia text drawn")
                }
                println("[ClickGUI] Step 4g: Skia.draw completed")
            } catch (e: Exception) {
                println("[ClickGUI] ERROR in Skia rendering: ${e.message}")
                e.printStackTrace()
                // 降级渲染：使用 Minecraft 原生渲染显示错误信息
                context.drawTextWithShadow(
                    textRenderer,
                    Text.literal("ClickGUI Error: ${e.message}"),
                    10, 70, 0xFF0000
                )
            }
        } else {
            println("[ClickGUI] Skia NOT initialized!")
            // Skia 未初始化时的降级显示
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Skia renderer not initialized!"),
                width / 2,
                height / 2,
                0xFFFFFF
            )
        }
        
        println("[ClickGUI] Step 5: Calling super.render")
        super.render(context, mouseX, mouseY, delta)
        println("[ClickGUI] ========== RENDER END ==========")
    }
    
    // 重写背景渲染，使用半透明黑色背景
    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0x90000000.toInt()) // 半透明黑色
    }
    
    private fun drawWindow(canvas: Canvas, mouseX: Float, mouseY: Float) {
        println("[ClickGUI] Drawing window at $windowX,$windowY ${windowWidth}x${windowHeight}")
        println("[ClickGUI] Colors - bg: $backgroundColor, theme: $themeColor, text: ${ColorManager.onSurface}")
        
        // 1. 绘制主窗口背景（确保在最底层）
        Skia.drawRoundedRect(windowX, windowY, windowWidth, windowHeight, 10f, backgroundColor)
        
        // 2. 绘制标题栏（可拖拽区域）
        Skia.drawRoundedRect(windowX, windowY, windowWidth, 30f, 10f, themeColor)
        Skia.drawText("ClickGUI", windowX + 10f, windowY + 22f, ColorManager.onSurface, titleFont)
        
        // 3. 处理窗口拖拽
        if (isDragging) {
            windowX = mouseX - dragOffsetX
            windowY = mouseY - dragOffsetY
        }
        
        // 4. 绘制分类标签
        val categories = Category.values()
        val categoryWidth = 100f
        val categoryGap = 10f
        val categoryStartX = windowX + 10f
        val categoryStartY = windowY + 40f

        categories.forEachIndexed { index, category ->
            val x = categoryStartX + index * (categoryWidth + categoryGap)
            val y = categoryStartY

            // 检查分类标签鼠标悬停
            val isHovered = isMouseOver(x, y, categoryWidth, 30f, mouseX, mouseY)
            val color = if (selectedCategory == category) themeColor else if (isHovered) hoverColor else cardColor

            // 绘制分类按钮
            Skia.drawRoundedRect(x, y, categoryWidth, 30f, 5f, color)
            Skia.drawText(category.name, x + 10f, y + 22f, ColorManager.onSurface, font)

            // 5. 绘制选中分类的模块列表
            if (selectedCategory == category) {
                drawModuleList(canvas, categoryStartX, categoryStartY + 40f, category, mouseX, mouseY)
            }
        }

        // 6. 绘制设置面板（如果有选中的模块）
        if (selectedModule != null) {
            drawSettingsPanel(canvas, windowX + windowWidth - 200f, windowY + 40f, selectedModule!!, mouseX, mouseY)
        }
    }
    
    private fun drawModuleList(canvas: Canvas, x: Float, y: Float, category: Category, mouseX: Float, mouseY: Float) {
        val modules = ModuleManager.modules.filter { it.category == category }
        if (modules.isEmpty()) return

        val moduleWidth = 250f    // 加宽模块卡片
        val moduleHeight = 30f    // 加高模块卡片
        val moduleGap = 5f
        val moduleStartX = x + 10f // 调整模块列表起始位置

        modules.forEachIndexed { index, module ->
            val moduleY = y + index * (moduleHeight + moduleGap)
            
            // 限制模块列表在窗口内
            if (moduleY + moduleHeight > windowY + windowHeight) return@forEachIndexed

            // 检查模块鼠标悬停
            val isHovered = isMouseOver(moduleStartX, moduleY, moduleWidth, moduleHeight, mouseX, mouseY)
            val color = if (selectedModule == module) themeColor else if (isHovered) hoverColor else cardColor

            // 绘制模块卡片
            Skia.drawRoundedRect(moduleStartX, moduleY, moduleWidth, moduleHeight, 5f, color)
            
            // 绘制模块名称
            Skia.drawText(module.name, moduleStartX + 10f, moduleY + 20f, ColorManager.onSurface, font)
            
            // 绘制开关状态
            val statusText = if (module.enabled) "ON" else "OFF"
            val statusColor = if (module.enabled) ColorManager.primary else ColorManager.error
            Skia.drawText(statusText, moduleStartX + moduleWidth - 40f, moduleY + 20f, statusColor, font)
        }
    }
    
    private fun drawSettingsPanel(canvas: Canvas, x: Float, y: Float, module: Module, mouseX: Float, mouseY: Float) {
        val panelWidth = 180f
        val panelHeight = 250f

        // 确保设置面板在窗口内
        val panelY = if (y + panelHeight > windowY + windowHeight) windowY + windowHeight - panelHeight - 10f else y

        // 绘制设置面板背景
        Skia.drawRoundedRect(x, panelY, panelWidth, panelHeight, 5f, backgroundColor)
        
        // 绘制标题
        Skia.drawText("${module.name} Settings", x + 10f, panelY + 20f, ColorManager.onSurface, titleFont)
        
        // 示例：绘制模块基础信息
        Skia.drawText("Enabled: ${module.enabled}", x + 10f, panelY + 50f, ColorManager.onSurface, font)
        Skia.drawText("Keybind: ${GLFW.glfwGetKeyName(module.key, 0) ?: "NONE"}", x + 10f, panelY + 80f, ColorManager.onSurface, font)
        Skia.drawText("Category: ${module.category.name}", x + 10f, panelY + 110f, ColorManager.onSurface, font)
    }
    
    // 修复鼠标悬停检测
    private fun isMouseOver(x: Float, y: Float, width: Float, height: Float, mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + height)
    }
    
    // 修复鼠标点击事件
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val scale = MinecraftClient.getInstance().window.scaleFactor.toFloat()
        val scaledX = mouseX.toFloat() / scale
        val scaledY = mouseY.toFloat() / scale
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // 1. 检测标题栏拖拽
            if (isMouseOver(windowX, windowY, windowWidth, 30f, scaledX, scaledY)) {
                isDragging = true
                dragOffsetX = scaledX - windowX
                dragOffsetY = scaledY - windowY
                return true
            }
            
            // 2. 检测分类点击
            val categories = Category.values()
            val categoryWidth = 100f
            val categoryGap = 10f
            val categoryX = windowX + 10f
            val categoryY = windowY + 40f
            
            categories.forEachIndexed { index, category ->
                val x = categoryX + index * (categoryWidth + categoryGap)
                val y = categoryY
                
                if (isMouseOver(x, y, categoryWidth, 30f, scaledX, scaledY)) {
                    selectedCategory = category
                    selectedModule = null // 取消选中的模块
                    return true
                }
            }
            
            // 3. 检测模块点击
            if (selectedCategory != null) {
                val modules = ModuleManager.modules.filter { it.category == selectedCategory }
                val moduleWidth = 250f
                val moduleHeight = 30f
                val moduleGap = 5f
                val moduleX = windowX + 20f
                val moduleY = windowY + 80f
                
                modules.forEachIndexed { index, module ->
                    val y = moduleY + index * (moduleHeight + moduleGap)
                    
                    if (isMouseOver(moduleX, y, moduleWidth, moduleHeight, scaledX, scaledY)) {
                        module.toggle() // 切换模块开关
                        selectedModule = module // 选中模块显示设置
                        return true
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button)
    }
    
    // 修复鼠标释放事件
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isDragging = false
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }
    
    // 修复窗口关闭逻辑
    override fun close() {
        println("[ClickGUIScreen] close() called")
        val mc = MinecraftClient.getInstance()
        // 关闭屏幕时，禁用ClickGUI模块
        if (com.ink.recode.modules.impl.render.ClickGUI.enabled) {
            println("[ClickGUIScreen] Disabling ClickGUI module")
            com.ink.recode.modules.impl.render.ClickGUI.enabled = false
        }
        mc.setScreen(null)
    }
    
    // 允许按 ESC 关闭
    override fun shouldCloseOnEsc(): Boolean {
        return true
    }
    
    // 清理资源
    override fun removed() {
        super.removed()
    }
}