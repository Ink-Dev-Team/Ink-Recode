package com.ink.recode

import java.awt.color.*;
import com.ink.recode.Category
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.value.Value
import net.minecraft.client.MinecraftClient


open class Module(@JvmField var name: String, var description: String, var category: Category) {
    @JvmField var enabled=false
    @JvmField var key=-1
    val mc= MinecraftClient.getInstance()
    
    @JvmField val values = mutableListOf<Value<*>>()
    
    enum class BindMode {
        TOGGLE,
        HOLD
    }
    
    @JvmField var bindMode = BindMode.TOGGLE
    
    open fun onTick()
    {

    }
    open fun onRender(event: RenderEvent)
    {

    }
    open fun onEnable() {}
    open fun onDisable() {}
    fun enable()
    {
        this.enabled = true
        onEnable()
    }
    fun disable()
    {
        this.enabled=false
        onDisable()
    }
    fun toggle()
    {
        if(enabled)
        {
            disable()
        }
        else{
            enable()
        }
    }
}