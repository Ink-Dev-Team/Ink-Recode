package com.ink.recode.event

import com.ink.recode.event.events.TickEvent
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.modules.*
import com.ink.recode.Module
import com.ink.recode.ModuleManager
import com.ink.recode.event.Listener
import com.ink.recode.event.events.KeyboardEvent

object EventManager {
    fun init()
    {
        EventBus.register(EventManager)
    }
    @Listener
    fun onTick(event: TickEvent)
    {
        ModuleManager.modules
            .filter{it.enabled}
            .forEach{it.onTick()}
    }
    @Listener
    fun onKey(event: KeyboardEvent)
    {
        System.out.println("[DEBUG] key=" + event.key + " modules=" + ModuleManager.modules.size)
        ModuleManager.modules.forEach { module ->
            if (module.key == event.key) {
                System.out.println("[DEBUG] toggle " + module.name)
                module.toggle()
            }
        }
    }
    @Listener
    fun onRender(event: RenderEvent)
    {
        ModuleManager.modules
            .filter{it.enabled}
            .forEach{it.onRender(event)}
    }
}