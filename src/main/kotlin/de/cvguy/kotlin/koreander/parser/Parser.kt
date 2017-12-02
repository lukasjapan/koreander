package de.cvguy.kotlin.koreander.parser

import de.cvguy.kotlin.koreander.exception.ParseError
import de.cvguy.kotlin.koreander.parser.token.Token
import de.cvguy.kotlin.koreander.parser.token.Code
import de.cvguy.kotlin.koreander.parser.token.Comment
import de.cvguy.kotlin.koreander.parser.token.DocType
import de.cvguy.kotlin.koreander.parser.token.Element
import de.cvguy.kotlin.koreander.parser.token.ElementId
import de.cvguy.kotlin.koreander.parser.token.ElementClass
import de.cvguy.kotlin.koreander.parser.token.SilentCode
import de.cvguy.kotlin.koreander.parser.token.Text
import de.cvguy.kotlin.koreander.parser.token.WhiteSpace

import java.util.Stack

class KoreanderParser(
        private val lexer: Lexer = Lexer()
) {
    fun generateScriptCode(input: String, contextClass: String): String {
        return KoreanderParseEngine(lexer.lexString(input), contextClass).output
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

    val output: String = run {
        val lines = mutableListOf<String>()

        lines.add("val $outputVarName = mutableListOf<String>()")
        lines.add("(bindings[\"context\"] as $contextClass).apply({")

        for (token in iterator) {
            val outputLines = when (token) {
                is Code -> process(token)
                is Comment -> process(token)
                is DocType -> process(token)
                is Element -> process(token)
                is ElementId -> process(token)
                is ElementClass -> process(token)
                is SilentCode -> process(token)
                is Text -> process(token)
                is WhiteSpace -> process(token)
                else -> throw AssertionError("This should never happen.")
            }

            lines.addAll(outputLines)
        }
        lines.addAll(closeOpenTags(0))

        lines.add("})")
        lines.add("$outputVarName.joinToString(\"\\n\")")

        lines.joinToString("\n")
    }

    private fun process(token: Code): List<String> {
        // check for valid code here

        val isBlock = iterator.peek().takeIf { it is WhiteSpace && it.count > currentDepth } != null

        val line = if (isBlock) {
            openTags.push(OpenTag(currentDepth, "}).toString())", true))
            "$outputVarName.add(\"$currentWhitespace\" + (${token.code} {"
        } else {
            "$outputVarName.add(\"$currentWhitespace\" + (${token.code}).toString())"
        }

        return listOf(line)
    }

    private fun process(token: Comment): List<String> {
        return listOf(outputAdd("<!-- ${token.content} -->"))
    }

    private fun process(token: DocType): List<String> {
        val xmlLine = token.encoding?.let { "<?xml version='1.0' encoding='$it' ?>" }

        val docTypeLine = when (token.type) {
            "" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
            "Strict" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
            "Frameset" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">"
            "5" -> "<!DOCTYPE html>"
            "1.1" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
            "Basic" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML Basic 1.1//EN\" \"http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd\">"
            "Mobile" -> "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.2//EN\" \"http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd\">"
            "RDFa" -> "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">"
            else -> throw ParseError("", token.line, token.character, "Invalid doctype: ${token.type}")
        }

        return listOf(xmlLine, docTypeLine).filterNotNull().map(this::outputAdd)
    }

    private fun process(token: Element): List<String> {
        val id = iterator.peek().takeIf { it is ElementId }?.also { iterator.next() } as? ElementId
        val classes = iterator.peek().takeIf { it is ElementClass }?.also { iterator.next() } as? ElementClass
        return processElement(token, id, classes)
    }

    private fun process(token: ElementId): List<String> {
        val classes = iterator.peek().takeIf { it is ElementClass }?.also { iterator.next() } as? ElementClass
        return processElement(null, token, classes)
    }

    private fun process(token: ElementClass): List<String> {
        return processElement(null, null, token)
    }

    private fun processElement(element: Element?, id: ElementId?, classes: ElementClass?): List<String> {
        val tag = element?.tag ?: "div"
        val idString = id?.let { " id=\"${it.id}\"" } ?: ""
        val classesString = classes?.let { " class=\"${it.name}\"" } ?: ""

        val line = "<$tag$idString$classesString>"

        openTags.push(OpenTag(currentDepth, "</$tag>", false))

        return listOf(outputAdd(line))
    }

    private fun process(token: SilentCode): List<String> {
        // check for valid code here

        val isBlock = iterator.peek()?.takeIf { it is WhiteSpace && it.count > currentDepth } != null

        val line = if (isBlock) {
            openTags.push(OpenTag(currentDepth, "}", true))
            "${token.code}{"
        } else {
            token.code
        }

        return listOf(line)
    }

    private fun process(token: Text): List<String> {
        return listOf(outputAdd(token.content))
    }

    private fun process(token: WhiteSpace): List<String> {
        return closeOpenTags(token.count).also {
            openTags.push(OpenTag(token.count, "", true))
        }
    }

    private fun closeOpenTags(downTo: Int): List<String> {
        val result = mutableListOf<String>()

        while (openTags.isNotEmpty() && currentDepth >= downTo) {
            val tag = openTags.pop()
            if (tag.code) {
                if (tag.closeBy.isNotBlank()) {
                    result.add(tag.closeBy)
                }
            } else {
                result.add(outputAdd(tag.closeBy))
            }
        }

        return result
    }

    private fun ListIterator<Token>.peek(): Token? = if (hasNext()) { next().also { previous() } } else null

    private val currentDepth get() = openTags.lastOrNull()?.depth ?: 0
    private val currentWhitespace get() = " ".repeat(currentDepth)

    private fun outputAdd(input: String) = "$outputVarName.add(\"$currentWhitespace${input.replace("\"", "\\\"")}\")"
}