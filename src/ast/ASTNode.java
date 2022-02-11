package ast;
import java.util.List;

abstract public class ASTNode {
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
   // @Override
   // public String toString() {
   //     return this.getClass().getName() + "(val: " + value + "; children: "+ childrenToString() + ")";
   // }

     @Override
     public String toString() {
         return this.getClass().getName();
     }

    public ASTNode(List<ASTNode> children, String value){
        this.children = children;
        this.value = value;
    }

    public boolean isLeaf(){
        return this.children.isEmpty();
    }


}

