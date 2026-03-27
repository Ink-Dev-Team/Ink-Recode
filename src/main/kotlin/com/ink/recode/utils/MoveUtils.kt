package com.ink.recode.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.MathHelper

object MoveUtils {
    private val mc = MinecraftClient.getInstance()
    
    fun isMoving(): Boolean {
        val player = mc.player ?: return false
        return player.forwardSpeed != 0.0f || player.sidewaysSpeed != 0.0f
    }
    
    fun getSpeed(): Double {
        val player = mc.player ?: return 0.0
        return Math.sqrt(player.velocity.x * player.velocity.x + player.velocity.z * player.velocity.z)
    }
    
    fun setSpeed(speed: Double) {
        val player = mc.player ?: return
        val yaw = player.yaw
        val forward = player.forwardSpeed
        val strafe = player.sidewaysSpeed
        
        val f = MathHelper.sin(yaw * 0.017453292f)
        val f1 = MathHelper.cos(yaw * 0.017453292f)
        
        player.velocity = player.velocity.add(
            (forward * speed * f1 - strafe * speed * f).toDouble() - player.velocity.x,
            0.0,
            (strafe * speed * f1 + forward * speed * f).toDouble() - player.velocity.z
        )
    }
    
    fun strafe(speed: Double = getSpeed()) {
        setSpeed(speed)
    }
    
    fun isOnGround(): Boolean {
        val player = mc.player ?: return false
        return player.isOnGround
    }
}