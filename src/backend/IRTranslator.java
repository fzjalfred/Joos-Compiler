package backend;

import ast.CompilationUnit;
import ast.MethodDecl;
import backend.asm.Tile;
import tir.src.joosc.ir.ast.CompUnit;
import tir.src.joosc.ir.ast.FuncDecl;
import tir.src.joosc.ir.visit.CanonicalizeVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import type.RootEnvironment;
import visitors.IRTranslatorVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IRTranslator {
    private List<CompilationUnit> comps;
    public List<CompUnit> ir_comps;
    IRTranslatorVisitor visitor;
    public IRTranslator(List<CompilationUnit> comps, RootEnvironment env){
        this.comps = comps;
        ir_comps = new ArrayList<CompUnit>();
        visitor = new IRTranslatorVisitor(env);
    }

    public void translate(){
        for (CompilationUnit comp : comps){
            if (!comp.fileName.contains("stdlib") || comp.fileName.contains("Object")){
                System.out.println(comp.fileName);
                comp.accept(visitor);
                ir_comps.add(visitor.compUnit);
            }
        }

    }

    public void canonicalize(CompUnit compUnit){
        System.out.println("before canonicalize");
        System.out.println(compUnit.functions());
        System.out.println("===================");
        CanonicalizeVisitor cv = new CanonicalizeVisitor();
        cv.visit(compUnit);
        cv.processComp(compUnit);
        System.out.println("after canonicalize");
        System.out.println(compUnit.functions());
        CheckCanonicalIRVisitor ckv = new CheckCanonicalIRVisitor();
        System.out.print("Canonical? ");
        System.out.println(ckv.visit(compUnit));
        System.out.println(ckv.offender);
    }

    public Tile tiling(CompUnit compUnit){
        TilingVisitor tv = new TilingVisitor();
        return tv.visit(compUnit);
    }




}
