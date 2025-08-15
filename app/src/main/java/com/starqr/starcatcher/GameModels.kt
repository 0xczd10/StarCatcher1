package com.starqr.starcatcher

import kotlin.random.Random

// Star и Connection остаются без изменений
data class Star(val id: Int, val x: Float, val y: Float)

data class Connection(val starId1: Int, val starId2: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Connection) return false
        return (starId1 == other.starId1 && starId2 == other.starId2) ||
                (starId1 == other.starId2 && starId2 == other.starId1)
    }
    override fun hashCode(): Int = starId1.hashCode() + starId2.hashCode()
}

// НОВЫЙ ОБЪЕКТ: Чёрная дыра
data class BlackHole(
    val x: Float, // Координата центра X
    val y: Float, // Координата центра Y
    val radius: Float // Радиус (в относительных единицах, как у звезд)
)

// ОБНОВЛЕННЫЙ УРОВЕНЬ
data class ConstellationLevel(
    val levelNumber: Int,
    val stars: List<Star>,
    val requiredConnections: List<Connection>,
    val blackHoles: List<BlackHole> = emptyList(), // Список препятствий
    val allowLineCrossing: Boolean = true // Правило пересечения
)