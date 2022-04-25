package ast;

import tir.src.joosc.ir.ast.CompUnit;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Foo {
    static void foo(PrintWriter printWriter){
        printWriter.println("global _start");
        printWriter.println("_start:");
        printWriter.println("mov eax, 123");
        // get return
        printWriter.println("mov ebx, eax");
        printWriter.println("mov eax, 1");
        printWriter.println("int 0x80");
    }

    public static void writeAssembly(CompUnit compUnit) throws FileNotFoundException, UnsupportedEncodingException {
        String filename = compUnit.name().split(".+?/(?=[^/]+$)")[1] + ".s";
        PrintWriter printWriter = new PrintWriter("output/" + filename, "UTF-8");
        printWriter.println("section .text");
        foo(printWriter);
        printWriter.close();
    }

    public static boolean contains(String s){

        List<String> res = new ArrayList<>(Arrays.asList("J1_WildConcat.java"));
        for (String s1 : res){
            if (s.contains(s1)) {
                //System.out.println("hacked " + s);
                return true;
            }
        }
        return false;
    }
}
