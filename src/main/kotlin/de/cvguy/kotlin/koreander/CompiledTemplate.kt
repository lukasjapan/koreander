package de.cvguy.kotlin.koreander

import org.jetbrains.kotlin.cli.common.repl.ReplCodeLine
import org.jetbrains.kotlin.cli.common.repl.ReplCompileResult
import java.io.Serializable
import kotlin.reflect.KType

/**
 *
 */
data class CompiledTemplate(
        val type: KType,
        val codeLine: ReplCodeLine,
        val compiledData: ReplCompileResult.CompiledClasses
) : Serializable