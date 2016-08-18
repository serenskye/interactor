package interactor.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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

  //repositoryUseCase
  private TypeMirror interactorReturnType;

  private ClassName interactorClassName;


  public InteractorAnnotatedClass(TypeElement classElement) throws ProcessingException {

    TypeMirror superclass = classElement.getSuperclass();
    //gets first generic type
    interactorReturnType = getGenericType(superclass);

    annotatedClassElement = classElement;
    qualifiedName = annotatedClassElement.getQualifiedName().toString();
    interactorClassName = ClassName.get(annotatedClassElement);
    methodName = "execute" + interactorClassName.simpleName();
  }

  public void generateExecuteMethod(TypeSpec.Builder interfaceBuilder, TypeSpec.Builder classBuilder) {

    //get parameters
    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Subscriber.class), WildcardTypeName.get(interactorReturnType));
    ParameterSpec parameterSpec = ParameterSpec.builder(parameterizedTypeName, PARAMETER_NAME, Modifier.FINAL).build();

    if(classBuilder != null) {
      classBuilder.addMethod(MethodSpec.methodBuilder(methodName)
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC).addParameter(parameterSpec).returns(void.class).addStatement("new $T().execute($L)", interactorClassName, PARAMETER_NAME).build());
    }

    if(interfaceBuilder != null) {
      interfaceBuilder.addMethod(MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(parameterSpec).returns(void.class).build());
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
}
