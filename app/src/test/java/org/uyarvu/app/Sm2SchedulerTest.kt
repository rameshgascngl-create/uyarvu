package org.uyarvu.app

import org.junit.Assert.assertEquals
import org.junit.Test
import org.uyarvu.app.domain.learning.RecallQuality
import org.uyarvu.app.domain.learning.Sm2Scheduler
import org.uyarvu.app.domain.learning.Sm2State

class Sm2SchedulerTest {
    @Test
    fun forgottenCard_resetsRepetitionsToZero() {
        val next = Sm2Scheduler.nextReview(Sm2State(2.5, 12, 3), RecallQuality.AGAIN)
        assertEquals(0, next.repetitions)
        assertEquals(1, next.intervalDays)
    }

    @Test
    fun goodReviews_progressOneThenSixDays() {
        val first = Sm2Scheduler.nextReview(Sm2State(2.5, 0, 0), RecallQuality.GOOD)
        assertEquals(1, first.intervalDays)
        val second = Sm2Scheduler.nextReview(first, RecallQuality.GOOD)
        assertEquals(6, second.intervalDays)
    }
}
