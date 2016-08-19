package interactor.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor6;

import rx.Subscriber;

public class InteractorAnnotatedClass {

  private static final String PARAMETER_NAME = "subscriber";

  private String methodName;

  private TypeElement annotatedClassElement;
  private String qualifiedName;

  private TypeMirror interactorReturnType;

  private ClassName interactorClassName;

  private List<List<Param>> constructors;

  public InteractorAnnotatedClass(TypeElement classElement) throws ProcessingException {

    TypeMirror superclass = classElement.getSuperclass();
    //gets first generic type
    interactorReturnType = getGenericType(superclass);

    annotatedClassElement = classElement;
    qualifiedName = annotatedClassElement.getQualifiedName().toString();
    interactorClassName = ClassName.get(annotatedClassElement);
    methodName = "execute" + interactorClassName.simpleName();

    constructors = getConstructors();
  }

  private List<List<Param>> getConstructors() {
    List<List<Param>> params = new ArrayList<>();
    for (Element enclosed : annotatedClassElement.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
        List<Param> constructorParam = new ArrayList<>();
        ExecutableElement constructorElement = (ExecutableElement) enclosed;
        List<? extends VariableElement> constructorParams = constructorElement.getParameters();
        for (VariableElement element : constructorParams) {
          constructorParam.add(new Param(element.getSimpleName().toString(), element.asType()));
        }
        params.add(constructorParam);
      }
    }
    return params;
  }

  public void generateExecuteMethod(TypeSpec.Builder interfaceBuilder, TypeSpec.Builder classBuilder) {

    //get parameters
    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Subscriber.class), WildcardTypeName.get(interactorReturnType));
    ParameterSpec parameterSpec = ParameterSpec.builder(parameterizedTypeName, PARAMETER_NAME, Modifier.FINAL).build();

    for (List<Param> constructor : constructors) {
      if (classBuilder != null) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).addParameter(parameterSpec);

        StringBuilder constructorParams = new StringBuilder();
        //add constructor params
        for (int i = 0; i < constructor.size(); i++) {
          Param param = constructor.get(i);
          String name = param.name;
          constructorParams.append(name);
          if (i != constructor.size() - 1) {
            //not last
            constructorParams.append(", ");
          }
          methodBuilder.addParameter(ParameterSpec.builder(ParameterizedTypeName.get(param.type), name, Modifier.FINAL).build());
        }

        methodBuilder.returns(void.class);
        if (!constructor.isEmpty()) {
          methodBuilder.addStatement("new $T($L).execute($L)", interactorClassName, constructorParams.toString(), PARAMETER_NAME);
        } else {
          methodBuilder.addStatement("new $T().execute($L)", interactorClassName, PARAMETER_NAME);
        }

        classBuilder.addMethod(methodBuilder.build());
      }

      if (interfaceBuilder != null) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(parameterSpec).returns(void.class);
        if (!constructor.isEmpty()) {
          for (int i = 0; i < constructor.size(); i++) {
            Param param = constructor.get(i);
            methodBuilder.addParameter(ParameterSpec.builder(ParameterizedTypeName.get(param.type), param.name, Modifier.FINAL).build());
          }
        }
        interfaceBuilder.addMethod(methodBuilder.build());
      }
    }
  }

  /**
   * The original element that was annotated with @Factory
   */
  public TypeElement getTypeElement() {
    return annotatedClassElement;
  }

  public CharSequence getQualifiedName() {
    return qualifiedName;
  }

  public static TypeMirror getGenericType(final TypeMirror type) {
    final TypeMirror[] result = {null};

    type.accept(new SimpleTypeVisitor6<Void, Void>() {
      @Override
      public Void visitDeclared(DeclaredType declaredType, Void v) {
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (!typeArguments.isEmpty()) {
          result[0] = typeArguments.get(0);
        }
        return null;
      }

      @Override
      public Void visitPrimitive(PrimitiveType primitiveType, Void v) {
        return null;
      }

      @Override
      public Void visitArray(ArrayType arrayType, Void v) {
        return null;
      }

      @Override
      public Void visitTypeVariable(TypeVariable typeVariable, Void v) {
        return null;
      }

      @Override
      public Void visitError(ErrorType errorType, Void v) {
        return null;
      }

      @Override
      protected Void defaultAction(TypeMirror typeMirror, Void v) {
        throw new UnsupportedOperationException();
      }
    }, null);

    return result[0];
  }

  private static class Param {
    private final String name;
    private final TypeMirror type;

    public Param(String name, TypeMirror type) {
      this.name = name;
      this.type = type;
    }
  }
}
