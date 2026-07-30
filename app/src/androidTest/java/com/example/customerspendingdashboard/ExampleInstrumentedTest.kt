package com.example.customerspendingdashboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Default instrumented test scaffold verifying the app context resolves correctly. */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    // The instrumented app context resolves to the expected application id.
    @Test
    fun appContextHasTheExpectedPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.customerspendingdashboard", appContext.packageName)
    }
}
