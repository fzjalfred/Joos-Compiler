package tir.src.joosc.ir.ast;

import ast.*;
import backend.asm.nop;
import backend.asm.Code;
import backend.asm.LabelOperand;
import backend.asm.Tile;
import backend.asm.dcc;
import exception.BackendError;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CanonicalizeVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import type.RootEnvironment;
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
    public Set<String> definedLabels;
    public TypeDecl oriType;
    public RootEnvironment env;
    public Map<AbstractMethodDecl, Integer> interfaceMethodMap = null;
    public List<FieldDecl> staticFields = new ArrayList<FieldDecl>();

    public List<Code> constructVtable(){
        if (oriType instanceof ClassDecl){
            externStrs.add(tools.getVtable((ClassDecl)env.lookup(tools.nameConstructor("java.lang.String")),env));
            ClassDecl classDecl = (ClassDecl)oriType;
            Code[] codes = new Code[classDecl.methodMap.size()+2];
            codes[0] = new dcc(dcc.ccType.d, new LabelOperand(tools.getItable(classDecl, env)));
            definedLabels.add(tools.getItable(classDecl, env));
            definedLabels.add(tools.getVtable(classDecl, env));
            if (classDecl.parentClass != null) {
                codes[1] = new dcc(dcc.ccType.d, new LabelOperand(tools.getVtable(classDecl.parentClass, env)));
                externStrs.add(tools.getVtable(classDecl.parentClass, env));
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
                for (AbstractMethodDecl collision_method: parentInterfaceMethod_iterater.interfaceMethodMap.keySet()) {
                    methods_in_itable.put(collision_method, classDecl.itable_offset_counter);
                    classDecl.itable_offset_counter+=4;
                }
                //methods_in_itable.putAll(parentInterfaceMethod_iterater.interfaceMethodMap);
                parentInterfaceMethod_iterater = parentInterfaceMethod_iterater.parentClass;
            }
            this.interfaceMethodMap = methods_in_itable;
            // calc itable size
            int N = methods_in_itable.size();
            int bitmask = (int)Math.pow(2, N)-2;
            int size = 1;
            if (bitmask >= 0) {
                size = bitmask+2;
            }
            Code[] codes = new Code[size];
            codes[0] = new dcc(dcc.ccType.d, new LabelOperand(Integer.toString(bitmask)));
            for (int i = 1; i < size; i++) {
                codes[i] = new dcc(dcc.ccType.d, new LabelOperand("-1"));
            }
//            System.out.println("======create=======");
            Set<Integer> hash_buffer = new HashSet<Integer>();
            while (hash_buffer.size()<N) {
                for (AbstractMethodDecl itable_method: methods_in_itable.keySet()) {
                    int itable_offset = (itable_method.getName().hashCode()&bitmask) + 1;
                    if (hash_buffer.contains(itable_offset)) {
                        hash_buffer.clear();
                        bitmask+=1;
                    }
                    hash_buffer.add(itable_offset);
                }
            }
            
//            System.out.println("curObject: "+(classDecl).getName());
//            System.out.println("methods_in_itable: "+methods_in_itable.size());
            for (AbstractMethodDecl itable_method: methods_in_itable.keySet()) {
                
                for (MethodDecl vtable_method : classDecl.methodMap.keySet()) {
                    if (itable_method.getName().equals(vtable_method.getName()) && 
                    ( (itable_method.getParamType() == null && vtable_method.getParamType() == null)||itable_method.getParamType().equals(vtable_method.getParamType())) ) {
                        String name = itable_method.getName() + "_" + itable_method.hashCode();
                        int itable_offset = (itable_method.getName().hashCode()&bitmask) + 1;
//                        System.out.println("bitmask: " + bitmask);
//                        System.out.println("size: " + size);
//                        System.out.println(itable_method.getName());
//                        System.out.println("offset:"+itable_offset);
                        int idx = classDecl.methodMap.get(vtable_method);
                        codes[itable_offset] = new dcc(dcc.ccType.d, new LabelOperand(Integer.toString(idx)));
                    }
                }
            }
//            System.out.println("=======================");
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
        definedLabels = new HashSet<>();
    }

    public CompUnit(String name, Map<String, FuncDecl> functions) {
        this.name = name;
        this.functions = functions;
        externStrs = new HashSet<String>();
        stringLiteralToLabel = new HashMap<>();
        definedLabels = new HashSet<>();
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
