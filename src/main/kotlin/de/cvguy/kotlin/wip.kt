package de.cvguy.kotlin

import de.cvguy.kotlin.koreander.Koreander


data class Hum<out T>(val xxx: String = "eeeh", val hum: T)


fun main(args: Array<String>) {
    val koreander = Koreander()

    val output = koreander.render("/test.kor", Hum<Int>("eejjj", 346346))

    println(output)
}