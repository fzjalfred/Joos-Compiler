package ast;

import java.util.List;

public class SingleTypeImportDecl extends ImportDecl {
    public SingleTypeImportDecl(List<ASTNode> children, String value){
        super(children, value);
    }

    public Name getName(){
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }
}
