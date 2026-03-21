package com.ink.recode.utils

import com.ink.recode.event.Listener
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.event.events.TickEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.abs

object RotationManager {
    
    private val mc = MinecraftClient.getInstance()
    
    private var targetRotation: Rotation? = null
    private var currentRotation: Rotation = Rotation.ZERO
    private var lastRotation: Rotation = Rotation.ZERO
    private var rotationSpeed: Float = 10f
    private var rotationSmoothness: Float = 5f
    private var isRotating: Boolean = false
    private var rotationTicks: Int = 0
    
    fun setRotations(target: Rotation, speed: Float = 10f, smoothness: Float = 5f) {
        println("[RotationManager] Setting target rotation: $target, speed: $speed, smoothness: $smoothness")
        
        val player = mc.player ?: return
        
        val clampedTarget = RotationUtils.clampRotation(target)
        
        if (!RotationUtils.validateRotation(clampedTarget)) {
            println("[RotationManager] Invalid rotation detected, clamping")
            targetRotation = RotationUtils.clampRotation(Rotation(player.yaw, player.pitch))
        } else {
            targetRotation = clampedTarget
        }
        
        // 从玩家当前视角开始旋转
        currentRotation = Rotation(player.yaw, player.pitch)
        lastRotation = currentRotation
        
        rotationSpeed = speed
        rotationSmoothness = smoothness
        isRotating = true
        rotationTicks = 0
        
        println("[RotationManager] Target rotation set: $targetRotation")
        println("[RotationManager] Current rotation: $currentRotation")
        println("[RotationManager] Distance to target: ${RotationUtils.getDistance(currentRotation, targetRotation!!)}")
    }
    
    fun getRotation(): Rotation {
        return currentRotation
    }
    
    fun reset() {
        println("[RotationManager] Resetting rotation")
        targetRotation = null
        currentRotation = Rotation.ZERO
        lastRotation = Rotation.ZERO
        isRotating = false
        rotationTicks = 0
    }
    
    fun isRotating(): Boolean {
        return isRotating && targetRotation != null
    }
    
    fun updateRotation() {
        if (!isRotating || targetRotation == null) return
        
        val player = mc.player ?: return
        
        rotationTicks++
        
        val startTime = System.nanoTime()
        
        val smoothedRotation = RotationUtils.smooth(
            currentRotation,
            targetRotation!!,
            rotationSpeed
        )
        
        val finalRotation = RotationUtils.smooth(
            smoothedRotation,
            targetRotation!!,
            rotationSmoothness
        )
        
        lastRotation = currentRotation
        currentRotation = finalRotation
        
        player.yaw = finalRotation.yaw
        player.pitch = finalRotation.pitch
        
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1000000.0 // 转换为毫秒
        
        if (duration > 1.0) {
            println("[RotationManager] Performance warning: Rotation update took ${duration}ms")
        }
        
        if (RotationUtils.isClose(currentRotation, targetRotation!!, 0.5f)) {
            println("[RotationManager] Rotation completed in $rotationTicks ticks")
            isRotating = false
            targetRotation = null
            rotationTicks = 0
        }
    }
    
    fun applyMovementCorrection() {
        if (!isRotating) return
        
        val player = mc.player ?: return
        
        val input = mc.options
        val forward = input.forwardKey.isPressed
        val backward = input.backKey.isPressed
        val left = input.leftKey.isPressed
        val right = input.rightKey.isPressed
        
        if (forward || backward || left || right) {
            var moveX = 0.0
            var moveZ = 0.0
            
            if (forward) moveZ -= 1.0
            if (backward) moveZ += 1.0
            if (left) moveX -= 1.0
            if (right) moveX += 1.0
            
            val moveVector = Vec3d(moveX, 0.0, moveZ)
            if (moveVector.lengthSquared() > 0.001) {
                val normalizedMove = moveVector.normalize()
                val movementAngle = Math.toDegrees(Math.atan2(-normalizedMove.x, normalizedMove.z)).toFloat()
                val yawDifference = MathHelper.wrapDegrees(movementAngle - player.yaw)
                
                if (abs(yawDifference) > 45f) {
                    val correctedYaw = player.yaw + yawDifference * 0.1f
                    player.yaw = correctedYaw
                    println("[RotationManager] Applied movement correction: $correctedYaw")
                }
            }
        }
    }
    
    @Listener
    fun onTick(event: TickEvent) {
        if (isRotating) {
            updateRotation()
        }
    }
    
    @Listener
    fun onRender(event: RenderEvent) {
        if (isRotating) {
            applyMovementCorrection()
        }
    }
    
    fun debugInfo(): String {
        return """
            RotationManager Debug:
            - Target: $targetRotation
            - Current: $currentRotation
            - Last: $lastRotation
            - Speed: $rotationSpeed
            - Smoothness: $rotationSmoothness
            - Rotating: $isRotating
            - Ticks: $rotationTicks
        """.trimIndent()
    }
}