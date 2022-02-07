package lexer;
import java.io.*;
import java.util.*;
import java_cup.runtime.*;


public class Main {

	static private void checkFileName(String thisFileName,String publicFilename, List<String> classList) throws Exception{
		String baseName = thisFileName.split(".+?/(?=[^/]+$)")[1].split("\\.(?=[^\\.]+$)")[0];
		// check public file name matches thisFileName or not
		if (!publicFilename.equals(baseName) && publicFilename != ""){
			throw new Exception(  "class: " + publicFilename + " does not match " + baseName);
		}
		for (String o : classList){
			if (baseName.equals(o)) {
				return;
			}
		}
		throw new Exception(  "no class name match " + baseName);
	}

	static public void main(String argv[]) {
		try {
			String fileName = argv[0];
			parser p = new parser(new Lexer(new FileReader(fileName)));
			Symbol result = p.parse();
			checkFileName(fileName,p.publicFileName, p.classList);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(42);
		}
	}

}