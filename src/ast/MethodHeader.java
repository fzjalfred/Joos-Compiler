package ast;

import java.util.List;

public class MethodHeader extends ASTNode {
    public MethodHeader(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        MethodDeclarator md = getMethodDeclarator();
        return md.getName();
    }
    public MethodDeclarator getMethodDeclarator(){
        assert children.get(2) instanceof MethodDeclarator;
        return (MethodDeclarator)children.get(2);
    }

    public Type getType(){
        if(children.get(1) instanceof Type){
            return (Type)children.get(1);
        }
        return null;
    }

}
