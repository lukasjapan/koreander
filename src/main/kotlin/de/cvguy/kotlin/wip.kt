package de.cvguy.kotlin

import de.cvguy.kotlin.koreander.Koreander


data class Hum<out T>(val xxx: String = "eeeh", val hum: T)


fun main(args: Array<String>) {
    val koreander = Koreander()

    val output = koreander.render("!!! 5\n" +
            "%html\n" +
            "    %head\n" +
            "    %body\n" +
            "        %p\n" +
            "            = xxx\n" +
            "            = hum\n" +
            "        .test\n" +
            "        #onlyid aaa={xxx} bbb=\"ccc\"\n" +
            "         #id.andclas", Hum<Int>("eejjj", 346346))

    println(output)
}