package ast;

import java.util.List;

public class Expr extends ASTNode {
    public Expr(List<ASTNode> children, String value){
        super(children, value);
    }
}
