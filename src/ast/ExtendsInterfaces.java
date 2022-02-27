package ast;

import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

public class ExtendsInterfaces extends ASTNode {
    public ExtendsInterfaces(List<ASTNode> children, String value){
        super(children, value);
    }
    public List<ClassOrInterfaceType> getInterfaceTypeList(){
        List<ClassOrInterfaceType> res = new ArrayList<ClassOrInterfaceType>();
        for (ASTNode node : children){
            assert node instanceof ClassOrInterfaceType;
            res.add((ClassOrInterfaceType)node);
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
