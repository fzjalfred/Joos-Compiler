package ast;

import visitors.Visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.tools;

public class PostFixExpr extends UnaryExprNotPlusMinus{

    public Referenceable refer;
    public Referenceable first_receiver = null;
    public List<FieldDecl> subfields = null;
    public PostFixExpr(List<ASTNode> children, String value){
        super(children, value);
        subfields = new ArrayList<FieldDecl>();
    }

    public Type getType() {
        if (type != null) {
            return type;
        }
        if (children.get(0) instanceof Primary) {
            type = ((Primary) children.get(0)).getType();
            return type;
        } else {
            // FIXME
            type = ((Name)children.get(0)).type;
            return type;
        }
    }

    public boolean hasName() {
        if (children.size() == 0) {
            return false;
        }
        return children.get(0) instanceof Name;
    }

    public Name getName(){
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    static public PostFixExpr get(String name, Referenceable refer){
        PostFixExpr res =  new PostFixExpr(new ArrayList<ASTNode>(Arrays.asList(tools.nameConstructor(name))), null);
        res.refer = refer;
        return res;
    }

    @Override
    public String toString() {
        if (hasName()){
            return getName().getValue();
        }   else {
            return super.toString();
        }
    }
}