package ast;

import visitors.Visitor;

import java.util.List;

public class ClassDecl extends TypeDecl{
    // children( modifiers ID Super Interface ClassBody)
    public ClassDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(1).value;
    }
    public ASTNode getModifiers() { return children.get(0);}
    public ClassBodyDecls getClassBodyDecls(){
        if (children.get(4) == null) return null;
        assert children.get(4).children.get(0) instanceof ClassBodyDecls;
        return (ClassBodyDecls)children.get(4).children.get(0);
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
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

}
