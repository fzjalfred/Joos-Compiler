package ast;

import java.util.List;

public class ImportDecl extends ASTNode{
    public ImportDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
