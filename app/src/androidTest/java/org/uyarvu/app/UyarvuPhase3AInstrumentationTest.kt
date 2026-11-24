package org.uyarvu.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UyarvuPhase3AInstrumentationTest {
    @Test
    fun appContext_matchesPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.uyarvu.app", appContext.packageName)
    }
}
