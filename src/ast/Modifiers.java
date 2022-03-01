package ast;

import lexer.Token;
import visitors.Visitor;

import java.util.*;

public class Modifiers extends ASTNode{
    Set<String> set = null;
    public Modifiers(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }

    public Set<String> getModifiersSet(){
        if (this.set != null) return set;
        Set<String> set = new HashSet<String>();
        for (ASTNode node : children){
            assert node instanceof Token;
            Token modifier = (Token)node;
            set.add(modifier.value);
        }
        this.set = set;
        return this.set;
    }
}
