[![Build Status](https://travis-ci.org/lukasjapan/koreander.svg?branch=master)](https://travis-ci.org/lukasjapan/koreander)
[![Coverage Status](https://coveralls.io/repos/github/lukasjapan/koreander/badge.svg?branch=master)](https://coveralls.io/github/lukasjapan/koreander?branch=master)

# Koreander

Koreander is a template engine for Kotlin that builds HTML code from a clean elegant syntax.
The view model is accessed by Kotlin Code which makes Koreander templates very vertasile yet being type-safe.

```
%html
    %body
        .content Welcome to ${"koreander".capitalized()}!
```

```
<html>
    <body>
        <div class="content">
            Welcome to Koreander!
        </div>
    </body>
</html>
```

## Introduction

Koreander is a template engine for Kotlin.
It features an ident based syntax similar to [jade4j](https://github.com/neuland/jade4j) (Java) or [slim](http://slim-lang.com/)/[haml](http://haml.info/) (Ruby).
However, Koreander is completely Kotlin flavored!

Koreander templates have

- type-safety!!
- excellent performance due to JVM compilation
- an amazing syntax for logic thanks to Kotlin
- so much more

### Installation

Using Koreander is as simple as adding a line to your package tool.
For Spring integration see [below](xxx).

#### Gradle

TBA - need to upload the package via CI

#### Maven

TBA - need to upload the package via CI

#### JAR

TBA - direct link to JAR?

### Quick Example

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

Generated HTML:

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

The template code is evaluated in the context of the view model instance!

## Usage

TBA

### Template Context

TBA

### Syntax

TBA

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

- Attribute escaping
- Ability to disable html safe
- Ident on tags with content but without block (%h1 Test -> <h1>Test</h1>)
- Self closing tags
