package ast;

import visitors.Visitor;

import java.util.List;
import java.util.ArrayList;

public class ArgumentList extends ASTNode {
    public ArgumentList(List<ASTNode> children, String value){
        super(children, value);
    }

    public List<Name> getNameList() {
        for (ASTNode node: children) {
            if (!(node instanceof PostFixExpr)) {
                return null;
            }
            PostFixExpr postFixExpr = (PostFixExpr) node;
            if (!postFixExpr.hasName()) {
                return null;
            }
        }

        List <Name> nameList = new ArrayList<Name>();
        for (ASTNode node : children) {
            PostFixExpr postFixExpr = (PostFixExpr) node;
            nameList.add(postFixExpr.getName());
        }
        return nameList;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}