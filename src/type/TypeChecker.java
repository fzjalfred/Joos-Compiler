package type;

import ast.CompilationUnit;
import exception.SemanticError;
import visitors.TypeCheckVisitor;

public class TypeChecker {
    RootEnvironment env;
    TypeCheckVisitor visitor;


    public TypeChecker(RootEnvironment env){
        this.env = env;
        visitor = new TypeCheckVisitor(env);
    }

    /** Check that the types of all expressions and subexpressions conform to the Joos 1W typing rules and that all statements are type-correct.
     * Check that all non-static field and method uses can be resolved to fields and methods that exist.
     * Check that fields/methods accessed as static are actually static, and that fields/methods accessed as non-static are actually non-static.
     * Check that all accesses of protected fields, methods and constructors are in a subtype of the type declaring the entity being accessed, or in the same package as that type.
     * Check that the name of a constructor is the same as the name of its enclosing class.
     * A constructor in a class other than java.lang.Object implicitly calls the zero-argument constructor of its superclass. Check that this zero-argument constructor exists.
     * Check that no objects of abstract classes are created.
     * Check that no bitwise operations occur.
     * Check that the implicit this variable is not accessed in a static method or in the initializer of a static field.*/
    public void check() throws SemanticError{
        checkTypeRules();
        //TODO: extra 8 rules
    }

    private void checkTypeRules() throws SemanticError {
        for (CompilationUnit comp : env.compilationUnits){
            SemanticError.currFile = comp.fileName;
            comp.accept(visitor);
        }
        assert visitor.context.isEmpty();   // assert all frames to be popped
    }
}
