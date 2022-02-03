package ast;

import java.util.List;

public class FieldDecl extends ClassMemberDecl {
    public FieldDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
