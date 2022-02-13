package type;

import ast.Name;
import ast.*;
import lexer.*;
import utils.*;

import java.util.*;

public class ScopeEnvironment extends Environment{
    public RootEnvironment root;    // root of all environment

    public Referenceable lookup(Name name){
        return root.lookup(name);
    }

    public Pair<Referenceable, ScopeEnvironment> lookupNameAndEnv(Name name) {
        return root.lookupNameAndEnv(name);
    }

    public Referenceable search(Name name){
        String nameStr = name.getValue();
        if (localDecls.containsKey(nameStr)) {
            return localDecls.get(nameStr);
        }
        return null;
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

    public Referenceable lookupTypeDecl(Token simpleName){
        assert simpleName.type == sym.ID;
        Referenceable res = search(simpleName);
        if (res == null && parent != root){
            return parent.lookup(simpleName);
        }
        return res;
    }

    public Referenceable searchTypeDecl(Token simpleName){
        String name = simpleName.value;
        for (String key : localDecls.keySet()){
            String simpleKey = tools.getSimpleName(key);
            Referenceable res = localDecls.get(key);
            if (simpleKey.equals(name) && res instanceof TypeDecl) return res;
        }
        return null;
    }

    protected Referenceable rootLookupHelper(Name name){
        if (prefix.equals("")) return null;
        Referenceable res = search(name);
        if (res != null){
            return res;
        }
        for (ASTNode node : childScopes.keySet()){
            res = childScopes.get(node).rootLookupHelper(name);
            if (res != null) return res;
        }
        return null;
    }
    protected Pair<Referenceable, ScopeEnvironment> rootLookupNameAndEnvHelper(Name name) {
        if (prefix.equals("")) return new Pair<Referenceable, ScopeEnvironment> (null, null);
        Referenceable res = search(name);
        if (res != null){
            return new Pair<Referenceable, ScopeEnvironment> (res, this);
        }
        for (ASTNode node : childScopes.keySet()){
            Pair<Referenceable, ScopeEnvironment> result = childScopes.get(node).rootLookupNameAndEnvHelper(name);
            if (result.first() != null) return result;
        }
        return new Pair<Referenceable, ScopeEnvironment> (null, null);
    }

    public Map<String,Referenceable> localDecls; // map prefix of Name to a decl
    public Map<ASTNode, ScopeEnvironment> childScopes;
    public Set<String> simpleNameSet; // set of all simple names; used for checking dup
    public String prefix;

    public ScopeEnvironment lookupEnv(ASTNode node){
        if (node == null) {
            return null;
        }
        if (childScopes.containsKey(node)) {
            return this;
        }
        if (parent != null) {
            return parent.lookupEnv(node);
        }
        return null;
    }


    public ScopeEnvironment(Environment parent, RootEnvironment root, String prefix){
        this.parent = parent;
        this.root = root;
        this.localDecls = new HashMap<String, Referenceable>();
        this.childScopes = new HashMap<ASTNode, ScopeEnvironment>();
        this.simpleNameSet = new HashSet<String>();
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
