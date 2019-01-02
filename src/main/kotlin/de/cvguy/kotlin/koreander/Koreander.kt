package de.cvguy.kotlin.koreander

import de.cvguy.kotlin.koreander.exception.InvalidTypeException
import de.cvguy.kotlin.koreander.filter.InlineCssFilter
import de.cvguy.kotlin.koreander.filter.InlineJavascriptFilter
import de.cvguy.kotlin.koreander.filter.KoreanderFilter
import de.cvguy.kotlin.koreander.filter.UnsafeHtmlFilter
import de.cvguy.kotlin.koreander.parser.KoreanderParser
import de.cvguy.kotlin.koreander.util.getKType
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineBase.CompiledKotlinScript
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.reflect.KType

class Koreander {
    val parser = KoreanderParser()
    val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine as KotlinJsr223JvmLocalScriptEngine
    val filters: MutableMap<String, KoreanderFilter> = mutableMapOf(
            "unsafehtml" to UnsafeHtmlFilter(),
            "js" to InlineJavascriptFilter(),
            "css" to InlineCssFilter()
    )

    init {
        // setup global context etc here
    }

    inline fun <reified T : Any>render(inputStream: InputStream, context: T, charset: Charset = StandardCharsets.UTF_8): String {
        return render(inputStream.readTextAndClose(charset), context)
    }

    inline fun <reified T : Any>render(url: URL, context: T, charset: Charset = StandardCharsets.UTF_8): String {
        return render(url.readText(charset), context)
    }

    inline fun <reified T : Any>render(file: File, context: T, charset: Charset = StandardCharsets.UTF_8): String {
        return render(file.readText(charset), context)
    }

    inline fun <reified T : Any>render(string: String, context: T): String {
        return unsafeRender(compile(string, typeOf(context)), context)
    }

    inline fun <reified T : Any>render(template: CompiledTemplate, context: T): String {
        val type = typeOf(context)

        if(template.type != type) throw InvalidTypeException(template.type, type)

        return unsafeRender(template, context)
    }

    fun unsafeRender(template: CompiledTemplate, context: Any): String {
        val kotlinScript = CompiledKotlinScript(engine, template.codeLine, template.compiledData)

        engine.put("context", context)
        engine.put("filters", filters)

        return kotlinScript.eval().toString()
    }

    fun compile(inputStream: InputStream, type: KType, charset: Charset = StandardCharsets.UTF_8): CompiledTemplate {
        return compile(inputStream.readTextAndClose(charset), type)
    }

    fun compile(url: URL, type: KType, charset: Charset = StandardCharsets.UTF_8): CompiledTemplate {
        return compile(url.readText(charset), type)
    }

    fun compile(file: File, type: KType, charset: Charset = StandardCharsets.UTF_8): CompiledTemplate {
        return compile(file.readText(charset), type)
    }

    fun compile(input: String, type: KType): CompiledTemplate {
        val kotlinScriptCode = parser.generateScriptCode(input, type.toString())

        val kotlinScript = engine.compile(kotlinScriptCode) as CompiledKotlinScript

        return CompiledTemplate(type, kotlinScript.codeLine, kotlinScript.compiledData)
    }

    fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
        return this.bufferedReader(charset).use { it.readText() }
    }

    companion object {
        inline fun <reified T : Any>typeOf(@Suppress("UNUSED_PARAMETER") instance: T) = getKType<T>()
    }
}

