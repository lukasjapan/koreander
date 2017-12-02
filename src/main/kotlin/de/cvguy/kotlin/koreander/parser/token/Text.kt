package de.cvguy.kotlin.koreander.parser.token

class Text(
        val content: String,
        line: Int,
        character: Int
) : Token(line, character)