package de.cvguy.kotlin.koreander.filter

class InlineJavascriptFilter : KoreanderFilter {
    override fun filter(input: String): String {
        return """<script type="text/javascript">
$input
</script>"""
    }
}