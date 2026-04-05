package com.ink.recode.modules.impl.render

import com.ink.recode.Category
import com.ink.recode.Module
import org.lwjgl.glfw.GLFW

object Hud: Module("Hud", "HUD display module", Category.RENDER) {
    init {
        this.enabled=true;
        this.key=GLFW.GLFW_KEY_H;
    }

}