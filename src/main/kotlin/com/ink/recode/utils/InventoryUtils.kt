package com.ink.recode.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.BlockItem

object InventoryUtils {
    private val mc = MinecraftClient.getInstance()
    
    fun isItemValid(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        
        // 排除某些特殊物品
        val item = stack.item
        return item != Items.AIR
    }
    
    fun findBlockInHotbar(): Int {
        val player = mc.player ?: return -1
        
        for (i in 0 until 9) {
            val stack = player.inventory.items[i]
            if (!stack.isEmpty && stack.item is BlockItem) {
                return i
            }
        }
        return -1
    }
    
    fun switchToSlot(slot: Int) {
        val player = mc.player ?: return
        player.inventory.selected = slot
    }
}