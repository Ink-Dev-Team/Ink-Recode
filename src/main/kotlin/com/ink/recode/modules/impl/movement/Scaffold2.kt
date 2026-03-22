package com.ink.recode.modules.impl.movement

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.ModeValue
import com.ink.recode.value.NumberValue
import com.ink.recode.utils.RotationManager
import com.ink.recode.utils.Rotation
import com.ink.recode.utils.RotationUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW

object Scaffold2 : Module("Scaffold", "自动搭路模块", Category.MOVEMENT) {

    private val mode = ModeValue("Mode", listOf("Normal", "Telly"), 0)
    
    private val tellyEnabled = BooleanValue("Telly", false)
    private val tellyTick = NumberValue("Telly Ticks", 5f, 0f, 20f, 1f)
    private val rotationSpeed = NumberValue("Rotation Speed", 10f, 1f, 180f, 1f)
    private val rotateBackSpeed = NumberValue("Rotate Back Speed", 10f, 1f, 180f, 1f)
    private val watchdogTelly = BooleanValue("Watchdog Telly", false)
    private val watchdogTellyRotationSpeed = NumberValue("Watchdog Telly Rotation Speed", 5f, 1f, 180f, 1f)
    private val autoJump = BooleanValue("Auto Jump", true)
    private val tower = BooleanValue("Tower", false)
    
    private var airTick = 0
    private var blockPos: BlockPos? = null
    private var enumFacing: Direction? = null
    private var lastRotation: Rotation? = null

    init {
        this.key = GLFW.GLFW_KEY_C
    }

    override fun onTick() {
        val player = mc.player ?: return
        
        when (mode.current) {
            "Telly" -> handleTellyMode(player)
            "Normal" -> handleNormalMode(player)
        }
    }

    private fun handleTellyMode(player: PlayerEntity) {
        if (tellyEnabled.get()) {
            if (player.isOnGround) {
                airTick = 0
                blockPos = null
                enumFacing = null
                val rotation = Rotation(player.yaw, player.pitch)
                RotationManager.setRotations(rotation, rotateBackSpeed.get())
            } else {
                if (airTick >= tellyTick.get().toInt()) {
                    val targetPos = findBestBlockPos(player)
                    if (targetPos != null) {
                        val (pos, facing) = targetPos
                        val rotation = getRotation(pos, facing)
                        val speed = if (watchdogTelly.get()) watchdogTellyRotationSpeed.get() else rotationSpeed.get()
                        RotationManager.setRotations(rotation, speed)
                        placeBlock(pos, facing)
                    }
                }
                airTick++
            }
        }
    }

    private fun handleNormalMode(player: PlayerEntity) {
        val targetPos = findBestBlockPos(player)
        if (targetPos != null) {
            val (pos, facing) = targetPos
            val rotation = getRotation(pos, facing)
            RotationManager.setRotations(rotation, rotationSpeed.get())
            placeBlock(pos, facing)
        }
    }

    private fun findBestBlockPos(player: PlayerEntity): Pair<BlockPos, Direction>? {
        val world = mc.world ?: return null
        
        val playerPos = player.blockPos
        val directions = listOf(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
        
        for (direction in directions) {
            val targetPos = playerPos.offset(direction)
            if (isValidBlockPos(targetPos)) {
                val adjacentPos = targetPos.offset(direction.opposite)
                val adjacentState = world.getBlockState(adjacentPos)
                if (!adjacentState.isAir && !adjacentState.isLiquid) {
                    return Pair(targetPos, direction.opposite)
                }
            }
        }
        
        return null
    }

    private fun isValidBlockPos(pos: BlockPos): Boolean {
        val world = mc.world ?: return false
        val state = world.getBlockState(pos)
        return state.isAir || state.isReplaceable
    }

    private fun getRotation(pos: BlockPos, facing: Direction): Rotation {
        val player = mc.player ?: return Rotation.ZERO
        
        val x = pos.x + 0.5 + facing.offsetX * 0.5
        val y = pos.y + 0.5 + facing.offsetY * 0.5
        val z = pos.z + 0.5 + facing.offsetZ * 0.5
        
        val playerEyePos = player.eyePos
        val delta = Vec3d(x, y, z).subtract(playerEyePos)
        
        val yaw = Math.toDegrees(Math.atan2(delta.z, delta.x)).toFloat() - 90f
        val pitch = -Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))).toFloat()
        
        return Rotation(yaw, pitch)
    }

    private fun placeBlock(pos: BlockPos, facing: Direction): Boolean {
        val player = mc.player ?: return false
        val world = mc.world ?: return false
        
        val stack = findBestBlock()
        if (stack == null || stack.item !is BlockItem) {
            return false
        }
        
        val hitResult = BlockHitResult(
            Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5),
            facing,
            pos.offset(facing),
            false
        )
        
        val success = mc.interactionManager?.interactBlock(player, player.activeHand, hitResult) == ActionResult.SUCCESS
        
        if (success) {
            player.swingHand(player.activeHand)
        }
        
        return success
    }

    private fun findBestBlock(): ItemStack? {
        val player = mc.player ?: return null
        
        var bestStack: ItemStack? = null
        var bestSlot = -1
        
        for (i in 0 until player.inventory.main.size) {
            val stack = player.inventory.main[i]
            if (stack.item is BlockItem) {
                if (bestStack == null || stack.count > bestStack.count) {
                    bestStack = stack
                    bestSlot = i
                }
            }
        }
        
        if (bestSlot >= 0) {
            player.inventory.selectedSlot = bestSlot
        }
        
        return bestStack
    }

    @JvmStatic
    fun handleAutoJump(player: PlayerEntity) {
        if (autoJump.get() && player.isOnGround && hasMovementInput(player)) {
            player.jump()
        }
        
        if (tower.get() && mc.options.jumpKey.isPressed) {
            player.addVelocity(0.0, 0.42, 0.0)
        }
    }

    private fun hasMovementInput(player: PlayerEntity): Boolean {
        val input = mc.options
        return input.forwardKey.isPressed || input.backKey.isPressed || 
               input.leftKey.isPressed || input.rightKey.isPressed
    }
}

data class MovementInput(
    val forward: Boolean,
    val backward: Boolean,
    val left: Boolean,
    val right: Boolean,
    val jump: Boolean,
    val sneak: Boolean
) {
    fun hasMovement(): Boolean {
        return forward || backward || left || right
    }
}
