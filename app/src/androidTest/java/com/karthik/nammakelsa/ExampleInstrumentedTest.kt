package com.karthik.nammakelsa

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun app_context_is_correct() {
        val appContext =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        assertEquals(
            "com.karthik.nammakelsa",
            appContext.packageName
        )
    }

    @Test
    fun firebase_can_initialize() {
        val appContext =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        assertNotNull(appContext)
        assertNotNull(appContext.applicationContext)
    }

    @Test
    fun app_resources_are_accessible() {
        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val appName =
            context.getString(R.string.app_name)

        assertNotNull(appName)
        assert(appName.isNotBlank())
    }
}