package de.cvguy.kotlin.koreander.parser.token

class Code(
        val code: String,
        line: Int,
        character: Int
) : Token(line, character)