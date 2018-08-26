package de.cvguy.kotlin.koreander.filter

class InlineCssFilter : KoreanderFilter {
    override fun filter(input: String): String {
        return """<style>
$input
</style>"""
    }
}