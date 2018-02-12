[![Build Status](https://travis-ci.org/lukasjapan/koreander.svg?branch=master)](https://travis-ci.org/lukasjapan/koreander)
[![Coverage Status](https://coveralls.io/repos/github/lukasjapan/koreander/badge.svg?branch=master)](https://coveralls.io/github/lukasjapan/koreander?branch=master)

# Koreander

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

Koreander is a HTML template engine for Kotlin that forces a clean separation of presentation logic and code.
Templates are executed in the context of a view model that has to be provided explicitly.
The properties and methods of the view model are accessed directly in pure Kotlin code from the template.

Its ident based syntax similar to [jade4j](https://github.com/neuland/jade4j) (Java) or [slim](http://slim-lang.com/)/[haml](http://haml.info/) (Ruby).
This is done fully Kotlin flavoured, resulting in an amazing syntax!

Koreander templates are type-safe!! and have excellent performance due to JVM compilation.

### Installation

Using Koreander is as simple as adding a few lines to your packaging tool.
For Spring integration see [below](xxx).

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

#### JAR

[JAR file on bintray (0.1-WIP)](https://bintray.com/lukasjapan/de.cvguy.kotlin/download_file?file_path=de%2Fcvguy%2Fkotlin%2Fkoreander%2F0.1-WIP%2Fkoreander-0.1-WIP.jar)

## Usage

TBA

### Full Example

A template saved as `input.kor`:

```
%html
    %head
        %meta name="generator" content={generatorName.toUpperCase()}
    %body
        %h1 Welcome to Koreander
        .content
            Hello ${name}!
```

View model `TemplateContext` and main code:

```kotlin
import de.cvguy.kotlin.koreander.Koreander

data class TemplateContext(
    val name: String,
    val generatorName: String
)

fun main(args: Array<String>) {
    val koreander = Koreander()

    val input = File("input.kor").readText()

    val viewModel = TemplateContext("World", "Koreander")

    val output = koreander.render(input, viewModel)

    println(output)
}
```

Generated output:

```html
<html>
    <head>
        <meta name="generator" content="KOREANDER"></meta>
    </head>
    <body>
        <h1>Welcome to Koreander</h1>
        <div class="content">
            Hello World!
        </div>
    </body>
</html>
```

### Template Context

TBA

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
