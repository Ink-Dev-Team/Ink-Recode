package com.ink.recode

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import com.ink.recode.event.*
import com.ink.recode.modules.impl.movement.Sprint
import com.ink.recode.modules.impl.render.WaterMark
import com.ink.recode.modules.impl.render.ClickGUI
import com.ink.recode.modules.impl.render.Thud
import com.ink.recode.modules.impl.combat.KillAura
import com.ink.recode.modules.impl.movement.Scaffold2
import com.ink.recode.utils.RotationManager
import com.ink.recode.webgui.HttpServer

object InkRecode : ModInitializer {
    private val logger = LoggerFactory.getLogger("ink-recode")

	override fun onInitialize() {
		EventManager.init()
		ModuleManager.register(Sprint)
		ModuleManager.register(WaterMark)
		ModuleManager.register(ClickGUI)
		ModuleManager.register(Thud)
		ModuleManager.register(KillAura)
		ModuleManager.register(Scaffold2)
		
		// 注册RotationManager到事件系统
		EventBus.register(RotationManager)
		
		// 启动WebGUI HTTP服务器
		HttpServer.start()
		
		logger.info("InkRecode initialized successfully")
	}
}