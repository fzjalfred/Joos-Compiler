package lexer;
import java.io.*;
import java.util.*;

import backend.IRTranslator;
import type.*;
import utils.*;
import hierarchy.HierarchyChecking;

public class Main {

	static public DebugID id = DebugID.None;

	static public void main(String argv[]) {
		try {
			RootEnvironment env = EnvironmentBuilder.buildRoot(argv);
			HierarchyChecking checker = new HierarchyChecking();
			checker.checkRootEnvironment(env);
			NameDisambiguation nameDisamb = new NameDisambiguation();
			nameDisamb.parentMap = checker.parentMap;
			nameDisamb.containMap = checker.containMap;
			nameDisamb.rootEnvironmentDisambiguation(env, false);
			TypeChecker typeChecker = new TypeChecker(env, checker, nameDisamb);
			typeChecker.check();
			IRTranslator translator = new IRTranslator(env.compilationUnits);
			translator.translate();
			System.out.println(translator.mapping);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(42);
		} catch (Error e){
			e.printStackTrace();
			System.exit(42);
		}
	}

}