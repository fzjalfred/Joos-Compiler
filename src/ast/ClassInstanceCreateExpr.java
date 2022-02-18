package ast;

import java.util.List;

public class ClassInstanceCreateExpr extends PrimaryNoArray {
    public ClassInstanceCreateExpr(List<ASTNode> children, String value){
        super(children, value);
    }

    public Type getType(){
        assert (children.get(0) instanceof Type);
        return (Type)children.get(0);
    }
}