package ast;

import visitors.Visitor;

import java.util.List;

public class ImportDecls extends ASTNode {
    public ImportDecls(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
