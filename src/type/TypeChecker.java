package type;

import ast.*;
import exception.SemanticError;
import hierarchy.HierarchyChecking;
import dataflowAnalysis.CFG;
import visitors.*;

public class TypeChecker {
    RootEnvironment env;
    TypeCheckVisitor visitor;
    UnreachableStmtVisitor dataflowVisitor;
    HierarchyChecking hierarchyChecker;
    NameDisambiguation nameDisambiguation;

    public TypeChecker(RootEnvironment env, HierarchyChecking hierarchyChecker, NameDisambiguation nameDisambiguation){
        this.env = env;
        this.hierarchyChecker = hierarchyChecker;
        this.nameDisambiguation = nameDisambiguation;
        visitor = new TypeCheckVisitor(env, hierarchyChecker);
        dataflowVisitor = new UnreachableStmtVisitor();

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
        checkUnreachableStmts();
        checkWorkList();
//        nameDisambiguation.rootEnvironmentDisambiguation(env, true);
        //TODO: extra 8 rules
    }

    private void checkTypeRules() throws SemanticError {
        for (CompilationUnit comp : env.compilationUnits){
            if (Foo.contains(comp.fileName)) continue;
            SemanticError.currFile = comp.fileName;
            comp.accept(visitor);
        }
        assert visitor.context.isEmpty();   // assert all frames to be popped
    }

    private void checkUnreachableStmts() throws SemanticError{
        for (CompilationUnit comp : env.compilationUnits){
            if (Foo.contains(comp.fileName)) continue;
            SemanticError.currFile = comp.fileName;
            comp.accept(dataflowVisitor);
        }
        //System.out.println(dataflowVisitor.mapping);
    }

    private void checkWorkList() throws SemanticError {
        for (Referenceable ref : dataflowVisitor.mapping.keySet()) {
            boolean isVoid = false;
           if (ref instanceof ConstructorDecl) {
               isVoid = true;
           }
           if (ref instanceof MethodDecl) {
               if (((MethodDecl)ref).getType() == null) {
                   isVoid = true;
               }
                if (((MethodDecl)ref).isAbstract() || !((MethodDecl)ref).hasMethodBody()) continue;
            } else if (ref instanceof AbstractMethodDecl) {
                continue;
            }
            CFG cfg = dataflowVisitor.mapping.get(ref);
            SemanticError.currFile = cfg.filename;
            cfg.initWorkList();
            cfg.runWorkList(isVoid);
        }



    }
}
