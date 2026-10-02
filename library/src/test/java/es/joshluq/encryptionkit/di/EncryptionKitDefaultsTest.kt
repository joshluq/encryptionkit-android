package es.joshluq.encryptionkit.di

import es.joshluq.foundationkit.log.LoggerKit
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EncryptionKitDefaultsTest {
    @Test
    fun `emptyPathProvider should return null path`() {
        assertNull(EncryptionKitDefaults.emptyPathProvider.getCertificatePath())
    }

    @Test
    fun `tag constant should be EncryptionKit`() {
        assertEquals("EncryptionKit", EncryptionKitDefaults.TAG)
    }

    @Test
    fun `logger extension functions should delegate to LoggerKit methods with lambdas`() {
        val logger = mockk<LoggerKit>(relaxed = true)
        val throwable = RuntimeException("Boom")

        logger.v { "verbose message" }
        logger.v("CustomTag") { "verbose custom" }

        logger.d { "debug message" }
        logger.d("CustomTag") { "debug custom" }

        logger.i { "info message" }
        logger.i("CustomTag") { "info custom" }

        logger.w { "warn message" }
        logger.w(throwable) { "warn throwable" }
        logger.w("CustomTag", throwable) { "warn custom" }

        logger.e { "error message" }
        logger.e(throwable) { "error throwable" }
        logger.e("CustomTag", throwable) { "error custom" }

        verify { logger.v("EncryptionKit", "verbose message", null) }
        verify { logger.v("CustomTag", "verbose custom", null) }

        verify { logger.d("EncryptionKit", "debug message", null) }
        verify { logger.d("CustomTag", "debug custom", null) }

        verify { logger.i("EncryptionKit", "info message", null) }
        verify { logger.i("CustomTag", "info custom", null) }

        verify { logger.w("EncryptionKit", "warn message", null) }
        verify { logger.w("EncryptionKit", "warn throwable", throwable) }
        verify { logger.w("CustomTag", "warn custom", throwable) }

        verify { logger.e("EncryptionKit", "error message", null) }
        verify { logger.e("EncryptionKit", "error throwable", throwable) }
        verify { logger.e("CustomTag", "error custom", throwable) }
    }
}
