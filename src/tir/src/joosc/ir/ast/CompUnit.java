package tir.src.joosc.ir.ast;

import ast.AbstractMethodDecl;
import ast.ClassDecl;
import ast.MethodDecl;
import ast.TypeDecl;
import backend.asm.Code;
import backend.asm.LabelOperand;
import backend.asm.Tile;
import backend.asm.dcc;
import exception.BackendError;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CanonicalizeVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;
import utils.tools;

import java.util.*;

/**
 * An intermediate representation for a compilation unit
 */
public class CompUnit extends Node_c {
    private String name;
    private Map<String, FuncDecl> functions;
    public Map<String, String> stringLiteralToLabel;
    public Set<String> externStrs;
    public TypeDecl oriType;

    public List<Code> constructVtable(){
        if (oriType instanceof ClassDecl){
            ClassDecl classDecl = (ClassDecl)oriType;
            Code[] codes = new Code[classDecl.methodMap.size()+2];
            codes[0] = new dcc(dcc.ccType.d, new LabelOperand(tools.getItable(classDecl)));
            if (classDecl.parentClass != null) {
                codes[1] = new dcc(dcc.ccType.d, new LabelOperand(tools.getVtable(classDecl.parentClass)));
                externStrs.add(tools.getVtable(classDecl.parentClass));
            }
            else codes[1] = new dcc(dcc.ccType.d, new LabelOperand("0"));
            for (MethodDecl methodDecl : classDecl.methodMap.keySet()) {
                String name = methodDecl.getName() + "_" + methodDecl.hashCode();
                if (!classDecl.selfMethodMap.contains(methodDecl)) this.externStrs.add(name);
                int idx = classDecl.methodMap.get(methodDecl) / 4;
                codes[idx] = new dcc(dcc.ccType.d, new LabelOperand(name));
            }
            return new ArrayList<Code>(Arrays.asList(codes));
        }   else {
            return new ArrayList<>();
        }
    }

    public List<Code> constructItable(){
        if (oriType instanceof ClassDecl){
            ClassDecl classDecl = (ClassDecl)oriType;
            Map<AbstractMethodDecl, Integer> methods_in_itable = classDecl.interfaceMethodMap;
            ClassDecl parentInterfaceMethod_iterater = classDecl.parentClass;
            while(parentInterfaceMethod_iterater != null) {
                methods_in_itable.putAll(parentInterfaceMethod_iterater.interfaceMethodMap);
                parentInterfaceMethod_iterater = parentInterfaceMethod_iterater.parentClass;
            }
            // calc itable size
            //int N = (int)Math.ceil(Math.log(methods_in_itable.size())/Math.log(2));
            int size = methods_in_itable.size();
            Code[] codes = new Code[size];
            for (AbstractMethodDecl methodDecl: methods_in_itable.keySet()) {
                String name = methodDecl.getName() + "_" + methodDecl.hashCode();
                int idx = methods_in_itable.get(methodDecl)/4;
                codes[idx] = new dcc(dcc.ccType.d, new LabelOperand(name));
            }
            return new ArrayList<Code>(Arrays.asList(codes));
        }   else {
            return new ArrayList<>();
        }
    }

    public CompUnit(String name) {
        this.name = name;
        functions = new LinkedHashMap<>();
        externStrs = new HashSet<String>();
        stringLiteralToLabel = new HashMap<>();
    }

    public CompUnit(String name, Map<String, FuncDecl> functions) {
        this.name = name;
        this.functions = functions;
        externStrs = new HashSet<String>();
        stringLiteralToLabel = new HashMap<>();
    }

    public void appendFunc(FuncDecl func) {
        functions.put(func.name(), func);
    }

    public String name() {
        return name;
    }

    public Map<String, FuncDecl> functions() {
        return functions;
    }

    public FuncDecl getFunction(String name) {
        return functions.get(name);
    }

    @Override
    public String label() {
        return "COMPUNIT";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        boolean modified = false;

        Map<String, FuncDecl> results = new LinkedHashMap<>();
        for (FuncDecl func : functions.values()) {
            FuncDecl newFunc = (FuncDecl) v.visit(this, func);
            if (newFunc != func) modified = true;
            results.put(newFunc.name(), newFunc);
        }

        if (modified) return v.nodeFactory().IRCompUnit(name, results);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        for (FuncDecl func : functions.values())
            result = v.bind(result, v.visit(func));
        return result;
    }

    @Override
    public void canonicalize() {
        canonicalized_node = new Seq();
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        BackendError.currFile = name;
        List<Node> res = new ArrayList<Node>();
        for (FuncDecl f : functions.values()){
            res.add(f);
        }
        return new Pair<List<Node>, Tile>(res, v.unit());
    }
}
