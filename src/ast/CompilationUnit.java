package ast;

import java.util.List;

public class CompilationUnit extends ASTNode{
    public CompilationUnit(List<ASTNode> children, String value){
        super(children, value);
    }
}
