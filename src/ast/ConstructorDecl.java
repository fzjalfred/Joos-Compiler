package ast;

import tir.src.joosc.ir.ast.FuncDecl;
import visitors.IRTranslatorVisitor;
import visitors.TypeCheckVisitor;
import visitors.UnreachableStmtVisitor;
import visitors.Visitor;


import dataflowAnalysis.CFG;

import java.util.List;

public class ConstructorDecl extends ClassBodyDecl implements Referenceable, Callable{
    public ClassDecl whichClass;
    public FuncDecl funcDecl;
    public ConstructorDecl(List<ASTNode> children, String value){
        super(children, value);
        whichClass = null;
    }
    public String getName(){
        return getConstructorDeclarator().getName();
    }

    public ConstructorDeclarator getConstructorDeclarator(){
        assert children.get(1) instanceof ConstructorDeclarator;
        return (ConstructorDeclarator)children.get(1);
    }

    public Modifiers getModifiers(){
        return (Modifiers)children.get(0);
    }

    public ConstructorBody getConstructorBody(){
        assert children.get(2) instanceof ConstructorBody;
        return (ConstructorBody)children.get(2);
    }

    private void acceptMain(Visitor v){
        v.visit(this);
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
    }

    @Override
    public void accept(Visitor v){
        if (v instanceof TypeCheckVisitor){
            TypeCheckVisitor visitor = (TypeCheckVisitor)v;
            visitor.context.entry("Method Parameter List");
            acceptMain(v);
            visitor.context.pop();
        }   else if (v instanceof UnreachableStmtVisitor) {
            UnreachableStmtVisitor uv = (UnreachableStmtVisitor) v;
            acceptMain(v);
            if (uv.currVertex != null && uv.currVertex != uv.currCFG.START)
                uv.currCFG.setEdge(uv.currVertex, uv.currCFG.END);
            for (CFG.Vertex i : uv.ifpaths) {
                uv.currCFG.setEdge(i, uv.currCFG.END);
            }
            uv.ifpaths.clear();

        } else if (v instanceof IRTranslatorVisitor){
            getConstructorDeclarator().constructorDecl = this;
            String name = getName() + "_" + hashCode();
            funcDecl = new FuncDecl(name, 0, null, "THIS_"+getName());
            IRTranslatorVisitor iv = (IRTranslatorVisitor)v;
            iv.currFunc = funcDecl;
            for (ASTNode node: children){
                if (node != null) node.accept(v);
            }
            v.visit(this);
        }   else{
            acceptMain(v);
        }
    }

    @Override
    public Type getType() {
        return null;
    }
}
