package de.cvguy.kotlin

import de.cvguy.kotlin.koreander.Koreander

data class Beer(
        val name: String,
        val manufacturer: String,
        val alc: Double
)

data class ViewModel(
        val title: String,
        val beers: List<Beer>
)

fun main(args: Array<String>) {
    val koreander = Koreander()

    val viewModel = ViewModel(
            "Japanese Beers",
            listOf(
                    Beer("Asahi Super Dry", "Asahi Breweries Ltd ", 0.05),
                    Beer("Kirin Ichiban Shibori", "Kirin Brewery Company, Limited", 0.05),
                    Beer("Yebisu", "Sapporo Breweries Ltd.", 0.05),
                    Beer("Sapporo Black Label", "Sapporo Breweries Ltd.", 0.05),
                    Beer("The Premium Malts", "Suntory", 0.055),
                    Beer("Kirin Lager", "Kirin Brewery Company, Limited", 0.049)
            )
    )

    val input = Koreander::class.java.getResource("/input.kor")

    val output = koreander.render(input, viewModel)

    println(output)
}