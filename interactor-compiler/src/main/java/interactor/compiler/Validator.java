package interactor.compiler;

import com.interactor.Interactor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class Validator {

  /**
   * Checks if the annotated element observes our rules
   */
  public static void checkValidClass(Elements elementUtils, Types typeUtils, InteractorAnnotatedClass item) throws ProcessingException {

    // Cast to TypeElement, has more type specific methods
    TypeElement classElement = item.getTypeElement();

    if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
      throw new ProcessingException(classElement, "The class %s is not public.", classElement.getQualifiedName().toString());
    }

    // Check if it's an abstract class
    if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new ProcessingException(classElement, "The class %s is abstract. You can't annotate abstract classes with @%", classElement.getQualifiedName().toString(),
          Interactor.class.getSimpleName());
    }

  /*  // Check inheritance: Class must be childclass as specified in @Factory.type();
    TypeElement superClassElement =
        elementUtils.getTypeElement(item.getQualifiedName());
    if (superClassElement.getKind() == ElementKind.INTERFACE) {
      // Check interface implemented
      if (!classElement.getInterfaces().contains(superClassElement.asType())) {
        throw new ProcessingException(classElement,
            "The class %s annotated with @%s must implement the interface %s",
            classElement.getQualifiedName().toString(), Presenter.class.getSimpleName(),
            item.getQualifiedName());
      }
    } else {
      // Check subclassing
      TypeElement currentClass = classElement;
      while (true) {
        TypeMirror superClassType = currentClass.getSuperclass();

        if (superClassType.getKind() == TypeKind.NONE) {
          // Basis class (java.lang.Object) reached, so exit
          throw new ProcessingException(classElement,
              "The class %s annotated with @%s must inherit from %s",
              classElement.getQualifiedName().toString(), Presenter.class.getSimpleName(),
              item.getQualifiedName());
        }

        if (superClassType.toString().equals(item.getQualifiedName())) {
          // Required super class found
          break;
        }

        // Moving up in inheritance tree
        currentClass = (TypeElement) typeUtils.asElement(superClassType);
      }
    }*/

    // Check if an empty public constructor is given
    for (Element enclosed : classElement.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
        ExecutableElement constructorElement = (ExecutableElement) enclosed;
        if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
          // Found an empty constructor
          return;
        }
      }
    }

    // No empty constructor found
    throw new ProcessingException(classElement, "The class %s must provide an public empty default constructor", classElement.getQualifiedName().toString());
  }
}
