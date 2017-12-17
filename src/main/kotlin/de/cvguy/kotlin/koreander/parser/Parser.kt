package de.cvguy.kotlin.koreander.parser

import de.cvguy.kotlin.koreander.exception.ExpectedOther
import de.cvguy.kotlin.koreander.exception.UnexpectedDocType
import de.cvguy.kotlin.koreander.exception.UnexpectedEndOfInput
import de.cvguy.kotlin.koreander.exception.UnexpextedToken
import de.cvguy.kotlin.koreander.parser.Token.Type.*

import java.util.Stack

class KoreanderParser(
        private val lexer: Lexer = Lexer()
) {
    fun generateScriptCode(input: String, contextClass: String): String {
        return KoreanderParseEngine(lexer.lexString(input), contextClass).parse()
    }
}

class KoreanderParseEngine(
        tokens: List<Token>,
        val contextClass: String
) {
    data class OpenTag(val depth: Int, val closeBy: String, val code: Boolean)

    private val outputVarName = "_koreanderTemplateOutput"
    private val iterator = tokens.listIterator()
    private val openTags = Stack<OpenTag>()
    private val lines = mutableListOf<String>()

    fun parse(): String {
        lines.clear()

        lines.add("val $outputVarName = mutableListOf<String>()")
        lines.add("(bindings[\"context\"] as $contextClass).apply({")

        unshiftDocType()

        // one loop execution processes one line of the template
        while(iterator.hasNext()) {
            val index = iterator.nextIndex()

            // optional whitespace
            unshiftWhiteSpace()

            unshiftTag()

            unshiftCode() || unshiftSilentCode() || unshiftComment() || unshiftText()

            // nothing has been processed
            if(index == iterator.nextIndex()) {
                throw UnexpextedToken(iterator.next())
            }
        }

        closeOpenTags(0)

        lines.add("})")
        lines.add("""$outputVarName.joinToString("\n")""")

        println(lines.joinToString("\n"))

        return lines.joinToString("\n")
    }

    private fun unshiftDocType(): Boolean {
        iterator.nextIfType(DOC_TYPE_IDENTIFIER) ?: return false
        val typeToken = iterator.nextIfType(DOC_TYPE)

        val docTypeLine = when (typeToken?.content) {
            null -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
            "Strict" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
            "Frameset" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">"
            "5" -> "<!DOCTYPE html>"
            "1.1" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
            "Basic" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\" \"http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd\">"
            "Mobile" -> "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.2//EN\" \"http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd\">"
            "RDFa" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">"
            else -> throw UnexpectedDocType(typeToken)
        }

        koreanderPrint(docTypeLine)

        return true
    }

    private fun unshiftWhiteSpace(): Boolean {
        val token = iterator.nextIfType(WHITE_SPACE) ?: return false

        val len = token.content.length

        closeOpenTags(len)

        openTags.push(OpenTag(len, "", true))

        return true
    }

    private fun closeOpenTags(downTo: Int) {
        while (openTags.isNotEmpty() && currentDepth >= downTo) {
            val tag = openTags.pop()
            if (tag.code) {
                if (tag.closeBy.isNotBlank()) {
                    lines.add(tag.closeBy)
                }
            } else {
                koreanderPrint(tag.closeBy)
            }
        }
    }

    private fun unshiftComment(): Boolean {
        val token = iterator.nextIfType(COMMENT) ?: return false
        koreanderPrint("<!-- ${token.content} -->")
        return true
    }

    private fun unshiftText(): Boolean {
        val token = iterator.nextIfType(TEXT) ?: return false
        koreanderPrint(token.content)
        return true
    }

    private fun unshiftTag(): Boolean {
        val elementToken = iterator.nextIfType(ELEMENT_IDENTIFIER)
        val elementExpression = elementToken?.let { iterator.nextForceType(BRACKET_EXPRESSION, STRING) }

        val elementIdToken = iterator.nextIfType(ELEMENT_ID_IDENTIFIER)
        val elementIdExpression = elementIdToken?.let { iterator.nextForceType(BRACKET_EXPRESSION, STRING) }

        val elementClassToken = iterator.nextIfType(ELEMENT_CLASS_IDENTIFIER)
        val elementClassExpression = elementClassToken?.let { iterator.nextForceType(BRACKET_EXPRESSION, STRING) }

        // must have at least one defined
        elementToken ?: elementIdToken ?: elementClassToken ?: return false

        val attributes = mutableListOf<Pair<Token, Token>>()

        while(true) {
            val name = iterator.nextIfType(BRACKET_EXPRESSION, STRING) ?: break
            iterator.nextForceType(ATTRIBUTE_CONNECTOR)
            val value = iterator.nextForceType(BRACKET_EXPRESSION, QUOTED_STRING, STRING)

            attributes.add(Pair(name, value))
        }

        val name = if(elementExpression == null) "div" else expressionCode(elementExpression, true)
        val id = if(elementIdExpression == null) "" else appendAttributeString("id", elementIdExpression)
        val classes = if(elementClassExpression == null) "" else appendAttributeString("class", elementClassExpression)
        val attribute = attributes.map { appendAttributeCode(it.first, it.second) }.joinToString("")

        if(iterator.peek()?.let{ it.type == WHITE_SPACE && it.content.length <= currentDepth} ?: true) {
            koreanderPrint("<$name$id$classes$attribute></$name>")
        }
        else {
            koreanderPrint("<$name$id$classes$attribute>")
            openTags.push(OpenTag(currentDepth, "</$name>", false))
        }

        return true
    }

    private fun appendAttributeString(name: String, value: Token): String {
        val valueExpression = expressionCode(value, true)
        return """ $name="$valueExpression""""
    }

    private fun appendAttributeCode(name: Token, value: Token): String {
        val nameExpression = expressionCode(name, true)
        val valueExpression = expressionCode(value, true)
        return """ $nameExpression="$valueExpression""""
    }

    private fun expressionCode(token: Token, inString: Boolean) = when(token.type) {
        EXPRESSION -> """(${token.content}).toString()""".let { if(inString) inStringExpression(it) else it }
        BRACKET_EXPRESSION -> """(${token.content.substring(1, token.content.length - 1)}).toString()""".let { if(inString) inStringExpression(it) else it }
        QUOTED_STRING -> if(inString) token.content.substring(1, token.content.length - 1) else token.content
        STRING, TEXT -> if(inString) token.content else """"${token.content}""""
        else -> throw ExpectedOther(token, setOf(BRACKET_EXPRESSION, QUOTED_STRING, EXPRESSION, STRING))
    }

    private fun unshiftCode(): Boolean {
        iterator.nextIfType(CODE_IDENTIFIER) ?: return false
        val code = iterator.nextForceType(EXPRESSION)

        if (iterator.nextIsDeeperWhitespace()) {
            openTags.push(OpenTag(currentDepth, "}).toString())", true))
            lines.add("$outputVarName.add(\"$currentWhitespace\" + (${code.content} {")
        } else {
            koreanderPrint(expressionCode(code, true))
        }

        return true
    }

    private fun unshiftSilentCode(): Boolean {
        iterator.nextIfType(SILENT_CODE_IDENTIFIER) ?: return false
        val code = iterator.nextForceType(EXPRESSION)

        if (iterator.nextIsDeeperWhitespace()) {
            openTags.push(OpenTag(currentDepth, "}", true))
            lines.add("${code.content} {\"")
        } else {
            lines.add(expressionCode(code, true))
        }

        return true
    }

    private fun ListIterator<Token>.peek(): Token? {
        if(!hasNext()) return null

        val result = next()

        previous()

        return result
    }

    private fun ListIterator<Token>.nextIsDeeperWhitespace(): Boolean {
        val token = peek() ?: return false
        return token.type == WHITE_SPACE && token.content.length > currentDepth
    }

    private fun ListIterator<Token>.nextIfType(vararg type: Token.Type): Token? {
        val token = peek() ?: return null

        if(type.contains(token.type)) {
            return next()
        }

        return null
    }

    private fun ListIterator<Token>.nextForceType(vararg type: Token.Type): Token {
        return nextIfType(*type) ?: throw ExpectedOther(peek() ?: throw UnexpectedEndOfInput(), type.toSet())
    }

    private val currentDepth get() = openTags.lastOrNull()?.depth ?: 0
    private val currentWhitespace get() = " ".repeat(currentDepth)

    private fun koreanderPrint(input: String) {
        lines.add("$outputVarName.add(\"\"\"$currentWhitespace$input\"\"\")")
    }

    private fun inStringExpression(expression: String): String {
        return """${'$'}{$expression}"""
    }
}