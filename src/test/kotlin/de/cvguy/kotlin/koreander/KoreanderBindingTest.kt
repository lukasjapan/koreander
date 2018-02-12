package de.cvguy.kotlin.koreander

import de.cvguy.kotlin.koreander.exception.InvalidTypeException
import org.junit.Assert
import org.junit.Test

class KoreanderBindingTest {
    val koreander = Koreander()
    val unit = Koreander.typeOf(Unit)

    @Test
    fun failsIfWrongTypeIsGiven() {
        val compiled = koreander.compile("", unit)
        try {
            koreander.render(compiled, "This is a String, not Unit")
            Assert.fail()
        }
        catch(e: InvalidTypeException) {
        }
    }
}