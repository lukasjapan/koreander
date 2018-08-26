package de.cvguy.kotlin.koreander.filter

interface KoreanderFilter {
    /**
     * @param input String Input string after block indent was removed
     * @param indent Int The amount of indent, if indend should be kept, add whitespace to the output accordingly
     * @return String The output which will be added to the HTML document as it is
     */
    fun filter(input: String): String
}