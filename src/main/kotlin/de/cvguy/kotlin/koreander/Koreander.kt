package de.cvguy.kotlin.koreander

import de.cvguy.kotlin.koreander.exception.InvalidTypeException
import de.cvguy.kotlin.koreander.exception.TemplateNotFoundException
import de.cvguy.kotlin.koreander.parser.KoreanderParser
import de.cvguy.kotlin.koreander.util.getKType
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineBase.CompiledKotlinScript
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.reflect.KType

class Koreander {
    val parser = KoreanderParser()
    val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine as KotlinJsr223JvmLocalScriptEngine

    init {
        // setup global context etc here
    }

    inline fun <reified T : Any>render(inputStream: InputStream, context: T, charset: Charset = StandardCharsets.UTF_8): String {
        return renderString(inputStream.readTextAndClose(charset), context)
    }

    inline fun <reified T : Any>render(resourceLocation: String, context: T, charset: Charset = StandardCharsets.UTF_8): String {
        return renderString(resourceLocation.readResourceText(charset), context)
    }

    inline fun <reified T : Any>renderString(string: String, context: T): String {
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

        return kotlinScript.eval().toString()
    }

    fun compile(input: String, type: KType): CompiledTemplate {
        val kotlinScriptCode = parser.generateScriptCode(input, type.toString())

        println(kotlinScriptCode)

        val kotlinScript = engine.compile(kotlinScriptCode) as CompiledKotlinScript

        return CompiledTemplate(type, kotlinScript.codeLine, kotlinScript.compiledData)
    }

    fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
        return this.bufferedReader(charset).use { it.readText() }
    }

    fun String.readResourceText(charset: Charset = Charsets.UTF_8):String {
        val resource = javaClass.getResource(this) ?: throw TemplateNotFoundException(this)
        return resource.readText(charset)
    }

    companion object {
        inline fun <reified T : Any>typeOf(instance: T) = getKType<T>()
    }
}

