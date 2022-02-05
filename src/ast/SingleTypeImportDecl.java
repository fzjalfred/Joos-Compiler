package ast;

import java.util.List;

public class SingleTypeImportDecl extends ImportDecl {
    public SingleTypeImportDecl(List<ASTNode> children, String value){
        super(children, value);
    }
}
