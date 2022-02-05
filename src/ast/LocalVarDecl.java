package ast;

import java.util.List;

public class LocalVarDecl extends LocalVarDeclStmt {
    public LocalVarDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
