package de.cvguy.kotlin.koreander.parser

import java.io.File
import de.cvguy.kotlin.koreander.parser.Token.Type.*

class Lexer {
    fun lexFile(input: File) = lexString(input.readText())
    fun lexString(input: String): List<Token> {
        val indexedLines = input.split("\n").withIndex()
        return indexedLines.map { LineLexer(it).lexLine() }.flatten()
    }

    // --------------
    // private
    // --------------

    private class LineLexer(private val input: IndexedValue<String>) {
        // copy constructor
        private constructor(other: LineLexer): this(other.input) {
            position = other.position
            result.addAll(other.result)
        }

        private var position: Int = 0
        private val result: MutableList<Token> = mutableListOf()

        private val line
            get() = input.index
        
        private val remainingInput
            get() = input.value.substring(position)

        private fun unshiftToken(type: Token.Type, len: Int) {
            result.add(
                    Token(
                            type,
                            input.value.substring(position, position + len),
                            line,
                            position
                    )
            )
            move(len)
        }

        private fun move(len: Int) {
            position += len
        }

        private fun unshiftToken(type: Token.Type) {
            unshiftToken(type, input.value.length - position)
        }

        fun lexLine(): List<Token> {
            result.clear()
            position = 0

            println("Lexing line: $line")

            // careful when editing
            // the template syntax is very position dependant

            unshiftDocType()
            unshiftWhitespace()

            tryLexing {
                val hasIdentifier = unshiftIdentifier('%', ELEMENT_IDENTIFIER)
                val hasExpression = unshiftBracketExpression() || unshiftSimpleString()
                hasIdentifier && hasExpression
            }

            tryLexing {
                val hasIdentifier = unshiftIdentifier('#', ELEMENT_ID_IDENTIFIER)
                val hasExpression = unshiftBracketExpression() || unshiftSimpleString()
                hasIdentifier && hasExpression
            }

            tryLexing {
                val hasIdentifier = unshiftIdentifier('.', ELEMENT_CLASS_IDENTIFIER)
                val hasExpression = unshiftBracketExpression() || unshiftSimpleString()
                hasIdentifier && hasExpression
            }

            while(true) {
                eatWhitespace()

                tryLexing {
                    val hasNameExpression = unshiftBracketExpression() || unshiftSimpleString()
                    val hasConnector = unshiftIdentifier('=', ATTRIBUTE_CONNECTOR)
                    val hasValueExpression = unshiftBracketExpression() || unshiftQuotedString() || unshiftSimpleString()
                    hasNameExpression && hasConnector && hasValueExpression
                } || break
            }

            unshiftSilentCode() || unshiftCode() || unshiftComment() || unshiftText()

            println("Result: $result")

            return result
        }

        private fun tryLexing(tryCb: LineLexer.() -> Boolean): Boolean {
            val tmp = LineLexer(this)

            if (!tmp.tryCb()) {
                return false
            }

            position = tmp.position
            result.clear()
            result.addAll(tmp.result)

            return true
        }

        private fun eatWhitespace(): Boolean {
            val match = Regex("^ +").find(remainingInput) ?: return false

            move(match.value.length)

            return true
        }

        private fun unshiftDocType(): Boolean {
            val input = remainingInput

            val match = Regex("^!!!( .+)?\$").find(input) ?: return false

            unshiftToken(DOC_TYPE_IDENTIFIER, 3)

            match.groupValues[1].takeIf { it.isNotEmpty() }?.let {
                eatWhitespace()
                unshiftToken(DOC_TYPE)
            }

            return true
        }

        private fun unshiftWhitespace(): Boolean {
            val match = Regex("^ *").find(remainingInput) ?: return false

            unshiftToken(WHITE_SPACE, match.value.length)

            return true
        }

        private fun unshiftSilentCode(): Boolean {
            Regex("^-.+\$").find(remainingInput) ?: return false

            unshiftToken(SILENT_CODE_IDENTIFIER, 1)
            unshiftToken(EXPRESSION)

            return true
        }

        private fun unshiftCode(): Boolean {
            Regex("^=.+\$").find(remainingInput) ?: return false

            unshiftToken(CODE_IDENTIFIER, 1)
            unshiftToken(EXPRESSION)

            return true
        }

        private fun unshiftComment(): Boolean {
            Regex("^\\/.*\$").find(remainingInput) ?: return false

            unshiftToken(COMMENT_IDENTIFIER, 1)
            unshiftToken(COMMENT)

            return true
        }

        private fun unshiftIdentifier(name: Char, type: Token.Type): Boolean {
            remainingInput.firstOrNull() == name || return false

            unshiftToken(type, 1)

            return true
        }

        private fun unshiftText(): Boolean {
            Regex("^.+\$").find(remainingInput) ?: return false

            eatWhitespace()

            unshiftToken(TEXT)

            return true
        }

        private fun unshiftQuotedString(): Boolean {
            val match = Regex("^\"(\\.|[^\"])*\"").find(remainingInput) ?: return false

            unshiftToken(QUOTED_STRING, match.value.length)

            return true
        }

        private fun unshiftSimpleString(): Boolean {
            val match = Regex("^[^ \"={}]+").find(remainingInput) ?: return false

            unshiftToken(STRING, match.value.length)

            return true
        }

        private fun unshiftBracketExpression(): Boolean {
            val input = remainingInput

            input.firstOrNull() == '{' || return false

            // start at second character because the opening bracket has already been checked
            var count = 1
            var pos = 1

            while(pos < input.length) {
                if(input[pos] == '{') count++
                if(input[pos] == '}') count--
                if(count == 0) break
                pos++
            }

            if(count != 0) return false

            unshiftToken(BRACKET_EXPRESSION, pos + 1)

            return true
        }
    }
}