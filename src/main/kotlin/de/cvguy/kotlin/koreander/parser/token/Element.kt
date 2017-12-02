package de.cvguy.kotlin.koreander.parser.token

class Element(
        val tag: String,
        line: Int,
        character: Int
) : Token(line, character)