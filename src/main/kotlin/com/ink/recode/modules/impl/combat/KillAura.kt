package com.ink.recode.modules.impl.combat

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.event.Listener
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.event.events.TickEvent
import com.ink.recode.render.FontManager
import com.ink.recode.render.Skia
import com.ink.recode.render.SkiaRenderer
import io.github.humbleui.skija.Font
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import java.awt.Color

enum class AttackMode {
    SINGLE, SWITCH, MULTI
}

enum class SortPriority {
    DISTANCE, HEALTH
}

object KillAura : Module("KillAura", "Automatic attack module", Category.COMBAT) {

    private val font: Font by lazy { FontManager.getBold(20f) }

    private var attackMode = AttackMode.SINGLE
    private var sortPriority = SortPriority.DISTANCE
    private val range: Float by lazy { 5.0f }
    private val attackSpeed: Int by lazy { 10 } // 攻击间隔（tick）
    private val fov: Float by lazy { 180f }

    private var target: LivingEntity? = null
    // Module 中的 mc 是 final，改为自定义 getter
    private val minecraft: MinecraftClient
        get() = MinecraftClient.getInstance()

    // 攻击冷却计时器
    private var attackTimer = 0

    init {
        this.enabled = true
        this.key = GLFW.GLFW_KEY_R
    }

    @Listener
    fun onTick(event: TickEvent) {
        if (!enabled) return
        if (minecraft.world == null || minecraft.player == null) return

        val player = minecraft.player ?: return

        // 攻击冷却计时
        if (attackTimer > 0) {
            attackTimer--
        }

        // 玩家状态检查
        if (player.handSwinging || player.isUsingItem || player.isBlocking) return

        val enemies = findEnemies()
        if (enemies.isEmpty()) {
            target = null
            return
        }

        target = selectTarget(enemies)

        // 检查攻击条件
        if (target != null && canAttack(target!!) && attackTimer <= 0) {
            attackTarget(target!!)
            attackTimer = attackSpeed // 重置冷却
        }
    }

    @Listener
    override fun onRender(event: RenderEvent) {
        if (!enabled) return
        if (!SkiaRenderer.isInitialized() || target == null) return

        try {
            Skia.draw { canvas ->
                val targetEntity = target!!
                val screenPos = minecraft.gameRenderer.camera.pos

                val x = 10f
                val y = 10f

                // 绘制目标信息面板
                Skia.drawRoundedRect(x, y, 150f, 60f, 5f, Color(0, 0, 0, 150))
                Skia.drawText(targetEntity.name?.string ?: "Unknown", x + 10f, y + 20f, Color(255, 255, 255), font)
                Skia.drawText("HP: ${targetEntity.health.toInt()}", x + 10f, y + 40f, Color(255, 0, 0), font)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun findEnemies(): List<LivingEntity> {
        val world = minecraft.world ?: return emptyList()
        val player = minecraft.player ?: return emptyList()

        // 获取玩家视角向量
        val playerRotationVec = getRotationVector(player.yaw, player.pitch)

        return world.entities
            .filterIsInstance<LivingEntity>()
            .filter { entity ->
                entity != player &&
                        entity.isAlive &&
                        player.canSee(entity) &&
                        entity.distanceTo(player) <= range &&
                        // 修复：MathHelper.cos 参数类型 - 转为 Float
                        calculateFOV(playerRotationVec, player, entity) >= MathHelper.cos(Math.toRadians(fov / 2.0).toFloat())
            }
            .sortedBy { entity ->
                when (sortPriority) {
                    SortPriority.DISTANCE -> entity.distanceTo(player)
                    SortPriority.HEALTH -> entity.health
                }
            }
            .toList()
    }

    // 计算玩家视角向量
    private fun getRotationVector(yaw: Float, pitch: Float): Vec3d {
        val f = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat()).toDouble()
        val f1 = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat()).toDouble()
        val f2 = -MathHelper.cos(-pitch * 0.017453292f).toDouble()
        val f3 = MathHelper.sin(-pitch * 0.017453292f).toDouble()
        return Vec3d(f1 * f2, f3, f * f2)
    }

    // 计算目标与玩家视角的 FOV 夹角
    private fun calculateFOV(playerVec: Vec3d, player: PlayerEntity, entity: LivingEntity): Float {
        val entityPos = Vec3d(
            entity.x - player.x,
            entity.y + entity.getEyeHeight(entity.pose) - (player.y + player.getEyeHeight(player.pose)),
            entity.z - player.z
        )
        val distance = entityPos.length()
        if (distance == 0.0) return 1.0f

        val entityVec = entityPos.normalize()
        return playerVec.dotProduct(entityVec).toFloat()
    }

    private fun selectTarget(enemies: List<LivingEntity>): LivingEntity? {
        return enemies.firstOrNull()
    }

    private fun canAttack(entity: LivingEntity): Boolean {
        val player = minecraft.player ?: return false
        return player.canSee(entity) && player.distanceTo(entity) <= range && entity.isAlive
    }

    private fun attackTarget(entity: LivingEntity) {
        val player = minecraft.player ?: return

        // 瞄准目标
        lookAtEntity(entity)

        // 执行攻击
        minecraft.interactionManager?.attackEntity(player, entity)
        player.swingHand(Hand.MAIN_HAND)
    }

    // 精准瞄准实体
    private fun lookAtEntity(entity: Entity) {
        val player = minecraft.player ?: return

        // 获取眼高并统一为 Double 类型
        val playerEyeHeight = player.getEyeHeight(player.pose).toDouble()
        val entityEyeHeight = (entity as? LivingEntity)?.getEyeHeight(entity.pose)?.toDouble() ?: 1.0

        val eyesPos = Vec3d(player.x, player.y + playerEyeHeight, player.z)
        val targetPos = Vec3d(entity.x, entity.y + entityEyeHeight, entity.z)
        val diff = targetPos.subtract(eyesPos)

        // 计算旋转角度
        val yaw = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0)
        val pitch = MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))))

        // 设置玩家视角
        player.yaw = yaw.toFloat()
        player.pitch = pitch.toFloat()
    }
}