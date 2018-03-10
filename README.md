[![Build Status](https://travis-ci.org/lukasjapan/koreander.svg?branch=master)](https://travis-ci.org/lukasjapan/koreander)
[![Coverage Status](https://coveralls.io/repos/github/lukasjapan/koreander/badge.svg?branch=master)](https://coveralls.io/github/lukasjapan/koreander?branch=master)

# ![Koreander](koreander.png) 

Koreander is a HTML template engine for Kotlin with a clean elegant [haml](http://haml.info/) inspired syntax.

## Quick Example

**Code**

```kotlin
data class ViewModel(val name: String)

val viewModel = ViewModel("world")

val input = File("input.kor").readText()
val output = Koreander().render(input, viewModel)
```

**input.kor**

```
%html
    %body
        .content Hello ${name.capitalized()}!
```

->

```html
<html>
    <body>
        <div class="content">Hello World!</div>
    </body>
</html>
```

## Introduction

Koreander is a HTML template engine for Kotlin.
Html tags are defined by an indent based syntax that is similar to [jade4j](https://github.com/neuland/jade4j) (Java) or [slim](http://slim-lang.com/)/[haml](http://haml.info/) (Ruby).
Templates are executed in the context of a view model class from where the properties and methods can be accessed in pure Kotlin code.

By combining the simplicity of Kotlin and HAML, the Koreander template syntax is simply amazing.
Koreander templates are also type-safe!! and have excellent performance due to JVM compilation.

### Installation

Using Koreander is as simple as adding a few lines to your packaging tool.
For Spring integration see [below](#Spring).

#### Gradle

```
repositories {
    maven {
        url  "https://dl.bintray.com/lukasjapan/de.cvguy.kotlin"
    }
}

dependencies {
    compile 'de.cvguy.kotlin:koreander:0.1-WIP'
}
```

#### Maven

TBA

## Usage

After initializing a Koreander instance, create the view model and pass it to the `render` function along with the template.
The template can be passed as `String`, `URL` or `Input Stream`. 

Detailed information of the syntax can be found below.
For the impatient, here are the main points of the Koreander syntax summarized:

- HTML tags are opened by `%` and are closed automatically based on indent rules → `%tag`
- Lines with deeper indent are included inside the next less deep tag
- Attributes can be written right after tags
- There are shortcuts for div tags →  `.class`, `#id`
- Texts are evaluated as Kotlin string templates, therefore Kotlin code can be inserted (almost) anywhere
- Code is executed as if it would be inside the view model class
- Code only lines can be expressed by a leading `-`
- Deeper indented lines after code are passed to the code as a block  → `expression { /* deeper indented lines */ }` 


### Full Example

A template saved as `input.kor`:

```
!!! 5
%html
    %head
        %link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" rel="stylesheet"
    %body
        .container
            %h1 ${title}
            %table class="table table-striped"
                %thead
                    %tr
                        %th Name
                        %th Manufacturor
                        %th Alc. percentage
                %tbody
                    - beers.forEach
                        %tr
                            %td ${it.name}
                            %td ${it.manufacturer}
                            %td ${"%.2f".format(it.alc * 100.0)}%
```

Code:

```kotlin
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

    val input = File("input.kor").readText()

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

    val output = koreander.render(input, viewModel)

    println(output)
}
```

Generated output:

```html
<!DOCTYPE html>
<html>
    <head>
        <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" rel="stylesheet"></link>
    </head>
    <body>
        <div class="container">
            <h1>Japanese Beers</h1>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Manufacturor</th>
                        <th>Alc. percentage</th>
                    </tr>
                </thead>
                <tbody>
                        <tr>
                            <td>Asahi Super Dry</td>
                            <td>Asahi Breweries Ltd </td>
                            <td>5.00%</td>
                        </tr>
                        <tr>
                            <td>Kirin Ichiban Shibori</td>
                            <td>Kirin Brewery Company, Limited</td>
                            <td>5.00%</td>
                        </tr>
                        <tr>
                            <td>Yebisu</td>
                            <td>Sapporo Breweries Ltd.</td>
                            <td>5.00%</td>
                        </tr>
                        <tr>
                            <td>Sapporo Black Label</td>
                            <td>Sapporo Breweries Ltd.</td>
                            <td>5.00%</td>
                        </tr>
                        <tr>
                            <td>The Premium Malts</td>
                            <td>Suntory</td>
                            <td>5.50%</td>
                        </tr>
                        <tr>
                            <td>Kirin Lager</td>
                            <td>Kirin Brewery Company, Limited</td>
                            <td>4.90%</td>
                        </tr>
                </tbody>
            </table>
        </div>
    </body>
</html>
```

### Syntax

#### <!DOCTYPE> declaration

TBA

#### HTML Tags

TBA

#### Attributes

TBA

#### ID shortcut `#` and class shortcut `.`

TBA

#### Text content

TBA

#### Output `=`

TBA

#### Control code `-`

TBA

#### Comment `/`

TBA

## Support

TBA

### Spring

TBA

### Syntax Highlighters

TBA

## Contributing

TBA?

## License

Korander is released under the [MIT license](http://www.opensource.org/licenses/MIT).

## Authors

- Lukas Prasuhn

## TODO

- Ability to disable html safe
- Self closing tags
