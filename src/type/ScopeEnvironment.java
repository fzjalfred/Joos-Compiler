package type;

import ast.Name;
import ast.*;
import lexer.*;
import utils.tools;

import java.util.HashMap;
import java.util.Map;

public class ScopeEnvironment extends Environment{
    public RootEnvironment root;    // root of all environment

    public Referenceable lookup(Name name){
        Referenceable res = search(name);
        if (res != null){
            return res;
        }
        for (ASTNode node : childScopes.keySet()){
            res = childScopes.get(node).search(name);
            if (res != null) return res;
        }
        return null;
    }

    public Referenceable search(Name name){
        String nameStr = name.getValue();
        if (localDecls.containsKey(nameStr)) {
            return localDecls.get(nameStr);
        }
        return null;    // null for now
    }

    public Referenceable lookup(Token simpleName){
        assert simpleName.type == sym.ID;
        Referenceable res = search(simpleName);
        if (res == null && parent != root){
            return parent.lookup(simpleName);
        }
        return res;
    }

    public Referenceable search(Token simpleName){
        String name = simpleName.value;
        for (String key : localDecls.keySet()){
            String simpleKey = tools.getSimpleName(key);
            if (simpleKey.equals(name)) return localDecls.get(key);
        }
        return null;
    }

    public Map<String,Referenceable> localDecls; // map prefix of Name to a decl
    public Map<ASTNode, ScopeEnvironment> childScopes;
    public String prefix;



    public ScopeEnvironment(Environment parent, RootEnvironment root, String prefix){
        this.parent = parent;
        this.root = root;
        this.localDecls = new HashMap<String, Referenceable>();
        this.childScopes = new HashMap<ASTNode, ScopeEnvironment>();
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "ScopeEnvironment{" + System.lineSeparator() +
                "localDecls=" + localDecls + System.lineSeparator() +
                "subScope=" + childScopes + System.lineSeparator() +
                '}';
    }
}
