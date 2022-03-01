package lexer;
import java.io.*;
import java.util.*;

import ast.CompilationUnit;
import ast.Name;
import java_cup.runtime.*;
import type.*;
import visitors.*;
import utils.*;
import hierarchy.HierarchyChecking;

public class Main {

	static public void main(String argv[]) {
		try {
			RootEnvironment env = EnvironmentBuilder.buildRoot(argv);
			HierarchyChecking checker = new HierarchyChecking();
			checker.checkRootEnvironment(env);
			NameDisambiguation nameDisamb = new NameDisambiguation();
			nameDisamb.rootEnvironmentDisambiguation(env);
			TypeChecker typeChecker = new TypeChecker(env);
			typeChecker.check();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(42);
		} catch (Error e){
			e.printStackTrace();
			System.exit(42);
		}
	}

}