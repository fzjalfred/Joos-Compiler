package ast;

import visitors.Visitor;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class ClassBodyDecls extends ASTNode{
    public ClassBodyDecls(List<ASTNode> children, String value){
        super(children, value);
    }

    public List<FieldDecl> getNonStaticFieldDecls(){
        List<FieldDecl> decls = new ArrayList<FieldDecl>();
        if (children.size() == 0) {
            return decls;
        }

        for (ASTNode node : children) {
            if (node instanceof FieldDecl && !(((FieldDecl)node).isStatic())) {
                decls.add((FieldDecl) node);
            }
        }
        return decls;
    }

    public List<FieldDecl> getStaticFieldDecls(){
        List<FieldDecl> decls = new ArrayList<FieldDecl>();
        if (children.size() == 0) {
            return decls;
        }
        for (ASTNode node : children) {
            if (node instanceof FieldDecl && ((FieldDecl)node).isStatic()){
                decls.add((FieldDecl) node);
            }
        }
        return decls;
    }

    public List<MethodDecl> getNonStaticMethodDecls(){
        List<MethodDecl> decls = new ArrayList<MethodDecl>();
        if (children.size() == 0) {
            return decls;
        }
        for (ASTNode node : children) {
            if (node instanceof MethodDecl && !(((MethodDecl)node).isStatic())) {
                decls.add((MethodDecl) node);
            }
        }
        return decls;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
