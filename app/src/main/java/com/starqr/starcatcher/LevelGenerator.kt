package com.starqr.starcatcher

import kotlin.math.pow
import kotlin.random.Random

object LevelGenerator {

    // --- НОВЫЕ КОНСТАНТЫ: Определяем "безопасную зону" ---
    // Отступ по горизонтали (10% с каждой стороны)
    private const val PADDING_X = 0.1f
    // Отступ по вертикали (15% сверху и 15% снизу, чтобы избежать UI)
    private const val PADDING_Y = 0.15f

    fun generateLevel(levelNumber: Int): ConstellationLevel {
        val starCount = 3 + (levelNumber / 3)
        val decoyCount = 1 + (levelNumber / 5)
        val blackHoleCount = if (levelNumber >= 5) 1 + (levelNumber / 7) else 0
        val allowCrossing = levelNumber < 10

        val allStars = mutableListOf<Star>()
        val realStars = mutableListOf<Star>()
        val connections = mutableListOf<Connection>()
        val blackHoles = mutableListOf<BlackHole>()

        // Генерируем "настоящие" звезды в безопасной зоне
        for (i in 1..starCount) {
            var newStar: Star
            do {
                // ИСПОЛЬЗУЕМ НОВУЮ ФУНКЦИЮ ДЛЯ ГЕНЕРАЦИИ КООРДИНАТ
                newStar = Star(i, getRandomX(), getRandomY())
            } while (allStars.any { distance(it, newStar) < 0.1f }) // Проверка на слишком близкое соседство
            allStars.add(newStar)
            realStars.add(newStar)
        }

        // Генерируем "ложные" звезды в безопасной зоне
        for (i in 1..decoyCount) {
            var newDecoy: Star
            do {
                newDecoy = Star(starCount + i, getRandomX(), getRandomY())
            } while (allStars.any { distance(it, newDecoy) < 0.1f })
            allStars.add(newDecoy)
        }

        // Генерируем черные дыры в безопасной зоне
        for (i in 1..blackHoleCount) {
            val radius = Random.nextDouble(0.05, 0.1).toFloat()
            blackHoles.add(BlackHole(getRandomX(), getRandomY(), radius))
        }

        // Логика создания соединений остается прежней
        val unlinkedStars = realStars.toMutableList()
        var currentStar = unlinkedStars.removeAt(0)
        while (unlinkedStars.isNotEmpty()) {
            val nearestStar = unlinkedStars.minByOrNull { distance(currentStar, it) }!!
            connections.add(Connection(currentStar.id, nearestStar.id))
            unlinkedStars.remove(nearestStar)
            currentStar = nearestStar
        }

        return ConstellationLevel(
            levelNumber = levelNumber,
            stars = allStars.shuffled(),
            requiredConnections = connections,
            blackHoles = blackHoles,
            allowLineCrossing = allowCrossing
        )
    }

    // --- НОВЫЕ ФУНКЦИИ для генерации координат в безопасной зоне ---
    private fun getRandomX(): Float {
        // Генерирует X между 0.1 и 0.9
        return PADDING_X + Random.nextFloat() * (1.0f - PADDING_X * 2)
    }

    private fun getRandomY(): Float {
        // Генерирует Y между 0.15 и 0.85
        return PADDING_Y + Random.nextFloat() * (1.0f - PADDING_Y * 2)
    }

    private fun distance(s1: Star, s2: Star): Float {
        return kotlin.math.sqrt((s1.x - s2.x).pow(2) + (s1.y - s2.y).pow(2))
    }
}