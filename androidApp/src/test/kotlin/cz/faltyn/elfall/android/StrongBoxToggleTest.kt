package cz.faltyn.elfall.android

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StrongBoxToggleTest {
    @Test fun oldApiFailsClosed() { assertFailsWith<IllegalStateException> { strongBoxOnly(27) { 1 } } }
    @Test fun missingStrongBoxFailsClosed() {
        assertFailsWith<IllegalStateException> { strongBoxOnly(33) { throw RuntimeException("no StrongBox") } }
    }
    @Test fun strongBoxAvailable() = assertEquals(7, strongBoxOnly(33) { 7 })
    @Test fun defaultIsOff() = assertEquals(false, BuildConfig.STRONGBOX_REQUIRED)
}
