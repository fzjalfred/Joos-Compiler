package ast;

import java.util.List;

public class ClassDecl extends TypeDecl{
    // children( modifiers ID Super Interface ClassBody)
    public ClassDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
