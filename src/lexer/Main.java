import java.io.*;
import java.util.*;
import java_cup.runtime.*;


public class Main {

	static public void main(String argv[]) {
		if (argv[1].equals("lexer")){
			try {
				Lexer l = new Lexer(new FileReader(argv[0]));
				while(true) {
					Token tk = Token.class.cast(l.next_token().value);
					System.out.print(sym.terminalNames[tk.type]+" "+tk.lexeme+"\n");
					if (tk.type == sym.EOF){
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}	else {
			try {
				parser p = new parser(new Lexer(new FileReader(argv[0])));
				Symbol result = p.debug_parse();
				System.out.println("success");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}


	}

}