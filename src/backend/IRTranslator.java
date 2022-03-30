package backend;

import ast.CompilationUnit;
import ast.MethodDecl;
import tir.src.joosc.ir.ast.CompUnit;
import tir.src.joosc.ir.ast.FuncDecl;
import tir.src.joosc.ir.visit.CanonicalizeVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
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
                comp.accept(visitor);
            }
        }
        mapping = visitor.mapping;
    }

    public void canonicalize(CompUnit compUnit){
        System.out.println("before canonicalize");
        System.out.println(compUnit.functions());
        System.out.println("===================");
        CanonicalizeVisitor cv = new CanonicalizeVisitor();
        cv.visit(compUnit);
        System.out.println("after canonicalize");
        System.out.println(compUnit.functions().get("main").canonicalized_node);
        CheckCanonicalIRVisitor ckv = new CheckCanonicalIRVisitor();
        System.out.print("Canonical? ");
        System.out.println(ckv.visit(compUnit.functions().get("main").canonicalized_node));
        System.out.println(ckv.offender);
    }




}
