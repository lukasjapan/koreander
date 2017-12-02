package de.cvguy.kotlin.koreander.exception

class ParseError(
        val file: String,
        val line: Int,
        val character: Int,
        message: String
) : KoreanderException(message) {
    override val message: String = message
        get() = "Parse error in $file at $line:$character - $field"
}