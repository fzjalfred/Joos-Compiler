package lexer;
import java.io.*;
import java.util.*;

import ast.Name;
import java_cup.runtime.*;
import type.*;
import utils.*;

public class Main {

	static public void main(String argv[]) {
		try {
			RootEnvironment env = EnvironmentBuilder.buildRoot(argv);
			System.out.println(env);
			Name name1 = tools.nameConstructor("B.class1");
			System.out.println(env.lookup(name1));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(42);
		} catch (Error e){
			e.printStackTrace();
			System.exit(42);
		}
	}

}