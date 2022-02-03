package lexer;
import java.io.*;
import java.util.*;
import java_cup.runtime.*;


public class Main {

	static public void main(String argv[]) {
		try {
			parser p = new parser(new Lexer(new FileReader(argv[0])));
			Symbol result = p.debug_parse();
			System.out.println(result.value);
			System.out.println("success");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}