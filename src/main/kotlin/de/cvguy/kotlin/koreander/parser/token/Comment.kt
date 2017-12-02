package de.cvguy.kotlin.koreander.parser.token

class Comment(
        val content: String,
        line: Int,
        character: Int
) : Token(line, character)