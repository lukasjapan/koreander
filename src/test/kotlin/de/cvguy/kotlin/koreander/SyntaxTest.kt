package de.cvguy.kotlin.koreander

import org.junit.Test
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized
import org.junit.Assert.assertEquals
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


@RunWith(Parameterized::class)
class SyntaxTest(val input: Path, val output: Path, val testName: String) {
    val koreander = Koreander()
    val context = TestContext()

    data class TestContext(
            val string: String = "This is a string.",
            val list: List<Int> = listOf(5, 7, 9)
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name="{2}")
        fun data(): Collection<Array<Any>> {
            val url = SyntaxTest::class.java.getResource("/syntax")
            val path = Paths.get(url.toURI())
            val fileSets = mutableListOf<Array<Any>>()

            // find all .kor files for testing
            Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if(file.toString().endsWith(".kor")) {
                        val filename = file.toString().dropLast(4) + ".html"
                        val expected = Paths.get(filename)
                        val testName = file
                                .toString()
                                .dropLast(4)
                                .split(File.separator)
                                .takeLast(2)
                                .joinToString(File.separator)
                        fileSets.add(arrayOf(file, expected, testName))
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