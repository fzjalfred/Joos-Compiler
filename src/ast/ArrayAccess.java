package ast;

import java.util.List;

public class ArrayAccess extends Expr {
    public ArrayAccess(List<ASTNode> children, String value){
        super(children, value);
    }
}