package type;

import ast.*;
import java_cup.runtime.Symbol;
import lexer.*;
import java.io.FileReader;
import java.lang.ref.Reference;
import java.util.*;
import utils.*;

public class RootEnvironment extends Environment{

    private void checkFileName(String thisFileName,String publicFilename) throws Exception{
        String baseName = thisFileName.split(".+?/(?=[^/]+$)")[1].split("\\.(?=[^\\.]+$)")[0];
        // check public file name matches thisFileName or not
        if (!publicFilename.equals(baseName) && publicFilename != ""){
            throw new Exception(  "class: " + publicFilename + " does not match " + baseName);
        }
    }

    public List<ASTNode> uploadFiles(String[] fileNames) throws Exception,Error{
        List<ASTNode> res = new ArrayList<ASTNode>();
        for (String f : fileNames){
            parser p = new parser(new Lexer(new FileReader(f)));
            Symbol result = p.parse();
            checkFileName(f,p.publicFileName);
            res.add((ASTNode)result.value);
        }
        return res;
    }


    public RootEnvironment(){
        parent = null;
        packageScopes = new HashMap<String, ScopeEnvironment>();
        ASTNodeToScopes = new HashMap<ASTNode, ScopeEnvironment>();
    }

    public Map<String, ScopeEnvironment> packageScopes;    //map a prefix of Name to a scope
    public Map<ASTNode, ScopeEnvironment> ASTNodeToScopes;

    /** look up a qualified name under some package str if exists */
    private Referenceable lookupStr(String str, Name qualifiedName){
        if (packageScopes.containsKey(str)){
            Referenceable res =  packageScopes.get(str).rootLookupHelper(qualifiedName);
            if (res != null) return res;
        }
        return null;
    }

    /** look up a qualified name in root*/
    public Referenceable lookup(Name name){
        List<String> names = name.getFullName();
        String nameStr = "";
        Referenceable res = null;
        res = lookupStr("", name); // lookup empty package
        if (res != null) return res;
        for (String s : names){
            nameStr = nameStr + s;
            res = lookupStr(nameStr, name);
            if (res != null) return res;
            nameStr = nameStr + '.';
        }
        return null;
    }

    /** look up a qualified name under some package str if exists */
    private Pair<Referenceable, ScopeEnvironment> lookupStrEnv(String str, Name qualifiedName){
        if (packageScopes.containsKey(str)){
            Pair<Referenceable, ScopeEnvironment> res =  packageScopes.get(str).rootLookupNameAndEnvHelper(qualifiedName);
            if (res != null) return res;
        }
        return new Pair<Referenceable, ScopeEnvironment>(null, null);
    }

    /** look up a qualified name&its enclosing scope in root*/
    public Pair<Referenceable, ScopeEnvironment> lookupNameAndEnv(Name name) {
        List<String> names = name.getFullName();
        String nameStr = "";
        Pair<Referenceable, ScopeEnvironment> res = lookupStrEnv("", name); // lookup empty package
        if (res.first() != null) return res;
        for (String s : names){
            nameStr = nameStr + s;
            res = lookupStrEnv(nameStr,name);
            if (res.first() != null) return res;
            nameStr = nameStr + '.';
        }
        return new Pair<Referenceable, ScopeEnvironment>(null, null);
    }

    public Referenceable search(Name name){
        return null;
    }

    @Override
    public String toString() {
        return "RootEnvironment{" + System.lineSeparator() +
                "packageScopes=" + packageScopes + System.lineSeparator() +
                '}';
    }
}
