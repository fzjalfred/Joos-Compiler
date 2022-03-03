package ast;
import visitors.Visitor;

import java.util.List;

abstract public class ASTNode {
    public boolean traversed = false;
    public List<ASTNode> children;
    public String value;

    private String childrenToString(){
        String res = "List(";
        for (ASTNode node : children){
            res += node + " ";
        }
        res += ")";
        return  res;
    }
    @Override
    public String toString() {
        return this.getClass().getName();
    }

//     @Override
//     public String toString() {
//         return this.getClass().getName();
//     }

    public ASTNode(List<ASTNode> children, String value){
        this.children = children;
        this.value = value;
    }

    public boolean isLeaf(){
        return this.children.isEmpty();
    }

    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}

