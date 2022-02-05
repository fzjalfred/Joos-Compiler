package ast;

import java.util.List;

public class Primary extends PostFixExpr {
    public Primary(List<ASTNode> children, String value){
        super(children, value);
    }
}