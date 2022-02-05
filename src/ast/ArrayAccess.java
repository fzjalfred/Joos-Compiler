package ast;

import java.util.List;

public class ArrayAccess extends PrimaryNoArray {
    public ArrayAccess(List<ASTNode> children, String value){
        super(children, value);
    }
}