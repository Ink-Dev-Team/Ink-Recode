package com.ink.recode.modules.impl.movement

import com.ink.recode.Category
import com.ink.recode.Module
import com.ink.recode.event.Listener
import com.ink.recode.utils.*
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.ModeValue
import com.ink.recode.value.NumberValue
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

object Scaffold2 : Module("Scaffold2", "自动搭路2", Category.MOVEMENT) {

    private val blacklistedBlocks = listOf(
        Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.ENCHANTING_TABLE,
        Blocks.GLASS_PANE, Blocks.IRON_BARS, Blocks.SNOW, Blocks.COAL_ORE,
        Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.CHEST, Blocks.TORCH
    )

    private val mode = ModeValue("Mode", listOf("Normal", "Telly", "Snap"), 0)
    private val placeDelay = NumberValue("Place Delay", 0.0, 0.0, 5.0, 1.0)
    private val tellyTick = NumberValue("Telly Ticks", 1.0, 0.0, 6.0, 1.0)
    private val safeWalk = BooleanValue("Safe Walk", true)
    private val telly = BooleanValue("Telly", true)

    private var blockPos: BlockPos? = null
    private var enumFacing: Direction? = null
    private var airTick = 0
    private var yLevel = 0
    private var oldSlot = -1

    init {
        values.add(mode)
        values.add(placeDelay)
        values.add(tellyTick)
        values.add(safeWalk)
        values.add(telly)
    }

    override fun onEnable() {
        super.onEnable()
        mc.player?.let { oldSlot = it.inventory.selectedSlot }
    }

    override fun onDisable() {
        super.onDisable()
        mc.player?.inventory?.selectedSlot = oldSlot
    }

    @Listener
    fun onTick() {
        val player = mc.player ?: return
        val world = mc.world ?: return

        // 自动切换方块
        for (i in 0..8) {
            val stack = player.inventory.getStack(i)
            if (isValidBlock(stack)) {
                player.inventory.selectedSlot = i
                break
            }
        }

        if (player.isOnGround) {
            yLevel = (player.y.toInt() - 1)
        }

        updateBlockPos()

        if (telly.get()) {
            if (player.isOnGround) {
                airTick = 0
                blockPos = null
                enumFacing = null
            } else {
                if (airTick >= tellyTick.get().toInt()) {
                    placeBlock()
                }
                airTick++
            }
        } else {
            placeBlock()
        }
    }

    private fun updateBlockPos() {
        val player = mc.player ?: return
        val x = player.x.toInt()
        val y = yLevel
        val z = player.z.toInt()

        val pos = BlockPos(x, y, z)
        if (findSide(pos)) return

        for (d in 1..3) {
            val checkPos = BlockPos(x, y - d, z)
            if (findSide(checkPos)) return
        }
    }

    private fun findSide(pos: BlockPos): Boolean {
        val world = mc.world ?: return false
        if (world.getBlockState(pos).block != Blocks.AIR) return false

        for (dir in Direction.values()) {
            val offset = pos.offset(dir)
            if (world.getBlockState(offset).isSolid) {
                blockPos = pos
                enumFacing = dir.opposite
                return true
            }
        }
        return false
    }

    private fun placeBlock() {
        val pos = blockPos ?: return
        val face = enumFacing ?: return
        val player = mc.player ?: return

        val hit = BlockHitResult(
            Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5),
            face,
            pos,
            false
        )

        val result = mc.interactionManager?.interactBlock(player, Hand.MAIN_HAND, hit)
        if (result == ActionResult.SUCCESS) {
            player.swingHand(Hand.MAIN_HAND)
        }
    }

    private fun isValidBlock(stack: ItemStack): Boolean {
        if (stack.isEmpty || stack.item !is BlockItem) return false
        val block = (stack.item as BlockItem).block
        return !blacklistedBlocks.contains(block)
    }
}