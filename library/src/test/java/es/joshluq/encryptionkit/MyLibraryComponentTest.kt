package es.joshluq.encryptionkit

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test for the library, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MyLibraryComponentTest {
    @Test
    fun testGreet() {
        val result = MyLibraryComponent.greet("Android")
        assertEquals("Hello, Android!", result)
    }
}
