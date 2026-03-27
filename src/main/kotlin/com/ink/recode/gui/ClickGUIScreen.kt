package com.ink.recode.gui

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.ModuleManager
import com.ink.recode.render.FontManager
import com.ink.recode.render.Skia
import com.ink.recode.utils.RotationManager
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.ModeValue
import com.ink.recode.value.NumberValue
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.floor

class ClickGUIScreen : Screen(Text.literal("ClickGUI")) {
    
    private val modules = ModuleManager.modules
    private val categories = Category.values()
    private val categoryWidth = 120
    private val categoryHeight = 20
    private val moduleHeight = 18
    private val categorySpacing = 10
    private val categoryX = 20
    private val categoryY = 20
    
    // 颜色设置
    private val backgroundColor = Color(30, 30, 30, 200)
    private val categoryColor = Color(40, 40, 40, 220)
    private val moduleColor = Color(50, 50, 50, 200)
    private val moduleEnabledColor = Color(100, 100, 255, 200)
    private val textColor = Color(255, 255, 255, 255)
    private val hoverColor = Color(60, 60, 60, 200)
    
    // 鼠标位置
    private var mouseX = 0
    private var mouseY = 0
    
    // 展开的分类
    private val expandedCategories = mutableSetOf<Category>()
    
    // 正在编辑的数值
    private var editingValue: NumberValue? = null
    private var editingModule: Module? = null
    
    // 字体大小
    private val fontSize = 14f
    
    // 字体
    private val font by lazy { FontManager.getRegular(fontSize) }
    private val fontBold by lazy { FontManager.getBold(fontSize) }
    
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.mouseX = mouseX
        this.mouseY = mouseY
        
        // 渲染背景
        Skia.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 0f, backgroundColor)
        
        // 渲染分类
        categories.forEachIndexed { index, category ->
            val x = categoryX + (categoryWidth + categorySpacing) * index
            val y = categoryY
            
            // 渲染分类背景
            val isExpanded = expandedCategories.contains(category)
            val categoryBackground = if (isExpanded) categoryColor.brighter() else categoryColor
            Skia.drawRoundRect(x.toFloat(), y.toFloat(), categoryWidth.toFloat(), categoryHeight.toFloat(), 4f, categoryBackground)
            
            // 渲染分类名称
            val categoryText = "${category.name}"
            val textWidth = Skia.getTextWidth(categoryText, fontBold, fontSize)
            val textX = (x + (categoryWidth - textWidth) / 2).toFloat()
            val textY = (y + (categoryHeight - fontSize) / 2).toFloat()
            Skia.drawText(categoryText, textX, textY + fontSize, textColor, fontBold, fontSize)
            
            // 渲染展开/收起按钮
            val arrowText = if (isExpanded) "▼" else "▶"
            val arrowWidth = Skia.getTextWidth(arrowText, font, fontSize)
            val arrowX = (x + categoryWidth - arrowWidth - 5).toFloat()
            val arrowY = (y + (categoryHeight - fontSize) / 2).toFloat()
            Skia.drawText(arrowText, arrowX, arrowY + fontSize, textColor, font, fontSize)
            
            // 渲染模块
            if (isExpanded) {
                val categoryModules = modules.filter { it.category == category }
                categoryModules.forEachIndexed { moduleIndex, module ->
                    val moduleY = y + categoryHeight + moduleIndex * moduleHeight
                    
                    // 渲染模块背景
                    val moduleBackground = if (isHovering(x, moduleY, categoryWidth, moduleHeight)) {
                        hoverColor
                    } else if (module.enabled) {
                        moduleEnabledColor
                    } else {
                        moduleColor
                    }
                    Skia.drawRoundRect(x.toFloat(), moduleY.toFloat(), categoryWidth.toFloat(), moduleHeight.toFloat(), 2f, moduleBackground)
                    
                    // 渲染模块名称
                    val moduleText = module.name
                    val moduleTextWidth = Skia.getTextWidth(moduleText, font, fontSize)
                    val moduleTextX = (x + 5).toFloat()
                    val moduleTextY = (moduleY + (moduleHeight - fontSize) / 2).toFloat()
                    Skia.drawText(moduleText, moduleTextX, moduleTextY + fontSize, textColor, font, fontSize)
                    
                    // 渲染模块状态
                    val stateText = if (module.enabled) "ON" else "OFF"
                    val stateTextWidth = Skia.getTextWidth(stateText, font, fontSize)
                    val stateTextX = (x + categoryWidth - stateTextWidth - 5).toFloat()
                    val stateTextY = (moduleY + (moduleHeight - fontSize) / 2).toFloat()
                    Skia.drawText(stateText, stateTextX, stateTextY + fontSize, textColor, font, fontSize)
                    
                    // 渲染模块设置
                    if (module.enabled && module.values.isNotEmpty()) {
                        module.values.forEachIndexed { valueIndex, value ->
                            val valueY = moduleY + moduleHeight + valueIndex * moduleHeight
                            
                            // 渲染设置背景
                            val valueBackground = if (isHovering(x, valueY, categoryWidth, moduleHeight)) {
                                hoverColor
                            } else {
                                moduleColor.darker()
                            }
                            Skia.drawRoundRect(x.toFloat(), valueY.toFloat(), categoryWidth.toFloat(), moduleHeight.toFloat(), 2f, valueBackground)
                            
                            // 渲染设置名称
                            val valueText = value.name
                            val valueTextWidth = Skia.getTextWidth(valueText, font, fontSize)
                            val valueTextX = (x + 5).toFloat()
                            val valueTextY = (valueY + (moduleHeight - fontSize) / 2).toFloat()
                            Skia.drawText(valueText, valueTextX, valueTextY + fontSize, textColor, font, fontSize)
                            
                            // 渲染设置值
                            when (value) {
                                is BooleanValue -> {
                                    val boolText = if (value.get()) "ON" else "OFF"
                                    val boolTextWidth = Skia.getTextWidth(boolText, font, fontSize)
                                    val boolTextX = (x + categoryWidth - boolTextWidth - 5).toFloat()
                                    val boolTextY = (valueY + (moduleHeight - fontSize) / 2).toFloat()
                                    Skia.drawText(boolText, boolTextX, boolTextY + fontSize, textColor, font, fontSize)
                                }
                                is NumberValue -> {
                                    val numText = value.get().toString()
                                    val numTextWidth = Skia.getTextWidth(numText, font, fontSize)
                                    val numTextX = (x + categoryWidth - numTextWidth - 5).toFloat()
                                    val numTextY = (valueY + (moduleHeight - fontSize) / 2).toFloat()
                                    Skia.drawText(numText, numTextX, numTextY + fontSize, textColor, font, fontSize)
                                }
                                is ModeValue -> {
                                    val modeText = value.current
                                    val modeTextWidth = Skia.getTextWidth(modeText, font, fontSize)
                                    val modeTextX = (x + categoryWidth - modeTextWidth - 5).toFloat()
                                    val modeTextY = (valueY + (moduleHeight - fontSize) / 2).toFloat()
                                    Skia.drawText(modeText, modeTextX, modeTextY + fontSize, textColor, font, fontSize)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 渲染编辑中的数值
        editingValue?.let { value ->
            val module = editingModule ?: return@let
            val category = module.category
            val categoryIndex = categories.indexOf(category)
            val x = categoryX + (categoryWidth + categorySpacing) * categoryIndex
            val categoryModules = modules.filter { it.category == category }
            val moduleIndex = categoryModules.indexOf(module)
            val moduleY = categoryY + categoryHeight + moduleIndex * moduleHeight
            val valueIndex = module.values.indexOf(value)
            val valueY = moduleY + moduleHeight + valueIndex * moduleHeight
            
            // 渲染数值编辑框
            Skia.drawRoundRect(x.toFloat(), valueY.toFloat(), categoryWidth.toFloat(), moduleHeight.toFloat(), 2f, Color(70, 70, 70, 200))
            
            // 渲染数值名称
            val valueText = value.name
            val valueTextWidth = Skia.getTextWidth(valueText, font, fontSize)
            val valueTextX = (x + 5).toFloat()
            val valueTextY = (valueY + (moduleHeight - fontSize) / 2).toFloat()
            Skia.drawText(valueText, valueTextX, valueTextY + fontSize, textColor, font, fontSize)
            
            // 渲染数值输入
            val numText = value.get().toString()
            val numTextWidth = Skia.getTextWidth(numText, fontBold, fontSize)
            val numTextX = (x + categoryWidth - numTextWidth - 5).toFloat()
            val numTextY = (valueY + (moduleHeight - fontSize) / 2).toFloat()
            Skia.drawText(numText, numTextX, numTextY + fontSize, Color(100, 100, 255, 255), fontBold, fontSize)
        }
    }
    
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            categories.forEachIndexed { index, category ->
                val x = categoryX + (categoryWidth + categorySpacing) * index
                val y = categoryY
                
                // 检查是否点击了分类
                if (isHovering(x, y, categoryWidth, categoryHeight)) {
                    if (expandedCategories.contains(category)) {
                        expandedCategories.remove(category)
                    } else {
                        expandedCategories.add(category)
                    }
                    return true
                }
                
                // 检查是否点击了模块
                if (expandedCategories.contains(category)) {
                    val categoryModules = modules.filter { it.category == category }
                    categoryModules.forEachIndexed { moduleIndex, module ->
                        val moduleY = y + categoryHeight + moduleIndex * moduleHeight
                        
                        if (isHovering(x, moduleY, categoryWidth, moduleHeight)) {
                            module.toggle()
                            return true
                        }
                        
                        // 检查是否点击了模块设置
                        if (module.enabled && module.values.isNotEmpty()) {
                            module.values.forEachIndexed { valueIndex, value ->
                                val valueY = moduleY + moduleHeight + valueIndex * moduleHeight
                                
                                if (isHovering(x, valueY, categoryWidth, moduleHeight)) {
                                    when (value) {
                                        is BooleanValue -> {
                                            value.toggle()
                                        }
                                        is NumberValue -> {
                                            editingValue = value
                                            editingModule = module
                                        }
                                        is ModeValue -> {
                                            value.next()
                                        }
                                    }
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button)
    }
    
    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        // 处理鼠标滚轮
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }
    
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (editingValue != null) {
            when (keyCode) {
                GLFW.GLFW_KEY_ESCAPE -> {
                    editingValue = null
                    editingModule = null
                    return true
                }
                GLFW.GLFW_KEY_ENTER -> {
                    editingValue = null
                    editingModule = null
                    return true
                }
                GLFW.GLFW_KEY_UP -> {
                    editingValue?.add()
                    return true
                }
                GLFW.GLFW_KEY_DOWN -> {
                    editingValue?.sub()
                    return true
                }
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
    
    override fun charTyped(chars: Char, modifiers: Int): Boolean {
        if (editingValue != null) {
            // 处理字符输入
        }
        
        return super.charTyped(chars, modifiers)
    }
    
    override fun close() {
        RotationManager.reset()
        super.close()
    }
    
    private fun isHovering(x: Int, y: Int, width: Int, height: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }
}