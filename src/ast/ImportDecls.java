package ast;

import java.util.List;

public class ImportDecls extends ASTNode {
    public ImportDecls(List<ASTNode> children, String value){
        super(children, value);
    }
}
