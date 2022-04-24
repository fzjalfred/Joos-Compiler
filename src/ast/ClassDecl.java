package ast;

import backend.IRTranslator;
import tir.src.joosc.ir.ast.Exp;
import visitors.IRTranslatorVisitor;
import visitors.Visitor;

import java.util.*;

import utils.tools;

public class ClassDecl extends TypeDecl{
    // children( modifiers ID Super Interface ClassBody)
    public List<ClassDecl> subclasses = new ArrayList<ClassDecl>();
    public ClassDecl parentClass = null;
    public ConstructorDecl supercall = null;

    public Map<FieldDecl, Integer> fieldMap = new HashMap<FieldDecl, Integer>();
    public Map<MethodDecl, Integer> methodMap = new HashMap<MethodDecl, Integer>();
    public Map<AbstractMethodDecl, Integer> interfaceMethodMap = new HashMap<AbstractMethodDecl, Integer>();
    public Integer itable_offset_counter = 0;
    public Set<MethodDecl> selfMethodMap = new HashSet<>();

    public Map<String, List<ASTNode>> containMap = null;

    public ClassDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(1).value;
    }
    public Modifiers getModifiers() {
        assert children.get(0) instanceof Modifiers;
        return (Modifiers)children.get(0);
    }
    public ClassBodyDecls getClassBodyDecls(){
        if (children.get(4) == null) return null;
        assert children.get(4).children.get(0) instanceof ClassBodyDecls;
        return (ClassBodyDecls)children.get(4).children.get(0);
    }

    public List<FieldDecl> getAllNonStaticFieldDecls() {
        if (parentClass != null) {
            List <FieldDecl> res =  parentClass.getAllNonStaticFieldDecls();
            res.addAll(getNonStaticFieldDecls());
            return res;
        }
        return getNonStaticFieldDecls();
    }

    public List<FieldDecl> getNonStaticFieldDecls(){
        return getClassBodyDecls().getNonStaticFieldDecls();
    }

    public List<FieldDecl> getStaticFieldDecls(){
        return getClassBodyDecls().getStaticFieldDecls();
    }


//    public List<MethodDecl> getMethodDecls(){
//        List <MethodDecl> methodDecls = new ArrayList<MethodDecl>();
//
//        for (String key : containMap.keySet()) {
//            List <ASTNode> nodes = containMap.get(key);
//            for (ASTNode node : nodes) {
//                if (node instanceof MethodDecl && !((MethodDecl)node).isStatic()) {
//                    methodDecls.add((MethodDecl) node);
//                }
//            }
//        }
//        return methodDecls;
//    }


    public List<MethodDecl> getAllNonStaticMethodDecls() {
        if (parentClass != null) {
            List <MethodDecl> parentMethods = parentClass.getAllNonStaticMethodDecls();
            // iterate and replace
            List <MethodDecl> declareMethods = getNonstaticMethodDecls();
            for (MethodDecl selfMethod : declareMethods) {
                int index = 0;
                boolean overwriteOthers = false;
                for (MethodDecl parentMethod : parentMethods) {
                    if (selfMethod.hasSameSig(parentMethod)) {
                        parentMethods.set(index, selfMethod);
                        overwriteOthers = true;
                        break;
                    }
                    index++;
                }
                if (overwriteOthers == false){
                    parentMethods.add(selfMethod);
                }
            }
            return parentMethods;
        }
        return getNonstaticMethodDecls();
    }

    public List<MethodDecl> getNonstaticMethodDecls(){
        return getClassBodyDecls().getNonStaticMethodDecls();
    }

    public boolean isStatic() {
        Modifiers modifiers = (Modifiers) children.get(0);

        for (ASTNode modifier : modifiers.children) {
            if (modifier.value.equals( "static")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(Visitor v){
        List<FieldDecl> staticFields = getStaticFieldDecls();
        for (FieldDecl fieldDecl : staticFields) {
            if (fieldDecl.hasRight()) {
                Expr expr = fieldDecl.getExpr();
                if (expr.ir_node == null) {
                    expr.accept(v);
                }
            }
        }
        v.visit(this);
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }

    }

    @Override
    public Type getType() {
        return tools.getClassType(getName(), this);
    }
}
