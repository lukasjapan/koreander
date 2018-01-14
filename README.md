# Koreander

Koreander is a template engine that produces HTML code from a clean elegant whitespace driven syntax.
Koreander templates can embed Kotlin code with all its perks.

#### Example:

resources/input.kor

```
%html
    %head
        %meta name="generator" content={generator.toUpperCase()}
    %body
        %h1 Welcome to Koreander
        .content
            Hello ${name}!
```

main/example.kt

```kotlin
data class TemplateContext(
    val name: String,
    val generator: String
)

fun main(args: Array<String>) {
    val koreander = Koreander()

    val input = Koreander::class.java.getResource("/input.kor").readText()

    val output = koreander.render(input, TemplateContext("World", "Koreander"))

    println(output)
}
```

->

```html
<html>
    <head>
        <meta name="generator" content="KOREANDER"></meta>
    </head>
    <body>
        <h1>
        Welcome to Koreander
        </h1>
        <div class="content">
            Hello World!
        </div>
    </body>
</html>
```

## Introduction

TBA What is Koreander?

### Installation

TBA

#### Gradle

TBA

#### Other?

TBA?

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

- HTML Safety and attribute escaping
- Ident on tags with content but without block (%h1 Test -> <h1>Test</h1>)
- Self closing tags
