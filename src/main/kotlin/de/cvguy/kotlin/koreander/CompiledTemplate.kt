package de.cvguy.kotlin.koreander

import de.cvguy.kotlin.koreander.exception.TemplateNotFoundException
import org.jetbrains.kotlin.cli.common.repl.ReplCodeLine
import org.jetbrains.kotlin.cli.common.repl.ReplCompileResult
import java.io.ObjectInputStream
import java.io.Serializable
import kotlin.reflect.KType

/**
 *
 */
data class CompiledTemplate(
        val type: KType,
        val codeLine: ReplCodeLine,
        val compiledData: ReplCompileResult.CompiledClasses
) : Serializable {

    companion object {
        fun fromResource(resourceLocation: String): CompiledTemplate {
            val resource = this::class.java.getResource(resourceLocation) ?: throw TemplateNotFoundException(resourceLocation)

            return ObjectInputStream(resource.openStream()).use { it.readObject() as CompiledTemplate }
        }
    }
}