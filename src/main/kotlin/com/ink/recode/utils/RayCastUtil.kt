package com.ink.recode.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.Vec3d

object RayCastUtil {
    private val mc = MinecraftClient.getInstance()
    
    fun overBlock(rotation: Rotation, blockPos: BlockPos): Boolean {
        val player = mc.player ?: return false
        val eyePos = player.eyePosition
        val lookVec = getLookVector(rotation.yaw, rotation.pitch)
        val endPos = eyePos.add(lookVec.scale(5.0))
        
        val rayCastContext = RayTraceContext(
            eyePos,
            endPos,
            RayTraceContext.ShapeType.OUTLINE,
            RayTraceContext.FluidMode.NONE,
            player
        )
        
        val hitResult = mc.world?.rayTraceBlocks(rayCastContext)
        return hitResult is BlockRayTraceResult && hitResult.blockPos == blockPos
    }
    
    private fun getLookVector(yaw: Float, pitch: Float): Vec3d {
        val f = Math.cos((-yaw * 0.017453292f - Math.PI.toFloat()).toDouble())
        val f1 = Math.sin((-yaw * 0.017453292f - Math.PI.toFloat()).toDouble())
        val f2 = -Math.cos((-pitch * 0.017453292f).toDouble())
        val f3 = Math.sin((-pitch * 0.017453292f).toDouble())
        return Vec3d(f1 * f2, f3, f * f2)
    }
}