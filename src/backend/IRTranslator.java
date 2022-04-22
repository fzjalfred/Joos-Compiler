package backend;

import ast.CompilationUnit;
import ast.Expr;
import ast.FieldDecl;
import ast.MethodDecl;
import backend.asm.Tile;
import exception.BackendError;
import tir.src.joosc.ir.ast.*;
import tir.src.joosc.ir.interpret.Simulator;
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

    public FuncDecl createStaticFieldInit() {
        List<FieldDecl> fieldDecls = new ArrayList<FieldDecl>();
        for (CompUnit ir_comp : ir_comps) {
            fieldDecls.addAll(ir_comp.staticFields);
        }

        List<Statement> stmts = new ArrayList<Statement>();
        Label label = new Label("staticInit");
        stmts.add(label);
        for (FieldDecl staticField : fieldDecls) {
            if (staticField.hasRight() && !(staticField.getExpr().ir_node instanceof Const)) {
                Expr expr = staticField.getExpr();
                if (expr.ir_node == null) {
                    throw new BackendError("field decl not visited");
                }
                Temp temp = new Temp("staticFieldInit");
                stmts.add(new Move(temp, new Name(staticField.getFirstVarName() + "_" + staticField.hashCode())));
                stmts.add(new Move(new Mem(temp), expr.ir_node));
            }
        }
        stmts.add(new Return(new Const(0)));
        return new FuncDecl("staticInit", 0, new Seq(stmts), new FuncDecl.Chunk());
    }

    public void translate(){
        List<FieldDecl> fieldDecls = new ArrayList<>();
        int index = 0;
        for (CompilationUnit comp : comps){
            if (!comp.fileName.contains("stdlib") || comp.fileName.contains("Object") || comp.fileName.contains("String") || comp.fileName.contains("Arrays") || comp.fileName.contains("Stream") || comp.fileName.contains("Serializable")){
//                System.out.println(comp.fileName);
                comp.accept(visitor);
                ir_comps.add(visitor.compUnit);
                if (index != 0) {
                    fieldDecls.addAll(visitor.compUnit.staticFields);
                }
            }
            index++;
        }
        FuncDecl staticInit = createStaticFieldInit();
        ir_comps.get(0).appendFunc(staticInit);
        for (FieldDecl fieldDecl : fieldDecls) {
            ir_comps.get(0).externStrs.add(fieldDecl.getFirstVarName() + "_" + fieldDecl.hashCode());
        }
        System.out.println(ir_comps.get(0).functions());

    }

    public void canonicalize(CompUnit compUnit){
//        System.out.println("before canonicalize");
//        System.out.println(compUnit.functions());
//        System.out.println("===================");
        CanonicalizeVisitor cv = new CanonicalizeVisitor();
        cv.visit(compUnit);
        cv.processComp(compUnit);
//        System.out.println("after canonicalize");
//        System.out.println(compUnit.functions());
        CheckCanonicalIRVisitor ckv = new CheckCanonicalIRVisitor();
//        System.out.print("Canonical? ");
//        System.out.println(ckv.visit(compUnit));
//        System.out.println(ckv.offender);
    }

    public Tile tiling(CompUnit compUnit){
        TilingVisitor tv = new TilingVisitor();
        return tv.visit(compUnit);
    }




}
