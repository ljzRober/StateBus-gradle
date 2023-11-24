package processor;

import annotation.AbstractSubscribers;
import annotation.StateConstants;
import annotation.StateSubscribe;
import annotation.bean.SubscribeInfo;
import annotation.bean.SubscribeMethod;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
public class StateBusProcessor extends AbstractProcessor {

    private final Map<String, SubscribeInfo> methodsByClass = new HashMap<>();
    private ProcessingEnvironment environment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "hello APT");
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, StateSubscribe.class.getCanonicalName());
        this.environment = processingEnv;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(StateSubscribe.class.getCanonicalName());
        System.out.println("execute process");
        return hashSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("execute process");
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(StateSubscribe.class);
        if (elements.isEmpty()) {
            return false;
        }
        for (Element element : elements) {
            if (element.getKind().equals(ElementKind.METHOD)) {
                String subscriber = element.getSimpleName().toString();
                ExecutableElement executableElement = (ExecutableElement) element;
                StateSubscribe annotation = executableElement.getAnnotation(StateSubscribe.class);
                String canonicalName = getClsName(annotation).canonicalName();
                String methodName = annotation.methodName();
                String state = canonicalName + "$" + methodName;
                environment.getMessager().printMessage(Diagnostic.Kind.NOTE, state);
                TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
                String qualifiedName = typeElement.getQualifiedName().toString();
                environment.getMessager().printMessage(Diagnostic.Kind.NOTE, qualifiedName);
                if (methodsByClass.containsKey(qualifiedName)) {
                    methodsByClass.get(qualifiedName).addSubscribeMethod(new SubscribeMethod(subscriber, state));
                } else {
                    SubscribeInfo subscribeInfo = new SubscribeInfo(qualifiedName);
                    subscribeInfo.addSubscribeMethod(new SubscribeMethod(subscriber, state));
                    methodsByClass.put(qualifiedName, subscribeInfo);
                }
            }
        }
        return writeClassFile();
    }

    private ClassName getClsName(StateSubscribe stateSubscribe) {
        if (stateSubscribe == null) {
            return null;
        }
        ClassName className = null;
        try {
            Class<?> classPath = stateSubscribe.classPath();
            className = ClassName.get(classPath);
        } catch (MirroredTypeException e) {
//            e.printStackTrace();
            TypeMirror typeMirror = e.getTypeMirror();
            if (typeMirror != null) {
                TypeName typeName = ClassName.get(typeMirror);
                if (typeName instanceof ClassName) {
                    className = (ClassName) typeName;
                }
            }
        }
        return className;
    }

    private boolean writeClassFile() {

        environment.getMessager().printMessage(Diagnostic.Kind.NOTE, "execute writeClassFile");
        MethodSpec methodSpec = MethodSpec.methodBuilder("importPackages")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(SubscribeInfo.class, "SubscribeInfo")
                .addParameter(SubscribeMethod.class, "SubscribeMethod")
                .build();

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        methodsByClass.forEach((aClass, subscribeInfo) -> {
            codeBlock.add("putIndex(new SubscribeInfo($S))",
                    subscribeInfo.getClassPath())
                    .addStatement("");
            subscribeInfo.getSubscribeMethods().forEach(subscribeMethod -> {
                codeBlock.add("putMethod($S, new SubscribeMethod($S, $S))",
                        subscribeInfo.getClassPath(),
                        subscribeMethod.getMethodName(),
                        subscribeMethod.getSubscribeState())
                        .addStatement("");
            });
        });
        CodeBlock block = codeBlock.build();

        TypeSpec SubscribeClass = TypeSpec.classBuilder(StateConstants.ClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(AbstractSubscribers.class)
                .addStaticBlock(block)
                .addMethod(methodSpec)
                .build();

        JavaFile javaFile = JavaFile.builder(StateConstants.packagePath, SubscribeClass)
                .build();

        try {
            javaFile.writeTo(this.environment.getFiler());
            environment.getMessager().printMessage(Diagnostic.Kind.NOTE, "execute finish");

        } catch (IOException e) {
            e.printStackTrace();
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return false;
        }
        return true;
    }

}
