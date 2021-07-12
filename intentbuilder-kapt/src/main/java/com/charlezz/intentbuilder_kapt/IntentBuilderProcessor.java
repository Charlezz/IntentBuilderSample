package com.charlezz.intentbuilder_kapt;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.charlezz.intentbuilder.annotation.Extra;
import com.charlezz.intentbuilder.annotation.IntentBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public class IntentBuilderProcessor extends AbstractProcessor {

    private static final ClassName intentClass = ClassName.get("android.content", "Intent");
    private static final ClassName contextClass = ClassName.get("android.content", "Context");
    private static final String METHOD_PREFIX_NEW_INTENT = "intentFor";

    ArrayList<MethodSpec> newIntentMethodSpecs = new ArrayList<>();
    private String packageName;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(IntentBuilder.class);

        for (Element element : elements) {
            if (packageName == null) {
                Element e = element;
                while (!(e instanceof PackageElement)) {
                    e = e.getEnclosingElement();
                }
                packageName = ((PackageElement) e).getQualifiedName().toString();
            }

            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "IntentBuilder can only use for classes!");
                return false;
            }

            messager.printMessage(Diagnostic.Kind.NOTE, "packageName:" + packageName);
            makeBuilderFile(element);
            makeParserFile(packageName, element);
        }

        return true;
    }

    private void makeBuilderFile(Element classElement){
        String builderClassName = classElement.getSimpleName().toString();
        TypeSpec.Builder builderBuilder = TypeSpec.classBuilder(builderClassName + "Builder");
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

        // context 필드 선언
        builderBuilder.addField(ClassName.bestGuess("android.content.Context"), "context", Modifier.PRIVATE);
        constructorBuilder.addParameter(ClassName.bestGuess("android.content.Context"),"context");
        constructorBuilder.addStatement("this.context = context");

        List<? extends Element> enclosedElements = classElement.getEnclosedElements();
        for (Element ee : enclosedElements) {
            if (ee.getKind().isField() && ee.getAnnotation(Extra.class) != null) {

                //필드 선언
                builderBuilder.addField(
                        FieldSpec.builder(TypeName.get(ee.asType()), ee.getSimpleName().toString(), Modifier.PRIVATE).build()
                );

                //생성자 매개변수
                constructorBuilder.addParameter(TypeName.get(ee.asType()), ee.getSimpleName().toString());
                constructorBuilder.addStatement("this." + ee.getSimpleName().toString() + " = " + ee.getSimpleName().toString());

            }
        }

        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build");
        buildMethod.addModifiers(Modifier.PUBLIC);
        buildMethod.returns(ClassName.bestGuess("android.content.Intent"));
        buildMethod.addStatement("Intent intent = new Intent(context, "+builderClassName+".class)");

        for (Element ee : enclosedElements) {
            if (ee.getKind().isField() && ee.getAnnotation(Extra.class) != null) {
                buildMethod.addStatement("intent.putExtra(\""+ee.getSimpleName().toString()+"\","+ee.getSimpleName().toString()+")");
            }
        }
        buildMethod.addStatement("return intent");
        builderBuilder.addMethod(buildMethod.build());


        builderBuilder.addMethod(constructorBuilder.build());

        try {
            JavaFile.builder(packageName, builderBuilder.addModifiers(Modifier.PUBLIC).build())
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeParserFile(String packageName, Element classElement){
        String parserClassName = classElement.getSimpleName().toString();
        TypeSpec.Builder parserBuilder = TypeSpec.classBuilder(parserClassName + "Parser");
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addParameter(ClassName.bestGuess(packageName+"."+parserClassName),"activity")
                .addStatement("this.activity = activity")
                .addStatement("this.intent = activity.getIntent()")
                .addModifiers(Modifier.PUBLIC);

        parserBuilder.addField(
                FieldSpec.builder(ClassName.bestGuess(packageName+"."+parserClassName), "activity", Modifier.PRIVATE).build()
        );
        parserBuilder.addField(
                FieldSpec.builder(ClassName.bestGuess("android.content.Intent"), "intent", Modifier.PRIVATE).build()
        );

        //parse 메서드 만들기
        MethodSpec.Builder parseMethodBuilder = MethodSpec.methodBuilder("parse")
                .addModifiers(Modifier.PUBLIC);
//                .addStatement("Intent intent = activity.getIntent()");

        List<? extends Element> enclosedElements = classElement.getEnclosedElements();
        for (Element ee : enclosedElements) {
            if (ee.getKind().isField() && ee.getAnnotation(Extra.class) != null) {
                String key = ee.getSimpleName().toString();
                String typeName = ee.asType().toString();
                if(typeName.equals("java.lang.String")){
                    parseMethodBuilder.addStatement("activity."+key +"= intent.getStringExtra(\""+key+"\")");
                }else if(typeName.equals("int")){
                    parseMethodBuilder.addStatement("activity."+key +"= intent.getIntExtra(\""+key+"\",0)");
                }else{
                    parseMethodBuilder.addStatement("무야호 = "+ee.asType().toString());
                }
            }
        }

        parserBuilder.addMethod(parseMethodBuilder.build());
        parserBuilder.addMethod(constructorBuilder.build());
        try {
            JavaFile.builder(packageName, parserBuilder.addModifiers(Modifier.PUBLIC).build())
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add(IntentBuilder.class.getCanonicalName());
            }
        };
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}