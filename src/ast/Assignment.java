package ast;

import java.util.List;

public class Assignment extends AssignmentExpr {
    public Assignment(List<ASTNode> children, String value){
        super(children, value);
    }
}
