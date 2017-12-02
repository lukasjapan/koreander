package de.cvguy.kotlin.koreander.parser.token

class SilentCode(
        val code: String,
        line: Int,
        character: Int
) : Token(line, character)