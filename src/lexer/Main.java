package lexer;
import java.io.*;
import java.util.*;

import backend.IRTranslator;
import backend.RegistorAllocator;
import backend.asm.Tile;
import type.*;
import utils.*;
import hierarchy.HierarchyChecking;
import tir.src.joosc.ir.interpret.*;
import tir.src.joosc.ir.ast.*;

public class Main {

	static public DebugID id = DebugID.None;

	static public void createAssembly(IRTranslator translator, CompUnit compUnit) throws FileNotFoundException, UnsupportedEncodingException {
		try {
			PrintWriter printWriter = new PrintWriter("output/test.s", "UTF-8");
			printWriter.println("global _start");
			printWriter.println("_start:");
			printWriter.println("call test");
			// get return
			printWriter.println("mov ebx, eax");
			printWriter.println("mov eax, 1");
			printWriter.println("int 0x80");
			Tile t = translator.tiling(compUnit);
//			printWriter.println(t);
			RegistorAllocator registorAllocator = new RegistorAllocator(true, t.codes,compUnit);
			printWriter.println(new Tile(registorAllocator.allocate()));
			printWriter.close();
		} catch (IOException e1) {
			throw e1;
		}

	}

	static public void sim(IRTranslator translator, RootEnvironment env) throws FileNotFoundException, UnsupportedEncodingException {
		CompUnit compUnit = new CompUnit("test");
		for (FuncDecl funcDecl : translator.mapping.values()){
			compUnit.appendFunc(funcDecl);
		}
		// IR interpreter demo
		translator.canonicalize(compUnit);
		createAssembly(translator, compUnit);
		System.out.println(compUnit.functions());

		// IR interpreter demo
        /*{
            Simulator sim = new Simulator(compUnit);
            long result = sim.call("test");
            System.out.println("test evaluates to " + result);
        }*/
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