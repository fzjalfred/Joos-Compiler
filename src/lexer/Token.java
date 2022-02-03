package lexer;
import java.io.*;
import java.util.ArrayList;

import ast.ASTNode;

public class Token extends ASTNode {
    public int type;
    public Token(int type, String lexeme) {
        super(new ArrayList<ASTNode>(), lexeme);
        this.type = type;
    }
    @Override
    public String toString() {
        return "Token(type: " + sym.terminalNames[type] + " value: " + value + ")";
    }
}
