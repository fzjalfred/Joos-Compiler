package ast;

import visitors.TypeCheckVisitor;
import visitors.Visitor;

import java.util.List;

public class LocalVarDecl extends LocalVarDeclStmt implements Referenceable{
    public LocalVarDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public List<String> getName(){
        return getVarDeclarators().getName();
    }
    public VarDeclarators getVarDeclarators(){
        assert children.get(1) instanceof VarDeclarators;
        return (VarDeclarators)children.get(1);
    }

    public Type getType(){
        assert children.get(0) instanceof Type;
        return (Type)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        v.visit(this);
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        if (v instanceof TypeCheckVisitor){
            ((TypeCheckVisitor)v).localVarDecl = null;
        }
    }
}
