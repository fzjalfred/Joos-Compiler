package ast;

import java.util.List;

public class Literal extends PrimaryNoArray {
    public Literal(List<ASTNode> children, String value){
        super(children, value);
    }
}