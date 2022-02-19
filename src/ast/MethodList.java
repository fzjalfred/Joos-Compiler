package ast;

import exception.SemanticError;
import type.EnvironmentBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MethodList implements Referenceable{
    public List<MethodDecl> methods;
    public String qualifiedName;

    public MethodList(String qualifiedName){
        methods = new ArrayList<MethodDecl>();
        this.qualifiedName = qualifiedName;
    }

    public String getSimpleName(){
        String [] arr = qualifiedName.split(Pattern.quote("."));
        if (arr.length == 0) {
            return qualifiedName;
        }
        return arr[arr.length - 1];
    }

    public void add(MethodDecl method){
        methods.add(method);
    }

    /** check whether two methods are ambiguous or not
     *  1. first check num of params:
     *  2. check each type of params*/
    private void checkMethodDecl(MethodDecl method1, MethodDecl method2) throws SemanticError {
        int method1Params = method1.getMethodHeader().getMethodDeclarator().numParams();
        int method2Params = method2.getMethodHeader().getMethodDeclarator().numParams();
        if (method1Params != method2Params) return; // if num of param not equal, then they are different methods
        if (method1Params == 0) throw new SemanticError("Ambiguous method name " + method1.getName());
        List<Parameter> method1ParamList = method1.getMethodHeader().getMethodDeclarator().getParameterList().getParams();
        List<Parameter> method2ParamList = method2.getMethodHeader().getMethodDeclarator().getParameterList().getParams();
        List<Type> method1Types = EnvironmentBuilder.getTypesFromParams(method1ParamList);
        List<Type> method2Types = EnvironmentBuilder.getTypesFromParams(method2ParamList);
        for (int i = 0; i < method1Types.size(); i++){
            if (!method1Types.get(i).equals(method2Types.get(i))) return;
        }
        throw new SemanticError("Ambiguous method name " + method1.getName());
    }

    public void checkAmbiguousMethodDecl(MethodDecl method) throws SemanticError{
        for (MethodDecl m : methods){
            assert m.getName().equals(method.getName());
            checkMethodDecl(m, method);
        }
    }

    @Override
    public String toString() {
        return "MethodList{" + qualifiedName + " " +
                "methods=" + methods +
                '}';
    }
}
