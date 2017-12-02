package de.cvguy.kotlin.koreander.parser.token

class DocType(
        val type: String,
        val encoding: String?,
        line: Int,
        character: Int
) : Token(line, character) {
    enum class Type(val type: String) {
        TRANSITIONAL(""),
        STRICT(""),
        FRAMESET(""),
        HTML5("5"),
        XHTML1_1("1.1"),
        BASIC("Basic"),
        MOBILE("Mobile"),
        RDFA("RDFa")
    }
}