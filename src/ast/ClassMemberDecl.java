package ast;

import java.util.List;

public class ClassMemberDecl extends ClassBodyDecl implements Referenceable{
    public ClassMemberDecl(List<ASTNode> children, String value){
        super(children, value);
    }

}
