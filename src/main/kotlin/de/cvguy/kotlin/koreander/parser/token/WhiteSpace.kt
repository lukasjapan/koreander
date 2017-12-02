package de.cvguy.kotlin.koreander.parser.token

class WhiteSpace(
        val count: Int,
        line: Int,
        character: Int
) : Token(line, character)