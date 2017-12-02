package de.cvguy.kotlin.koreander.parser.token

class ElementClass(
        val name: String,
        line: Int,
        character: Int
) : Token(line, character)