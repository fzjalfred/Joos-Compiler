package backend;

import ast.*;
import ast.Expr;
import backend.asm.Tile;
import exception.BackendError;
import tir.src.joosc.ir.ast.*;
import tir.src.joosc.ir.ast.Name;
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
            if (staticField.hasRight()) {
                Expr expr = staticField.getExpr();
                if (expr.ir_node == null) {
                    throw new BackendError("field decl not visited");
                }
                Temp temp = new Temp("staticFieldInit");
                stmts.add(new Move(temp, new Name(staticField.getFirstVarName() + "_" + staticField.hashCode())));
                if (staticField.getExpr().ir_node instanceof Const) {
                    Temp content = new Temp("staticInitVal");
                    stmts.add(new Move(content, new Const(0)));
                    stmts.add(new Move(content, expr.ir_node));
                    stmts.add(new Move(new Mem(temp), content));

                } else {
                    stmts.add(new Move(new Mem(temp), expr.ir_node));
                }

            }
        }
        stmts.add(new Return(new Const(0)));
        return new FuncDecl("staticInit", 0, new Seq(stmts), new FuncDecl.Chunk());
    }

    public void translate(){
        List<FieldDecl> fieldDecls = new ArrayList<>();
        int index = 0;
        List <String> classTable = new ArrayList<>();
        for (CompilationUnit comp : comps){
            if (Foo.contains(comp.fileName)){
                ir_comps.add(new CompUnit(comp.fileName));
                continue;
            }
            comp.accept(visitor);
            ir_comps.add(visitor.compUnit);
            if (index != 0) {
                fieldDecls.addAll(visitor.compUnit.staticFields);
                classTable.addAll(visitor.compUnit.definedLabels);
                visitor.compUnit.constructFullName();
                if (visitor.compUnit.VTableName != "") {
                    classTable.add(visitor.compUnit.VTableName);
                    classTable.add(visitor.compUnit.ITableName);
                }  
            }
            index++;
        }
        FuncDecl staticInit = createStaticFieldInit();
        ir_comps.get(0).appendFunc(staticInit);
        for (FieldDecl fieldDecl : fieldDecls) {
            ir_comps.get(0).externStrs.add(fieldDecl.getFirstVarName() + "_" + fieldDecl.hashCode());
        }
        ir_comps.get(0).externStrs.addAll(classTable);
        // System.out.println(ir_comps.get(0).functions());

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
