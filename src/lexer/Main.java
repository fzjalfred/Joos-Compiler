package lexer;
import java.io.*;
import java.util.*;

import ast.ClassDecl;
import backend.IRTranslator;
import backend.RegistorAllocator;
import backend.asm.*;
import type.*;
import utils.*;
import hierarchy.HierarchyChecking;
import tir.src.joosc.ir.interpret.*;
import tir.src.joosc.ir.ast.*;

public class Main {

	static public DebugID id = DebugID.None;

	static public void writeLabel(PrintWriter w, String l){
		w.println("global " + l);
		w.println(new label(l));
	}
	static public void createAssembly(IRTranslator translator, CompUnit compUnit, int idx) throws FileNotFoundException, UnsupportedEncodingException {
		try {
			List<Code> vtable = compUnit.constructVtable();
			List<Code> itable = compUnit.constructItable();
			String filename = compUnit.name().split(".+?/(?=[^/]+$)")[1] + ".s";
			PrintWriter printWriter = new PrintWriter("output/" + filename, "UTF-8");
			printWriter.println("section .text");
			for (String str : compUnit.externStrs){
				if (!compUnit.definedLabels.contains(str)) {
					printWriter.println("extern " + str);
				}
			}
			printWriter.println("extern __malloc");
			printWriter.println("extern __exception");
			printWriter.println("extern __debexit");
			printWriter.println("extern NATIVEjava.io.OutputStream.nativeWrite");
			if (idx == 0) {
				printWriter.println("global _start");
				printWriter.println("_start:");
				printWriter.println("call test");
				// get return
				printWriter.println("mov ebx, eax");
				printWriter.println("mov eax, 1");
				printWriter.println("int 0x80");
			}

			Tile t = translator.tiling(compUnit);
			System.out.println(t);
//			printWriter.println(t);
			RegistorAllocator registorAllocator = new RegistorAllocator(true, t.codes,compUnit);
			printWriter.println(new Tile(registorAllocator.allocate()));
			printWriter.println("section .data");
			/** String literals */
			for (String s : compUnit.stringLiteralToLabel.keySet()){
				printWriter.println(new label(compUnit.stringLiteralToLabel.get(s)) + " " + new dcc(dcc.ccType.b, new LabelOperand( s )));
			}
			if (compUnit.oriType instanceof ClassDecl) {
				/**Vtable */
				writeLabel(printWriter, tools.getVtable((ClassDecl) compUnit.oriType));
				for (Code code : vtable){
					printWriter.println(code);
				}
				/**Itable */
				writeLabel(printWriter, tools.getItable((ClassDecl) compUnit.oriType));
				for (Code code : itable){
					printWriter.println(code);
				}
			}
			
			printWriter.close();
		} catch (IOException e1) {
			throw e1;
		}

	}

	static public void sim(IRTranslator translator, RootEnvironment env) throws FileNotFoundException, UnsupportedEncodingException {
		int idx = 0;
		for (CompUnit compUnit : translator.ir_comps){
			translator.canonicalize(compUnit);
			createAssembly(translator, compUnit, idx);
			idx++;
			System.out.println(compUnit.functions());
			System.out.println("ext strs has " + compUnit.externStrs);
		}
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
			IRTranslator translator = new IRTranslator(env.compilationUnits, env);
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