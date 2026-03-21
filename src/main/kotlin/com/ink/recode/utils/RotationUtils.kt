package com.ink.recode.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Rotation(
    val yaw: Float,
    val pitch: Float
) {
    companion object {
        val ZERO = Rotation(0f, 0f)
    }

    override fun toString(): String {
        return "Rotation(yaw=$yaw, pitch=$pitch)"
    }
}

object RotationUtils {
    
    private val mc = MinecraftClient.getInstance()
    
    fun smooth(current: Rotation, target: Rotation, speed: Float): Rotation {
        val yawDelta = calculateDelta(current.yaw, target.yaw)
        val pitchDelta = calculateDelta(current.pitch, target.pitch)
        
        val newYaw = current.yaw + yawDelta.coerceIn(-speed, speed)
        val newPitch = current.pitch + pitchDelta.coerceIn(-speed, speed)
        
        return Rotation(newYaw, newPitch)
    }
    
    fun validateRotation(rotation: Rotation): Boolean {
        val player = mc.player ?: return false
        
        val clampedRotation = clampRotation(rotation)
        
        val isPitchValid = clampedRotation.pitch >= -90f && clampedRotation.pitch <= 90f
        val isYawValid = clampedRotation.yaw >= -180f && clampedRotation.yaw <= 180f
        
        return isPitchValid && isYawValid
    }
    
    fun calculateDelta(current: Float, target: Float): Float {
        var delta = target - current
        
        while (delta < -180f) delta += 360f
        while (delta > 180f) delta -= 360f
        
        return delta
    }
    
    fun clampRotation(rotation: Rotation): Rotation {
        val clampedYaw = MathHelper.wrapDegrees(rotation.yaw)
        val clampedPitch = rotation.pitch.coerceIn(-90f, 90f)
        
        return Rotation(clampedYaw, clampedPitch)
    }
    
    fun getRotation(entity: Entity): Rotation {
        val player = mc.player ?: return Rotation.ZERO
        
        val playerEyeHeight = player.getEyeHeight(player.pose).toDouble()
        val entityEyeHeight = (entity as? net.minecraft.entity.LivingEntity)?.getEyeHeight(entity.pose)?.toDouble() ?: 1.0
        
        val eyesPos = Vec3d(player.x, player.y + playerEyeHeight, player.z)
        val targetPos = Vec3d(entity.x, entity.y + entityEyeHeight, entity.z)
        val diff = targetPos.subtract(eyesPos)
        
        val yaw = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0)
        val pitch = MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))))
        
        return Rotation(yaw.toFloat(), pitch.toFloat())
    }
    
    fun getDistance(rotation1: Rotation, rotation2: Rotation): Float {
        val yawDelta = abs(calculateDelta(rotation1.yaw, rotation2.yaw))
        val pitchDelta = abs(calculateDelta(rotation1.pitch, rotation2.pitch))
        
        return MathHelper.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta)
    }
    
    fun isClose(rotation1: Rotation, rotation2: Rotation, tolerance: Float = 1.0f): Boolean {
        return getDistance(rotation1, rotation2) <= tolerance
    }
}