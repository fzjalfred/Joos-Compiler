package ast;


import exception.SemanticError;
import type.EnvironmentBuilder;

import java.util.ArrayList;
import java.util.List;

public class AbstractMethodList implements Referenceable{
    List<AbstractMethodDecl> methods;
    String qualifiedName;

    public AbstractMethodList(String qualifiedName){
        methods = new ArrayList<AbstractMethodDecl>();
        this.qualifiedName = qualifiedName;
    }

    public void add(AbstractMethodDecl method){
        methods.add(method);
    }

    /** check whether two methods are ambiguous or not
     *  1. first check num of params:
     *  2. check each type of params*/
    private void checkMethodDecl(AbstractMethodDecl method1, AbstractMethodDecl method2) throws SemanticError {
        int method1Params = method1.getMethodDeclarator().numParams();
        int method2Params = method2.getMethodDeclarator().numParams();
        if (method1Params != method2Params) return; // if num of param not equal, then they are different methods
        if (method1Params == 0) throw new SemanticError("Ambiguous method name " + method1.getName());
        List<Parameter> method1ParamList = method1.getMethodDeclarator().getParameterList().getParams();
        List<Parameter> method2ParamList = method2.getMethodDeclarator().getParameterList().getParams();
        List<Type> method1Types = EnvironmentBuilder.getTypesFromParams(method1ParamList);
        List<Type> method2Types = EnvironmentBuilder.getTypesFromParams(method2ParamList);
        for (int i = 0; i < method1Types.size(); i++){
            if (!method1Types.get(i).equals(method2Types.get(i))) return;
        }
        throw new SemanticError("Ambiguous method name " + method1.getName());
    }

    public void checkAmbiguousMethodDecl(AbstractMethodDecl abstractMethodDecl) throws SemanticError{
        for (AbstractMethodDecl m : methods){
            assert m.getName().equals(abstractMethodDecl.getName());
            checkMethodDecl(m, abstractMethodDecl);
        }
    }

    @Override
    public String toString() {
        return "AbstractMethodList{" +
                "methods=" + methods +
                '}';
    }
}
