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

    public Referenceable lookup(Name name){
        List<String> names = name.getFullName();
        String nameStr = "";
        Referenceable res = null;
        for (String s : names){
            nameStr = nameStr + s;
            if (packageScopes.containsKey(nameStr)){
                res =  packageScopes.get(nameStr).rootLookupHelper(name);
                if (res != null) return res;
            }
            nameStr = nameStr + '.';
        }
        return null;
    }
    public Pair<Referenceable, ScopeEnvironment> lookupNameAndEnv(Name name) {
        List<String> names = name.getFullName();
        String nameStr = "";
        Pair<Referenceable, ScopeEnvironment> res = new Pair<Referenceable, ScopeEnvironment>(null, null);
        for (String s : names){
            nameStr = nameStr + s;
            if (packageScopes.containsKey(nameStr)){
                res =  packageScopes.get(nameStr).rootLookupNameAndEnvHelper(name);
                if (res.first() != null) return res;
            }
            nameStr = nameStr + '.';
        }
        return new Pair<Referenceable, ScopeEnvironment>(null, null);
    }

    public Referenceable search(Name name){
        return null;
    }

    public Referenceable lookup(Token simpleName){
        return null;
    }

    public Referenceable search(Token name) {return null;}
    public ScopeEnvironment lookupEnv(ASTNode node) {return null;};

    protected Referenceable rootLookupHelper(Name name){return null;}
    protected Pair<Referenceable, ScopeEnvironment> rootLookupNameAndEnvHelper(Name name){return new Pair<Referenceable, ScopeEnvironment>(null, null);}

    @Override
    public String toString() {
        return "RootEnvironment{" + System.lineSeparator() +
                "packageScopes=" + packageScopes + System.lineSeparator() +
                '}';
    }
}
