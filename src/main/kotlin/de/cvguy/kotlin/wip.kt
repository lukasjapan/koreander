package de.cvguy.kotlin

import de.cvguy.kotlin.koreander.Koreander


data class Hum<out T>(val xxx: String = "eeeh", val hum: T)


fun main(args: Array<String>) {
    val koreander = Koreander()

    val input = """%html
    %head
        %link href=https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css rel="stylesheet" itegrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous"
    %body
        %h1 Koreander - Current Context Example
        %p ${'$'}this"""

//    val output = koreander.render("!!! 5\n" +
//            "%html\n" +
//            "    %head\n" +
//            "    %body\n" +
//            "        %p\n" +
//            "            = xxx\n" +
//            "            = hum\n" +
//            "        .test\n" +
//            "        #onlyid aaa={xxx} bbb=\"ccc\"\n" +
//            "         #id.andclas", Hum<Int>("eejjj", 346346))

    val output = koreander.render(input, Unit)



    println(output)
}