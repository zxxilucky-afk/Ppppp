package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read pinkora app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PINKORA", appName)
    }

    @Test
    fun `diary pin hashing verifies correct pin and rejects wrong pin`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = SecurityManager(context)
        securityManager.clearAllLocalSecurity()

        assertFalse(securityManager.hasPinFlow.first())
        securityManager.setPin("2580")
        assertTrue(securityManager.hasPinFlow.first())
        assertTrue(securityManager.verifyPin("2580"))
        assertFalse(securityManager.verifyPin("1234"))

        securityManager.clearPin()
        assertFalse(securityManager.hasPinFlow.first())
    }
}
