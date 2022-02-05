package ast;

import java.util.List;

public class LocalVarDeclStmt extends BlockStmt {
    public LocalVarDeclStmt(List<ASTNode> children, String value){
        super(children, value);
    }
}
