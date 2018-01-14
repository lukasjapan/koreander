package de.cvguy.kotlin.koreander

import org.junit.Test
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import java.io.StringWriter
import java.io.StringReader
import java.net.URL
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import org.junit.Assert.assertEquals
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.ArrayList
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource








@RunWith(Parameterized::class)
class SyntaxTest(val input: Path, val output: Path) {
    val koreander = Koreander()
    val context = TestContext()

    data class TestContext(
            val string: String = "This is a string."
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Path>> {
            val url = SyntaxTest::class.java.getResource("/syntax")
            val path = Paths.get(url.toURI())
            val fileSets = mutableListOf<Array<Path>>()

            // find all .kor files for testing
            Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if(file.toString().endsWith(".kor")) {
                        val filename = file.toString().dropLast(4) + ".html"
                        val expected = Paths.get(filename)
                        fileSets.add(arrayOf(file, expected))
                    }
                    return FileVisitResult.CONTINUE
                }
            })

            return fileSets
        }
    }

    @Test
    fun RenderAndCheckOutput() {
        val templateInput = input.toFile().readText()
        val expectedOutput = output.toFile().readText()
        val actualOutput = koreander.render(templateInput, context)
        assertEquals(expectedOutput, actualOutput)
    }
}