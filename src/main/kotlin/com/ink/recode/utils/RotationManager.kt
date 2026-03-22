package com.ink.recode.utils

import com.ink.recode.event.Listener
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.event.events.TickEvent
import net.minecraft.client.MinecraftClient
import kotlin.math.abs

/**
 * 静默旋转管理器
 * 实现原理：只修改发送给服务器的旋转角度，保持客户端视觉角度不变
 */
object RotationManager {
    
    private val mc = MinecraftClient.getInstance()
    
    // 服务器旋转角度（发送给服务器的角度）
    private var serverRotation: Rotation? = null
    // 视觉旋转角度（玩家看到的角度）
    private var visualRotation: Rotation = Rotation.ZERO
    // 是否启用静默旋转
    private var isSilentRotating: Boolean = false
    // 旋转速度
    private var rotationSpeed: Float = 10f
    private var rotationSmoothness: Float = 5f
    
    /**
     * 设置目标旋转角度（静默旋转）
     * 只修改发送给服务器的角度，不改变玩家视觉角度
     */
    @JvmStatic
    fun setRotations(target: Rotation, speed: Float = 10f, smoothness: Float = 5f) {
        println("[RotationManager] Setting target rotation: $target, speed: $speed")
        
        val player = mc.player ?: return
        
        val clampedTarget = RotationUtils.clampRotation(target)
        
        if (!RotationUtils.validateRotation(clampedTarget)) {
            println("[RotationManager] Invalid rotation detected")
            return
        }
        
        // 保存玩家当前视觉角度
        visualRotation = Rotation(player.yaw, player.pitch)
        
        // 设置服务器目标角度
        serverRotation = clampedTarget
        rotationSpeed = speed
        rotationSmoothness = smoothness
        isSilentRotating = true
        
        println("[RotationManager] Silent rotation enabled")
        println("[RotationManager] Visual rotation: $visualRotation")
        println("[RotationManager] Server target: $serverRotation")
    }
    
    /**
     * 获取发送给服务器的旋转角度
     */
    @JvmStatic
    fun getServerRotation(): Rotation? {
        return serverRotation
    }
    
    /**
     * 获取视觉旋转角度
     */
    @JvmStatic
    fun getVisualRotation(): Rotation {
        return visualRotation
    }
    
    /**
     * 是否正在静默旋转
     */
    @JvmStatic
    fun isRotating(): Boolean {
        return isSilentRotating && serverRotation != null
    }
    
    /**
     * 重置旋转状态
     */
    @JvmStatic
    fun reset() {
        println("[RotationManager] Resetting rotation")
        serverRotation = null
        visualRotation = Rotation.ZERO
        isSilentRotating = false
    }
    
    /**
     * 更新旋转（每tick调用）
     * 平滑过渡到目标角度
     */
    private fun updateRotation() {
        if (!isSilentRotating || serverRotation == null) return
        
        val currentServerRotation = serverRotation ?: return
        
        // 平滑过渡到目标角度
        val smoothedRotation = RotationUtils.smooth(
            currentServerRotation,
            currentServerRotation,
            rotationSpeed
        )
        
        serverRotation = smoothedRotation
        
        // 检查是否接近目标
        if (RotationUtils.isClose(currentServerRotation, currentServerRotation, 0.5f)) {
            println("[RotationManager] Rotation completed")
            isSilentRotating = false
        }
    }
    
    /**
     * 应用移动修正
     * 当静默旋转时，修正移动方向以匹配旋转角度
     */
    private fun applyMovementCorrection() {
        if (!isSilentRotating) return
        
        val player = mc.player ?: return
        val serverRot = serverRotation ?: return
        
        // 计算视觉角度与服务器角度的差值
        val yawDiff = RotationUtils.calculateDelta(visualRotation.yaw, serverRot.yaw)
        
        // 如果差值过大，应用修正
        if (abs(yawDiff) > 45f) {
            // 这里可以添加移动方向修正逻辑
            println("[RotationManager] Movement correction: $yawDiff")
        }
    }
    
    @Listener
    fun onTick(event: TickEvent) {
        if (isSilentRotating) {
            updateRotation()
        }
    }
    
    @Listener
    fun onRender(event: RenderEvent) {
        if (isSilentRotating) {
            applyMovementCorrection()
        }
    }
    
    fun debugInfo(): String {
        return """
            RotationManager Debug:
            - Server Rotation: $serverRotation
            - Visual Rotation: $visualRotation
            - Silent Rotating: $isSilentRotating
            - Speed: $rotationSpeed
        """.trimIndent()
    }
}
