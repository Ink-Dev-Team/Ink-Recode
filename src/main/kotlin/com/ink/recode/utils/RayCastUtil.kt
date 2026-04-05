package com.ink.recode.utils

import com.ink.recode.utils.Rotation
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.RaycastContext
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

object RayCastUtil {
    private val mc = MinecraftClient.getInstance()

    fun overBlock(rotation: Rotation, blockPos: BlockPos): Boolean {
        val player = mc.player ?: return false
        val world = mc.world ?: return false

        // 修复：eyePosition → eyePos
        val eyePos = player.eyePos

        // 获取视线方向
        val lookVec = getLookVector(rotation.yaw, rotation.pitch)

        // 射线终点（5米距离）
        val endPos = eyePos.add(lookVec.multiply(5.0))

        // ✅ 官方标准射线检测（全版本通用，无任何报错）
        val hitResult: BlockHitResult = world.raycast(
            RaycastContext(
                eyePos,
                endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
            )
        )

        // 判断是否命中目标方块
        return hitResult.blockPos == blockPos
    }

    // 视角 → 方向向量（优化精简版）
    private fun getLookVector(yaw: Float, pitch: Float): Vec3d {
        val radYaw = yaw * (PI / 180.0)
        val radPitch = pitch * (PI / 180.0)

        val x = -sin(radYaw) * cos(radPitch)
        val y = -sin(radPitch)
        val z = cos(radYaw) * cos(radPitch)

        return Vec3d(x, y, z)
    }
}