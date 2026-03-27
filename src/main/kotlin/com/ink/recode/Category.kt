package com.ink.recode

/**
 * 模块分类
 */
enum class Category(val displayName: String, val icon: String) {
    COMBAT("Combat", "⚔"),
    MOVEMENT("Movement", "✈"),
    RENDER("Render", "👁"),
    WORLD("World", "🌍"),
    PLAYER("Player", "👤"),
    MISC("Misc", "🔧"),
    EXPLOIT("Exploit", "💥"),
    FUN("Fun", "🎮"),
    HUD("HUD", "📊"),
    SCRIPT("Script", "📜")
}