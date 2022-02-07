package lexer;
import java.io.*;
import java.util.*;
import java_cup.runtime.*;


public class Main {

	static private void checkFileName(String thisFileName,String publicFilename) throws Exception{
		String baseName = thisFileName.split(".+?/(?=[^/]+$)")[1].split("\\.(?=[^\\.]+$)")[0];
		// check public file name matches thisFileName or not
		if (!publicFilename.equals(baseName) && publicFilename != ""){
			throw new Exception(  "class: " + publicFilename + " does not match " + baseName);
		}
	}

	static public void main(String argv[]) {
		try {
			String fileName = argv[0];
			parser p = new parser(new Lexer(new FileReader(fileName)));
			Symbol result = p.parse();
			checkFileName(fileName,p.publicFileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(42);
		} catch (Error e){
			e.printStackTrace();
			System.exit(42);
		}
	}

}