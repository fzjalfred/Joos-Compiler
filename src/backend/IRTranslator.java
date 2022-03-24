package backend;

import ast.CompilationUnit;
import ast.MethodDecl;
import tir.src.joosc.ir.ast.FuncDecl;
import visitors.IRTranslatorVisitor;

import java.util.List;
import java.util.Map;

public class IRTranslator {
    private List<CompilationUnit> comps;
    public Map<MethodDecl, FuncDecl> mapping;
    public IRTranslator(List<CompilationUnit> comps){
        this.comps = comps;
        mapping = null;
    }

    public void translate(){
        IRTranslatorVisitor visitor = new IRTranslatorVisitor();
        for (CompilationUnit comp : comps){

            if (!comp.fileName.contains("stdlib")){
                System.out.println(comp.fileName);
                comp.accept(visitor);
            }
        }
        mapping = visitor.mapping;
    }




}
