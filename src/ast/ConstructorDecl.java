package ast;

import visitors.TypeCheckVisitor;
import visitors.UnreachableStmtVisitor;
import visitors.Visitor;

import java.util.List;

public class ConstructorDecl extends ClassBodyDecl implements Referenceable, Callable{
    public ConstructorDecl(List<ASTNode> children, String value){
        super(children, value);
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
        }   else if (v instanceof UnreachableStmtVisitor){
            UnreachableStmtVisitor uv = (UnreachableStmtVisitor)v;
            acceptMain(v);
            if (uv.currVertex != null) uv.currCFG.setEdge(uv.currVertex, uv.currCFG.END);
        }   else{
            acceptMain(v);
        }
    }

    @Override
    public Type getType() {
        return null;
    }
}
