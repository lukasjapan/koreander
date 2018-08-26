package de.cvguy.kotlin.koreander.filter

class UnsafeHtmlFilter : KoreanderFilter {
    override fun filter(input: String): String {
        return input
    }
}