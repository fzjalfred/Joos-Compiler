package ast;

import java.util.List;

public class FieldAccess extends PrimaryNoArray {
    public FieldAccess(List<ASTNode> children, String value){
        super(children, value);
    }
}