// 路径：src/main/kotlin/com/ink/recode/ModuleManager.kt
package com.ink.recode

import com.ink.recode.event.EventBus
import java.util.concurrent.CopyOnWriteArrayList

object ModuleManager {
    val modules: CopyOnWriteArrayList<Module> = CopyOnWriteArrayList()

    fun register(module: Module) {
        modules.add(module)
        EventBus.register(module)
    }

    // 可选：根据名称查找模块
    fun getModule(name: String): Module? {
        return modules.find { it.name.equals(name, ignoreCase = true) }
    }

}