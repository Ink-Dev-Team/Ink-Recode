package com.ink.recode.value

/**
 * 配置值系统：ModeValue（多选一）、BooleanValue（开关）、NumberValue（数值）
 */

/** 所有配置值的基类 */
abstract class Value<T>(open val name: String, open val description: String = "") {
    abstract fun get(): T
    abstract fun set(v: T)
}

/**
 * 多选一：从若干选项中选一个，用 index 表示当前项
 */
class ModeValue(
    name: String,
    val options: List<String>,
    defaultIndex: Int = 0,
    description: String = ""
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
}

/**
 * 布尔值：1 或 0（开/关）
 */
class BooleanValue(
    name: String,
    default: Boolean = false,
    description: String = ""
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
    name: String,
    default: Float,
    val min: Float,
    val max: Float,
    val step: Float = 1f,
    description: String = ""
) : Value<Float>(name, description) {

    private var value: Float = default.coerceIn(min, max)

    override fun get(): Float = value
    override fun set(v: Float) {
        value = v.coerceIn(min, max)
    }

    fun add() {
        value = (value + step).coerceIn(min, max)
    }
    fun sub() {
        value = (value - step).coerceIn(min, max)
    }
}
