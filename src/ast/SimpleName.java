package ast;
import lexer.*;

import java.util.List;

public class SimpleName extends Token{
    public SimpleName(int type, String lexeme){
        super(sym.ID, lexeme);
    }
}
