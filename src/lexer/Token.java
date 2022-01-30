import java.io.*;

public class Token {
    public int type;
    public String lexeme;
    public Token(int type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }
    public void print() {
        System.out.print(sym.terminalNames[type]+" "+lexeme+"\n");
    }
}
