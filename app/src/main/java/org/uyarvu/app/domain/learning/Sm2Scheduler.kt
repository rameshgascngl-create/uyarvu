package org.uyarvu.app.domain.learning

import kotlin.math.roundToInt

enum class RecallQuality(val score: Int) {
    AGAIN(1),
    HARD(3),
    GOOD(4),
    EASY(5)
}

data class Sm2State(
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitions: Int
)

object Sm2Scheduler {
    fun nextReview(current: Sm2State, quality: RecallQuality): Sm2State {
        val q = quality.score
        val newEase = (current.easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
            .coerceAtLeast(1.3)

        return if (q < 3) {
            Sm2State(easeFactor = newEase, intervalDays = 1, repetitions = 0)
        } else {
            val nextReps = current.repetitions + 1
            val nextInterval = when (nextReps) {
                1 -> 1
                2 -> 6
                else -> (current.intervalDays * newEase).roundToInt().coerceAtLeast(1)
            }
            Sm2State(easeFactor = newEase, intervalDays = nextInterval, repetitions = nextReps)
        }
    }
}
