package com.charlezz.intentbuilder

import android.app.Activity
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor


/**
 * ksp -> compile 타임시 source code 분석 -> 파일 생성
 * kapt -> stub 생성 -> 생성 된 stub을 다시 컴파일 -> 분석 -> 파일 생성
 *
 */
object IntentExtraInjector {
    fun inject(activity: Activity) {
        try {
            // 코틀린O 자바x
            val parserName = "${activity::class.simpleName}Parser"
            val parserClass = Class.forName("${activity.packageName}.$parserName").kotlin
            val parser = parserClass.primaryConstructor?.call(activity)

            parserClass.memberFunctions.forEach { kFunction: KFunction<*> ->
                if (kFunction.name == "parse") {
                    kFunction.call(parser)
                }
            }
        } catch (e: Exception) {
            // 코틀린x 자바O 일 때 다시 한번 시도
            try {

                val parserName = "${activity::class.simpleName}Parser"
                val parserClassJava = Class.forName("${activity.packageName}.$parserName")
                val parserConstructor = parserClassJava.getConstructor(activity::class.java)

                val parserInstance = parserConstructor.newInstance(activity)
                parserClassJava.getDeclaredMethod("parse").invoke(parserInstance)
            } catch (e: java.lang.Exception) {
                throw java.lang.RuntimeException(
                    "Exception occurred while parse intent extras!",
                    e
                )
            }
        }
    }
}