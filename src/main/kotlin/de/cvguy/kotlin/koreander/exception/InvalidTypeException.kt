package de.cvguy.kotlin.koreander.exception

import kotlin.reflect.KType

class InvalidTypeException(
        val expectedType: KType,
        val actualType: KType
) : KoreanderException("InvalidType: expected a $expectedType but got a $actualType.")