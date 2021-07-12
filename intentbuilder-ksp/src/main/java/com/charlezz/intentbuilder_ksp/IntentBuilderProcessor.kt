package com.charlezz.intentbuilder_ksp

import com.charlezz.intentbuilder.annotation.IntentBuilder
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

class IntentBuilderProcessor : SymbolProcessor {

    companion object {
        private val intentBuilderName = IntentBuilder::class.java.canonicalName
    }

    private lateinit var codeGenerator: CodeGenerator
    private lateinit var logger: KSPLogger

    fun init(
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
        this.logger = logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("IntentBuilderProcessor 시작")

        val symbols:Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(intentBuilderName)

        val ret = symbols.filter { !it.validate() }

        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(IntentBuilderVisitor(codeGenerator, logger), Unit) }
        return ret.toList()
    }

    override fun finish() {
        logger.warn("IntentBuilderProcessor 끝")
    }

    override fun onError() {
        logger.error("IntentBuilderProcessor 에러")
    }

}

class IntentBuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return IntentBuilderProcessor().apply {
            init(environment.codeGenerator, environment.logger)
        }
    }
}
