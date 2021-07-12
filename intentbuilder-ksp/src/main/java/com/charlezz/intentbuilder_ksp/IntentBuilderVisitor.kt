package com.charlezz.intentbuilder_ksp

import com.charlezz.intentbuilder.annotation.Extra
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

class IntentBuilderVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : KSVisitorVoid() {


    override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
        super.visitAnnotation(annotation, data)
        logger.warn("visitAnnotation = $annotation")
    }

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        super.visitAnnotated(annotated, data)
        logger.warn("visitAnnotated = $annotated")

    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        logger.warn("@IntentBuilder -> $classDeclaration 발견")

        val candidates = ArrayList<KSPropertyDeclaration>()

        // 후보군 선정
        for (property in classDeclaration.getDeclaredProperties()) {
            for (annotation in property.annotations) {
                if (annotation.shortName.getShortName() == Extra::class.java.simpleName) {
                    logger.warn("@Extra -> ${property.parentDeclaration?.simpleName?.asString()}::${property.simpleName.getShortName()} 발견")
                    candidates.add(property)
                    break
                }
            }
        }

        val packageName = classDeclaration.packageName.asString()

        //빌더 만들기
        makeBuilderFile(packageName, classDeclaration, candidates)

        //파서 만들기
        makeParserFile(packageName, classDeclaration, candidates)
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        logger.warn("visitFunctionDeclaration = $function")
    }

    private fun makeBuilderFile(
        packageName: String,
        classDeclaration: KSClassDeclaration,
        candidates: ArrayList<KSPropertyDeclaration>
    ) {

        val className = "${classDeclaration.simpleName.asString()}Builder"
        val file: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(true, classDeclaration.containingFile!!),
            packageName = packageName,
            fileName = className
        )

        file.appendLine("package $packageName")
        file.appendLine("")
        file.appendLine("import android.content.Intent")
        file.appendLine("import android.content.Context")
        file.appendLine("")
        file.appendLine("class $className(")

        // 생성자 매개변수 만들기
        file.appendLine("\tprivate val context:Context,")
        for (candidate in candidates) {
            file.appendLine(
                "\tprivate val ${candidate.simpleName.asString()}: ${
                    candidate.type.resolve().toString()
                },"
            )
        }
        file.appendLine("){")
        file.appendLine("\tfun build(): Intent {")
        file.appendLine("\t\treturn Intent(context, ${classDeclaration.qualifiedName?.asString()}::class.java).apply {")

        // Extra삽입

        for (candidate in candidates) {
            file.appendLine("\t\t\tputExtra(\"${candidate.simpleName.asString()}\", ${candidate.simpleName.asString()})")
        }

        file.appendLine("\t\t}")
        file.appendLine("\t}")
        file.appendLine("}")
        file.close()
        logger.warn("$className 파일 생성 완료")
    }

    private fun makeParserFile(
        packageName: String,
        classDeclaration: KSClassDeclaration,
        candidates: ArrayList<KSPropertyDeclaration>
    ) {
        // 파서 만들기
        val className = "${classDeclaration.simpleName.asString()}Parser"
        val file: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(true, classDeclaration.containingFile!!),
            packageName = packageName,
            fileName = className
        )

        file.appendLine("package $packageName")
        file.appendLine("")
        file.appendLine("class $className(")
        file.appendLine("\tprivate val activity:${classDeclaration.simpleName.asString()}")
        file.appendLine("){")
        file.appendLine("")
        file.appendLine("\tfun parse(){")
        file.appendLine("\t\tval intent = activity.intent")

        for (candidate in candidates) {
            when (candidate.type.resolve().toString()) {
                "String", "String?" -> {
                    file.appendLine("\t\tactivity.${candidate.simpleName.asString()} = intent.getStringExtra(\"${candidate.simpleName.asString()}\")?:\"\"")
                }
                "Int", "Int?" -> {
                    file.appendLine("\t\tactivity.${candidate.simpleName.asString()} = intent.getIntExtra(\"${candidate.simpleName.asString()}\",0)")
                }
            }
        }

        file.appendLine("\t}")
        file.appendLine("}")

        file.close()
        logger.warn("$className 파일 생성 완료")

    }

}

fun OutputStream.appendLine(str: String) {
    this.write("$str\n".toByteArray())
}