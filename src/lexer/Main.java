package lexer;
import java.io.*;
import java.util.*;

import ast.ClassDecl;
import backend.IRTranslator;
import backend.RegistorAllocator;
import backend.asm.*;
import backend.asm.Const;
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

	static public void writeString(String s, label strLabel, PrintWriter writer, RootEnvironment env){

		String realStr = s.substring(1, s.length()-1);
		label charListLabel = new label(strLabel.name + "_CHARS_" + strLabel.hashCode());
		ClassDecl StringDecl = (ClassDecl)env.lookup(tools.nameConstructor("java.lang.String"));
		ClassDecl ObjectDecl = (ClassDecl)env.lookup(tools.nameConstructor("java.lang.Object"));
		/** label: vtable  */
		writer.println(strLabel);
		writer.println(new dcc(dcc.ccType.w, new LabelOperand(tools.getVtable(StringDecl, env))));
		writer.println(new dcc(dcc.ccType.w, new LabelOperand( charListLabel.name )));
		writer.println(new dcc(dcc.ccType.w, new Const(realStr.length())));
		writer.println(new dcc(dcc.ccType.w, new LabelOperand(tools.getVtable(ObjectDecl, env))));
		writer.println(charListLabel);
		for (int i = 0; i < realStr.length(); i++){
			writer.println(new dcc(dcc.ccType.w, new Const(realStr.charAt(i))));
		}
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
				writeString(s, new label(compUnit.stringLiteralToLabel.get(s)), printWriter, compUnit.env);
			}
			if (compUnit.oriType instanceof ClassDecl) {
				/**Vtable */
				writeLabel(printWriter, tools.getVtable((ClassDecl) compUnit.oriType, compUnit.env));
				for (Code code : vtable){
					printWriter.println(code);
				}
				/**Itable */
				writeLabel(printWriter, tools.getItable((ClassDecl) compUnit.oriType, compUnit.env));
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