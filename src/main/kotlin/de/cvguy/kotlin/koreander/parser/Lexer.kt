package de.cvguy.kotlin.koreander.parser

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
import java.io.File

class Lexer {
    fun lexFile(input: File): List<Token> = lexString(input.readText())
    fun lexString(input: String): List<Token> = input.split("\n").withIndex().map(this::lexLine).flatten()

    // --------------
    // private
    // --------------

    private fun lexLine(line: IndexedValue<String>): List<Token> {
        println("Lexing line: ${line.value}")

        val lineNo = line.index
        var characterNo = 0
        var remainingInput = line.value

        val tokens = mutableListOf<Token>()

        fun MatchResult.process(cb: (MatchResult) -> Token) {
            tokens.add(cb(this))
            characterNo += this.value.length
            remainingInput = remainingInput.drop(this.value.length)
        }

        // Careful when editing, order matters

        if (lineNo == 0) Regex("^!!! ?(.*?) ?(.*?)\$").find(remainingInput)?.process {
            DocType(it.groupValues[1], it.groupValues[2].takeIf { it.isNotBlank() }, lineNo, characterNo)
        }

        if (characterNo == 0) Regex("^ *").find(remainingInput)?.process {
            WhiteSpace(it.value.length, lineNo, characterNo)
        }

        Regex("^- (.+)\$").find(remainingInput)?.process {
            SilentCode(it.groupValues[1], lineNo, characterNo + 2)
        }

        Regex("^= (.+)\$").find(remainingInput)?.process {
            Code(it.groupValues[1], lineNo, characterNo + 2)
        }

        Regex("^\\/ ?(.*)\$").find(remainingInput)?.process {
            Comment(it.groupValues[1], lineNo, characterNo + 2)
        }

        Regex("^%(\\w+) ?").find(remainingInput)?.process {
            Element(it.groupValues[1], lineNo, characterNo)
        }

        Regex("^#(\\w+) ?").find(remainingInput)?.process {
            ElementId(it.groupValues[1], lineNo, characterNo)
        }

        Regex("^\\.(\\w+) ?").find(remainingInput)?.process {
            ElementClass(it.groupValues[1], lineNo, characterNo)
        }

        Regex("^.+$").find(remainingInput)?.process {
            Text(it.value, lineNo, characterNo)
        }

        println("Result: $tokens")

        return tokens
    }
}