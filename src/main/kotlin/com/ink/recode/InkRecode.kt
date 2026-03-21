package com.ink.recode

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import com.ink.recode.event.*
import com.ink.recode.modules.impl.movement.Sprint
import com.ink.recode.modules.impl.render.WaterMark
import com.ink.recode.modules.impl.render.ClickGUI
import com.ink.recode.modules.impl.combat.KillAura

object InkRecode : ModInitializer {
    private val logger = LoggerFactory.getLogger("ink-recode")

	override fun onInitialize() {
		EventManager.init()
		ModuleManager.register(Sprint)
		ModuleManager.register(WaterMark)
		ModuleManager.register(ClickGUI)
		ModuleManager.register(KillAura)
		
		logger.info("InkRecode initialized successfully")
	}
}