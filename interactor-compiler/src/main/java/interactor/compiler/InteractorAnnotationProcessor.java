package interactor.compiler;

import com.google.auto.service.AutoService;
import com.interactor.Interactor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
public class InteractorAnnotationProcessor extends AbstractProcessor {

  private static final String PACKAGE_NAME = "com.joincoup.app.domain";
  private static final String INTERFACE_NAME = "InteractorProvider";
  private static final String CLASS_NAME = "AbstractInteractorProvider";

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager messager;
  private Set<InteractorAnnotatedClass> interactorAnnotatedClasses = new HashSet<InteractorAnnotatedClass>();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotataions = new LinkedHashSet<String>();
    annotataions.add(Interactor.class.getCanonicalName());
    return annotataions;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Interactor.class)) {
      try {
        // Check if a class has been annotated with @Factory
        if (annotatedElement.getKind() != ElementKind.CLASS) {
          throw new ProcessingException(annotatedElement, "Only classes can be annotated with @%s", Interactor.class.getSimpleName());
        }

        // We can cast it, because we know that it of ElementKind.CLASS
        TypeElement typeElement = (TypeElement) annotatedElement;
        System.out.println("annotated element " + typeElement.getSimpleName());

        InteractorAnnotatedClass annotatedClass = new InteractorAnnotatedClass(typeElement);

        Validator.checkValidClass(elementUtils, typeUtils, annotatedClass);

        interactorAnnotatedClasses.add(annotatedClass);
      } catch (ProcessingException e) {
        error(annotatedElement,"Interactor processing exception " +  e.getMessage());
      }
    }


    System.out.println("write code - Interaction processor");
    writeCode();
    interactorAnnotatedClasses.clear();

    return true;
  }

  private void writeCode() {
    if (interactorAnnotatedClasses.size() == 0) {
      return;
    }

    System.out.println("interactor annotated clases " + interactorAnnotatedClasses.size());

    TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(INTERFACE_NAME)
        .addJavadoc("Auto generated class")
        .addModifiers(Modifier.PUBLIC);

    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CLASS_NAME)
        .addJavadoc("Auto generated class")
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

    for (InteractorAnnotatedClass clazz : interactorAnnotatedClasses) {
      clazz.generateExecuteMethod(interfaceBuilder, classBuilder);
    }

    TypeSpec interfaceSpec = interfaceBuilder.build();
    JavaFile interfaceJavaFile = JavaFile.builder(PACKAGE_NAME, interfaceSpec).build();

    try {
      interfaceJavaFile.writeTo(filer);
    } catch (IOException e) {
      e.printStackTrace();
    }

    ClassName serviceInterface = ClassName.get("", INTERFACE_NAME);
    classBuilder.addSuperinterface(serviceInterface);
    TypeSpec classSpec = classBuilder.build();


    JavaFile classSpecJavaFile = JavaFile.builder(PACKAGE_NAME, classSpec).build();

    try {
      classSpecJavaFile.writeTo(filer);
    } catch (IOException e) {
      System.out.println("cannnot write to filer  - Interaction processor" + e);
    }
  }

  /**
   * Prints an error message
   *
   * @param e The element which has caused the error. Can be null
   * @param msg The error message
   */
  public void error(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
  }
}
