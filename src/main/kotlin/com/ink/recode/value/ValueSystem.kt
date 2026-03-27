package com.ink.recode.value

import java.awt.Color

/**
 * 配置值系统：ModeValue（多选一）、BooleanValue（开关）、NumberValue（数值）、ColorValue（颜色）、StringValue（字符串）
 */

/** 所有配置值的基类 */
abstract class Value<T>(open val name: String, open val description: String = "") {
    abstract fun get(): T
    abstract fun set(v: T)
    open fun isAvailable(): Boolean = true
}

/**
 * 多选一：从若干选项中选一个，用 index 表示当前项
 */
class ModeValue(
    override val name: String,
    @JvmField val options: List<String>,
    defaultIndex: Int = 0,
    override val description: String = ""
) : Value<Int>(name, description) {

    private var index: Int = defaultIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0))

    override fun get(): Int = index
    override fun set(v: Int) {
        index = v.coerceIn(0, (options.size - 1).coerceAtLeast(0))
    }

    /** 当前选中的选项文案 */
    val current: String get() = options.getOrNull(index) ?: ""

    /** 切换到下一个选项（循环） */
    fun cycle() {
        if (options.isEmpty()) return
        index = (index + 1) % options.size
    }

    fun setByOption(option: String) {
        val i = options.indexOf(option)
        if (i >= 0) index = i
    }
    
    /** 切换到下一个选项 */
    fun next() {
        cycle()
    }
}

/**
 * 布尔值：1 或 0（开/关）
 */
class BooleanValue(
    override val name: String,
    default: Boolean = false,
    override val description: String = ""
) : Value<Boolean>(name, description) {

    private var value: Boolean = default

    override fun get(): Boolean = value
    override fun set(v: Boolean) {
        value = v
    }

    fun toggle(): Boolean {
        value = !value
        return value
    }
}

class NumberValue(
    override val name: String,
    default: Double,
    @JvmField val min: Double,
    @JvmField val max: Double,
    @JvmField val step: Double = 1.0,
    override val description: String = ""
) : Value<Double>(name, description) {

    private var value: Double = default.coerceIn(min, max)

    override fun get(): Double = value
    override fun set(v: Double) {
        value = v.coerceIn(min, max)
    }

    fun add() {
        value = (value + step).coerceIn(min, max)
    }
    fun sub() {
        value = (value - step).coerceIn(min, max)
    }
}

/**
 * 颜色值：用于设置颜色
 */
class ColorValue(
    override val name: String,
    default: Color,
    override val description: String = ""
) : Value<Color>(name, description) {

    private var value: Color = default

    override fun get(): Color = value
    override fun set(v: Color) {
        value = v
    }
}

/**
 * 字符串值：用于设置文本
 */
class StringValue(
    override val name: String,
    default: String = "",
    @JvmField val onlyNumber: Boolean = false,
    override val description: String = ""
) : Value<String>(name, description) {

    private var value: String = default

    override fun get(): String = value
    override fun set(v: String) {
        value = v
    }
    
    fun setText(text: String) {
        value = text
    }
    
    // Getter for Java compatibility
    fun isOnlyNumber(): Boolean {
        return onlyNumber
    }
}