package de.cvguy.kotlin

import de.cvguy.kotlin.koreander.Koreander

data class TemplateContext(
        val name: String,
        val generator: String
)

fun main(args: Array<String>) {
    val koreander = Koreander()

    val input = Koreander::class.java.getResource("/input.kor").readText()

    val output = koreander.render(input, TemplateContext("World", "Koreander"))

    println(output)
}