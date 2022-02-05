package ast;

import java.util.List;

public class ArrayCreationExpr extends Primary {
    public ArrayCreationExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}