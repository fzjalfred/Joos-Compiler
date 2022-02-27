package ast;

import visitors.Visitor;

import java.util.*;

public class VarDeclarators extends ASTNode{
    public VarDeclarators(List<ASTNode> children, String value){
        super(children, value);
    }
    List<String> getName(){
        List<String> res = new ArrayList<String>();
        for (ASTNode node : children){
            assert node instanceof VarDeclarator;
            VarDeclarator vd = (VarDeclarator)node;
            res.add(vd.getName());
        }
        return res;
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
