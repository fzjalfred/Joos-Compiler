package lexer;
import java.io.*;
import java.util.*;

import backend.IRTranslator;
import type.*;
import utils.*;
import hierarchy.HierarchyChecking;
import tir.src.joosc.ir.interpret.*;
import tir.src.joosc.ir.ast.*;

public class Main {

	static public DebugID id = DebugID.None;

	static public void createAssembly(IRTranslator translator, CompUnit compUnit){
		System.out.println("	global _start");
		System.out.println("_start:");
		System.out.println("call test");
		System.out.println(); // get return value
		System.out.println(translator.tiling(compUnit));
	}

	static public void sim(IRTranslator translator, RootEnvironment env){
		CompUnit compUnit = new CompUnit("test");
		for (FuncDecl funcDecl : translator.mapping.values()){
			compUnit.appendFunc(funcDecl);
		}
		// IR interpreter demo
		translator.canonicalize(compUnit);
		createAssembly(translator, compUnit);
		System.out.println(compUnit.functions());
	}

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
			sim(translator, env);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(42);
		} catch (Error e){
			e.printStackTrace();
			System.exit(42);
		}
	}

}