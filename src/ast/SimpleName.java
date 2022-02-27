package ast;
import lexer.*;
import visitors.Visitor;

import java.util.List;

public class SimpleName extends Token{
    public SimpleName(int type, String lexeme){
        super(sym.ID, lexeme);
    }

    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
