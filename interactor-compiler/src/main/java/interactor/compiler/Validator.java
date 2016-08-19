package interactor.compiler;

import com.interactor.Interactor;

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
  }
}
