package com.ink.recode.utils

import kotlin.random.Random

object MathUtils {
    private val random = Random
    
    fun getRandomDoubleInRange(min: Double, max: Double): Double {
        return min + random.nextDouble() * (max - min)
    }
    
    fun getRandomFloatInRange(min: Float, max: Float): Float {
        return min + random.nextFloat() * (max - min)
    }
    
    fun getRandomIntInRange(min: Int, max: Int): Int {
        return random.nextInt(min, max + 1)
    }
    
    fun clamp(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }
    
    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceIn(min, max)
    }
    
    fun clamp(value: Int, min: Int, max: Int): Int {
        return value.coerceIn(min, max)
    }
    
    fun lerp(start: Double, end: Double, progress: Double): Double {
        return start + (end - start) * progress
    }
    
    fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }
}