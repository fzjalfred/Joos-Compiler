import java.io.*;
import java.util.*;
 
public class Main {

	static public void main(String argv[]) {
		try {
			Lexer p = new Lexer(new FileReader(argv[0]));
			while(true) {
				Token tk = Token.class.cast(p.next_token().value);
				tk.print();
				if (tk.type == sym.EOF){
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}