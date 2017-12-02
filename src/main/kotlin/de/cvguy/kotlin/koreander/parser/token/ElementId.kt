package de.cvguy.kotlin.koreander.parser.token

class ElementId(
        val id: String,
        line: Int,
        character: Int
) : Token(line, character)