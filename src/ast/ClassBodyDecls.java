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

    public List<MethodDecl> getMethodDecls(){
        List<MethodDecl> decls = new ArrayList<MethodDecl>();
        if (children.size() == 1 && children.get(0) instanceof MethodDecl){
            decls.add((MethodDecl) children.get(0));
        }
        if (children.size() > 1){
            decls.addAll(((ClassBodyDecls)children.get(0)).getMethodDecls());
            if (children.get(1) instanceof MethodDecl) {
                decls.add((MethodDecl) children.get(1));
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
