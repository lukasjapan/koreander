package de.cvguy.kotlin.koreander.parser

import de.cvguy.kotlin.koreander.exception.*
import de.cvguy.kotlin.koreander.parser.Token.Type.*
import org.jetbrains.kotlin.backend.common.pop
import org.slf4j.LoggerFactory

import java.util.Stack

val TRIPLE_QUOT = "\"\"\""

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
    val logger = LoggerFactory.getLogger(javaClass)

    abstract class TemplateLine(protected val content: String, depth: Int) {
        var depth: Int = depth
            private set

        abstract fun outputExpression(): String
        open fun templateLine(): String = "_koreanderTemplateOutput.add(${outputExpression()})"
        fun resetDepth() { depth = 0 }
    }

    class OutputTemplateLine(content: String, depth: Int) : TemplateLine(content, depth) {
        override fun outputExpression() = TRIPLE_QUOT + " ".repeat(depth) + content + TRIPLE_QUOT + ".htmlEscape()"
    }

    class HtmlSafeTemplateLine(content: String, depth: Int) : TemplateLine(content, depth) {
        override fun outputExpression() = TRIPLE_QUOT + " ".repeat(depth) + content + TRIPLE_QUOT
    }

    class ControlLine(content: String, depth: Int = 0) : TemplateLine(content, depth) {
        override fun outputExpression() = throw AssertionError("Control lines do not output anything.")
        override fun templateLine(): String = content
    }

    class ExpressionLine(content: String, depth: Int) : TemplateLine(content, depth) {
        override fun outputExpression(): String = (if(depth > 0) TRIPLE_QUOT + " ".repeat(depth) + TRIPLE_QUOT + " + " else "") + content
    }

    class FilteredTemplateLine(content: String, val filter: String, depth: Int) : TemplateLine(content, depth) {
        override fun outputExpression() = TRIPLE_QUOT + " ".repeat(depth) + TRIPLE_QUOT + " + " + TRIPLE_QUOT + content + TRIPLE_QUOT + """.koreanderFilter("$filter").replace("\n", "\n" + " ".repeat($depth))"""
    }

    private val iterator = tokens.listIterator()
    private val lines = mutableListOf<TemplateLine>()
    private val delayedLines = Stack<TemplateLine>()

    fun String.htmlEscape(): String { return replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt").replace("\"", "&quot;") }

    fun parse(): String {
        lines.clear()


        lines.add(ControlLine("import de.cvguy.kotlin.koreander.filter.KoreanderFilter"))
        lines.add(ControlLine("val _koreanderTemplateOutput = mutableListOf<String>()"))
        lines.add(ControlLine("val _koreanderFilter = bindings[\"filters\"] as Map<String, KoreanderFilter>"))
        lines.add(ControlLine("""fun String.htmlEscape(): String { return replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt").replace("\"", "&quot;") }"""))
        lines.add(ControlLine("""fun String.koreanderFilter(filter: String): String { return _koreanderFilter[filter]?.filter(this) ?: "Filter '${"$"}filter' not found." }"""))
        lines.add(ControlLine("(bindings[\"context\"] as $contextClass).apply({"))

        unshiftDocType()

        // one loop execution processes one line of the template
        while(iterator.hasNext()) {
            val index = iterator.nextIndex()

            // optional whitespace
            unshiftWhiteSpace()

            unshiftFilter(true)

            val hadTag = unshiftTag()

            val hadOutput = unshiftFilter(false) || unshiftCode() || unshiftSilentCode() || unshiftComment() || unshiftText()

            // can close tag on the same line (a little hacky for now)
            // maybe inside here, search for (more complex) patterns and process pattern wise
            if(hadTag && hadOutput && iterator.nextIsClosingWhitespace()) {
                oneLinerTagOutput()
            }

            // there is some output, put it next after opening tag as it seems most natural
            if(hadTag && hadOutput && iterator.nextIsDeeperWhitespace()) {
                oneLinerOpenTagOutput()
            }

            // nothing has been processed
            if(index == iterator.nextIndex()) {
                throw UnexpextedToken(iterator.next())
            }
        }

        closeOpenTags(0)

        lines.add(ControlLine("})"))
        lines.add(ControlLine("""_koreanderTemplateOutput.joinToString("\n")"""))

        return lines.map { it.templateLine() }.filter { it.isNotEmpty() }.joinToString("\n").also {
            logger.debug("Templace code: {}", it)
        }
    }

    private fun oneLinerTagOutput() {
        val expressionLine = lines.pop()
        val openingTagLine = lines.pop()
        val closingTagLine = delayedLines.pop()
        val depth = openingTagLine.depth

        openingTagLine.resetDepth()
        expressionLine.resetDepth()
        closingTagLine.resetDepth()

        val expression = listOf(
                openingTagLine.outputExpression(),
                expressionLine.outputExpression(),
                closingTagLine.outputExpression()
        ).joinToString(" + ")

        lines.add(ExpressionLine(expression, depth))
    }

    private fun oneLinerOpenTagOutput() {
        val expressionLine = lines.pop()
        val openingTagLine = lines.pop()
        val depth = openingTagLine.depth

        openingTagLine.resetDepth()
        expressionLine.resetDepth()

        val expression = listOf(
                openingTagLine.outputExpression(),
                expressionLine.outputExpression()
        ).joinToString(" + ")

        lines.add(ExpressionLine(expression, depth))
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

        lines.add(HtmlSafeTemplateLine(docTypeLine, currentDepth))

        return true
    }

    private fun unshiftWhiteSpace(): Boolean {
        val token = iterator.nextIfType(WHITE_SPACE) ?: return false

        val len = token.content.length

        closeOpenTags(len)

        // remember as current depth
        delayedLines.push(ControlLine("", len))

        return true
    }

    private fun unshiftFilter(standalone: Boolean): Boolean {
        val token = iterator.nextIfType(FILTER_IDENTIFIER) ?: return false

        val filter = token.content.trim(':', ' ')
        var input = ""

        if(standalone && iterator.nextIsDeeperWhitespace()) {
            // block mode - get all input from deeper indented block
            val whiteSpace = iterator.next()
            val baseDepth = whiteSpace.content.length
            while(true) {
                val nextToken = iterator.peek()
                if(nextToken == null) break
                if(nextToken.type == WHITE_SPACE) {
                    if(nextToken.content.length < currentDepth) break
                    if(nextToken.content.length < baseDepth) throw InvalidPluginInputExeption(nextToken)
                    input += "\n" + iterator.next().content.substring(baseDepth)
                }
                else {
                    input += iterator.next().content
                }
            }

            lines.add(FilteredTemplateLine(input, filter, token.character))
        }
        else {
            // one line mode - take all tokens unless next whitespace
            while(true) {
                val nextToken = iterator.peek()
                if(nextToken == null) break
                if(nextToken.type == WHITE_SPACE) break
                input += iterator.next().content
            }

            lines.add(FilteredTemplateLine(input, filter, if(standalone) token.character else 0))
        }

        return true
    }

    private fun closeOpenTags(downTo: Int) {
        while (delayedLines.isNotEmpty() && currentDepth >= downTo) {
            lines.add(delayedLines.pop())
        }
    }

    private fun unshiftComment(): Boolean {
        val token = iterator.nextIfType(COMMENT) ?: return false
        lines.add(HtmlSafeTemplateLine("<!-- ${token.content} -->", currentDepth))
        return true
    }

    private fun unshiftText(): Boolean {
        val token = iterator.nextIfType(TEXT) ?: return false
        lines.add(OutputTemplateLine(token.content, currentDepth))
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

        if(iterator.nextIsClosingWhitespace()) {
            lines.add(HtmlSafeTemplateLine("<$name$id$classes$attribute></$name>", currentDepth))
        }
        else {
            lines.add(HtmlSafeTemplateLine("<$name$id$classes$attribute>", currentDepth))
            delayedLines.push(HtmlSafeTemplateLine("</$name>", currentDepth))
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
        EXPRESSION, BRACKET_EXPRESSION -> {
            val expression = if (token.type == BRACKET_EXPRESSION) {
                token.content.substring(1, token.content.length - 1)
            }
            else {
                token.content
            }

            if(inString) {
                "${'$'}{(${expression}).toString().htmlEscape()}"
            }
            else {
                "(${expression}).toString().htmlEscape()"
            }
        }
        QUOTED_STRING, STRING, TEXT -> {
            val content = if (token.type == QUOTED_STRING) {
                token.content.substring(1, token.content.length - 1)
            }
            else {
                token.content
            }

            val codeSafe = !content.contains('$')

            if(inString && codeSafe) {
                content.htmlEscape()
            }
            else if(inString && !codeSafe) {
                "${'$'}{${TRIPLE_QUOT + content + TRIPLE_QUOT}.htmlEscape()}"
            }
            else if(!inString && codeSafe) {
                "\"${content.htmlEscape()}\""
            }
            else/* if(!inString && !codeSafe) */{
                TRIPLE_QUOT + token.content.substring(1, token.content.length - 1) + TRIPLE_QUOT + ".htmlEscape()"
            }
        }
        else -> throw ExpectedOther(token, setOf(BRACKET_EXPRESSION, QUOTED_STRING, EXPRESSION, STRING))
    }

    private fun unshiftCode(): Boolean {
        iterator.nextIfType(CODE_IDENTIFIER) ?: return false
        val code = iterator.nextForceType(EXPRESSION)

        if (iterator.nextIsDeeperWhitespace()) {
            lines.add(ControlLine("_koreanderTemplateOutput.add(\"$currentWhitespace\" + (${code.content} {"))
            delayedLines.push(ControlLine("}).toString())"))
        } else {
            lines.add(ExpressionLine(expressionCode(code, false), currentDepth))
        }

        return true
    }

    private fun unshiftSilentCode(): Boolean {
        iterator.nextIfType(SILENT_CODE_IDENTIFIER) ?: return false
        val code = iterator.nextForceType(EXPRESSION)

        if (iterator.nextIsDeeperWhitespace()) {
            lines.add(ControlLine("${code.content} {"))
            delayedLines.push(ControlLine("}", currentDepth))
        } else {
            lines.add(ControlLine(code.content))
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

    private fun ListIterator<Token>.nextIsClosingWhitespace(): Boolean {
        hasNext() || return true // end of input, will be closed
        val token = peek() ?: return false
        return token.type == WHITE_SPACE && token.content.length <= currentDepth
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

    private val currentDepth get() = delayedLines.lastOrNull()?.depth ?: 0
    private val currentWhitespace get() = " ".repeat(currentDepth)
}