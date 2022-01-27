import java.io.*;
import java.util.*;

interface Expr { Object run(HashMap<String, Object> hm); }
interface Condition { boolean test(Expr e1, Expr e2, HashMap<String, Object> hm); }
interface Operator { int count(Expr e1, Expr e2, HashMap<String, Object> hm); }

interface SimpleInstruction { void run(HashMap<String,Object> hm); }

interface WhileInstructionI extends SimpleInstruction {void run(HashMap<String, Object> hm); }
interface IfInstructionI extends SimpleInstruction {void run(HashMap<String, Object> hm); }
   
public class Main {

	static public void main(String argv[]) {
		try {
			Lexer p = new Lexer(new FileReader(argv[0]));
			while(true) {
				Token tk = Token.class.cast(p.next_token().value);
				System.out.print(sym.terminalNames[tk.type]+" "+tk.lexeme+"\n");
				if (tk.type == sym.EOF){
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}