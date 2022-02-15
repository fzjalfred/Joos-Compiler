package ast;

import exception.SemanticError;
import type.EnvironmentBuilder;

import java.util.*;

public class ConstructorList implements Referenceable{
    List<ConstructorDecl> cons;
    String qualifiedName;

    public ConstructorList(String qualifiedName){
        cons = new ArrayList<ConstructorDecl>();
        this.qualifiedName = qualifiedName;
    }

    public void add(ConstructorDecl ctor){
        cons.add(ctor);
    }

    /** check whether two methods are ambiguous or not
     *  1. first check num of params:
     *  2. check each type of params*/
    private void checkMethodDecl(ConstructorDecl ctor1, ConstructorDecl ctor2) throws SemanticError {
        int method1Params = ctor1.getConstructorDeclarator().numParams();
        int method2Params = ctor2.getConstructorDeclarator().numParams();
        if (method1Params != method2Params) return; // if num of param not equal, then they are different methods
        if (method1Params == 0) throw new SemanticError("Ambiguous method name " + ctor1.getName());
        List<Parameter> method1ParamList = ctor1.getConstructorDeclarator().getParameterList().getParams();
        List<Parameter> method2ParamList = ctor2.getConstructorDeclarator().getParameterList().getParams();
        List<Type> method1Types = EnvironmentBuilder.getTypesFromParams(method1ParamList);
        List<Type> method2Types = EnvironmentBuilder.getTypesFromParams(method2ParamList);
        //System.out.println("comparing " + method1Types + " to " + method2Types + " first " + method1Types.get(0));
        for (int i = 0; i < method1Types.size(); i++){
            if (!method1Types.get(i).equals(method2Types.get(i))) return;
        }
        throw new SemanticError("Ambiguous method name " + ctor1.getName());
    }

    public void checkAmbiguousMethodDecl(ConstructorDecl ctor) throws SemanticError{
        for (ConstructorDecl c : cons){
            assert c.getName().equals(ctor.getName());
            checkMethodDecl(c, ctor);
        }
    }

    @Override
    public String toString() {
        return "ConstructorList{" +
                "cons=" + cons +
                '}';
    }
}
