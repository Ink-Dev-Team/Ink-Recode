package com.ink.recode

import java.awt.color.*;
import com.ink.recode.Category;
import com.ink.recode.event.events.RenderEvent
import net.minecraft.client.MinecraftClient


open class Module(var name: String, var description: String, var category: Category) {
    var enabled=false
    var key=-1
    val mc= MinecraftClient.getInstance()
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