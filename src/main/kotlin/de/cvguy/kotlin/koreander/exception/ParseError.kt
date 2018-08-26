package de.cvguy.kotlin.koreander.exception

import de.cvguy.kotlin.koreander.parser.Token

open class ParseError(
        val file: String,
        val line: Int,
        val character: Int,
        message: String
) : KoreanderException(message) {
    override val message: String = message
        get() = "Parse error in $file at $line:$character - $field"
}

class UnexpextedToken(token: Token): ParseError("tba", token.line, token.character, "Unexpexted ${token.type}")

class UnexpectedDocType(token: Token): ParseError("tba", token.line, token.character, "Unexpexted DocType ${token.content}")

class ExpectedOther(token: Token, expectedType: Set<Token.Type>): ParseError("tba", token.line, token.character, "Expected ${expectedType.joinToString(",")} but found ${token.type}")

class UnexpectedEndOfInput(): ParseError("tba", 0, 0, "Unexpected end of input")

class InvalidPluginInputExeption(invalidWhitespace: Token) : ParseError("tba", invalidWhitespace.line, invalidWhitespace.character + invalidWhitespace.content.length, "Invalid plugin input. Keep whitespace sane.")