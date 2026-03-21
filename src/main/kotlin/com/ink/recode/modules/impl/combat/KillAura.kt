package com.ink.recode.modules.impl.combat

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.event.Listener
import com.ink.recode.event.events.RenderEvent
import com.ink.recode.event.events.TickEvent
import com.ink.recode.render.FontManager
import com.ink.recode.render.Skia
import com.ink.recode.render.SkiaRenderer
import com.ink.recode.utils.RotationManager
import com.ink.recode.utils.RotationUtils
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.NumberValue
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
    
    // SilentRotation 配置
    private val silentRotationEnabled = BooleanValue("SilentRotation", true, "启用静音旋转")
    private val rotationSpeed = NumberValue("RotationSpeed", 10f, 1f, 30f, 1f, "旋转速度")
    private val rotationSmoothness = NumberValue("RotationSmoothness", 5f, 1f, 20f, 1f, "旋转平滑度")
    private val silentRotationMode = BooleanValue("SilentRotationMode", true, "静音旋转模式")
    
    // 1.9 Mode 配置
    private val mode19Enabled = BooleanValue("1.9Mode", false, "启用1.9模式")
    private val cpsMin = NumberValue("CPSMin", 8f, 1f, 20f, 0.5f, "最小每秒攻击次数")
    private val cpsMax = NumberValue("CPSMax", 12f, 1f, 20f, 0.5f, "最大每秒攻击次数")
    
    init {
        this.enabled = true
        this.key = GLFW.GLFW_KEY_R
        
        // 注册value到模块
        values.add(silentRotationEnabled)
        values.add(rotationSpeed)
        values.add(rotationSmoothness)
        values.add(silentRotationMode)
        values.add(mode19Enabled)
        values.add(cpsMin)
        values.add(cpsMax)
    }

    private var target: LivingEntity? = null
    
    // Module 中的 mc 是 final，改为自定义 getter
    private val minecraft: MinecraftClient
        get() = MinecraftClient.getInstance()

    // 攻击冷却计时器
    private var attackTimer = 0
    
    // CPS 随机延迟系统
    private var nextAttackTime = 0L
    private var lastAttackTime = 0L

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

        // SilentRotation 集成
        if (silentRotationEnabled.get() && target != null) {
            val targetRotation = RotationUtils.getRotation(target!!)
            RotationManager.setRotations(targetRotation, rotationSpeed.get(), rotationSmoothness.get())
        } else if (!silentRotationEnabled.get()) {
            RotationManager.reset()
        }

        // 检查攻击条件
        if (target != null && canAttack(target!!)) {
            if (mode19Enabled.get()) {
                // 1.9 Mode: 使用CPS系统
                val currentTime = System.currentTimeMillis()
                if (currentTime >= nextAttackTime) {
                    attackTarget(target!!)
                    lastAttackTime = currentTime
                    // 计算下一次攻击时间（随机延迟）
                    val minDelay = (1000.0 / cpsMax.get()).toLong()
                    val maxDelay = (1000.0 / cpsMin.get()).toLong()
                    nextAttackTime = currentTime + minDelay + (Math.random() * (maxDelay - minDelay)).toLong()
                }
            } else {
                // 传统模式: 使用攻击冷却
                if (attackTimer <= 0) {
                    attackTarget(target!!)
                    attackTimer = attackSpeed
                }
            }
        }
    }

    @Listener
    override fun onRender(event: RenderEvent) {
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

        // 如果启用静音旋转，不直接调用lookAtEntity
        // 旋转由RotationManager在tick事件中处理
        if (!silentRotationMode.get()) {
            // 直接瞄准目标
            lookAtEntity(entity)
        }

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
    
    override fun onDisable() {
        println("[KillAura] onDisable called, resetting rotation")
        RotationManager.reset()
    }
}