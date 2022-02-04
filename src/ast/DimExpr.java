package ast;

import java.util.List;

public class DimExpr extends ASTNode {
    public DimExpr(List<ASTNode> children, String value){
        super(children, value);
    }
}