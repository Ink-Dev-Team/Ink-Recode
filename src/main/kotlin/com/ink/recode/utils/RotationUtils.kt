package com.ink.recode.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.Direction
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 旋转角度数据类
 * 用于Java互操作，提供getter方法
 */
data class Rotation(
    @JvmField val yaw: Float,
    @JvmField val pitch: Float
) {
    companion object {
        @JvmField
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
        
        val playerEyeHeight = player.eyeHeight
        val entityEyeHeight = (entity as? net.minecraft.entity.LivingEntity)?.eyeHeight ?: 1.0
        
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
    
    fun calculate(pos: BlockPos, direction: Direction): Rotation {
        val player = mc.player ?: return Rotation.ZERO
        val eyesPos = Vec3d(player.x, player.y + player.eyeHeight, player.z)
        val blockCenter = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        
        // 计算朝向方块的向量
        val lookVec = blockCenter.subtract(eyesPos).normalize()
        
        // 计算 yaw 和 pitch
        val yaw = Math.toDegrees(Math.atan2(lookVec.z, lookVec.x)) - 90.0
        val pitch = -Math.toDegrees(Math.atan2(lookVec.y, Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z)))
        
        return Rotation(yaw.toFloat(), pitch.toFloat())
    }
    
    fun calculate(pos: Vec3d): Rotation {
        val player = mc.player ?: return Rotation.ZERO
        val eyesPos = Vec3d(player.x, player.y + player.eyeHeight, player.z)
        
        // 计算朝向目标点的向量
        val lookVec = pos.subtract(eyesPos).normalize()
        
        // 计算 yaw 和 pitch
        val yaw = Math.toDegrees(Math.atan2(lookVec.z, lookVec.x)) - 90.0
        val pitch = -Math.toDegrees(Math.atan2(lookVec.y, Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z)))
        
        return Rotation(yaw.toFloat(), pitch.toFloat())
    }
}