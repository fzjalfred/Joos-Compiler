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
        if (localDecls.containsKey(nameStr) && !(localDecls.get(nameStr) instanceof CompilationUnit)) {
            return localDecls.get(nameStr);
        }
        return null;
    }

    /** lookup should first lookup 1.enclosing class/interface
     *                             2.single-type import
     *                             3.names declared in enclosing package
     *                             4.import on demand                   */
    public Referenceable lookupEnclosingAndSingleImport(Token simpleName){
        assert simpleName.type == sym.ID;
        Referenceable res = search(simpleName);
        if (res == null && parent != root){
            return parent.lookupEnclosingAndSingleImport(simpleName);
        }
        return res;
    }

    public Pair<Referenceable, ScopeEnvironment> lookupEnclosingAndSingleImportAndEnv(Token simpleName){
        assert simpleName.type == sym.ID;
        Referenceable res = search(simpleName);
        if (res == null && parent != root){
            return parent.lookupEnclosingAndSingleImportAndEnv(simpleName);
        }
        return new Pair<Referenceable, ScopeEnvironment>(res, this);
    }

    public Referenceable lookupEnclosingPackage(Token simpleName){
        assert simpleName.type == sym.ID;
        if (!isCompliationUnit()){
            return parent.lookupEnclosingPackage(simpleName);
        }
        for (ASTNode node : childScopes.keySet()){
            if (node instanceof PackageDecl){
                return childScopes.get(node).search(simpleName);
            }
        }
        return null; // should not come here
    }

    public Pair<Referenceable, ScopeEnvironment> lookupEnclosingPackageAndEnv(Token simpleName){
        assert simpleName.type == sym.ID;
        if (!isCompliationUnit()){
            return parent.lookupEnclosingPackageAndEnv(simpleName);
        }
        for (ASTNode node : childScopes.keySet()){
            if (node instanceof PackageDecl){
                return new Pair<Referenceable, ScopeEnvironment>(childScopes.get(node).search(simpleName), childScopes.get(node));
            }
        }
        return new Pair<Referenceable, ScopeEnvironment>(null, null); // should not come here
    }

    public Referenceable lookupImportOnDemand(Token simpleName){
        assert simpleName.type == sym.ID;
        if (!isCompliationUnit()){
            return parent.lookupImportOnDemand(simpleName);
        }
        for (ASTNode node : childScopes.keySet()){
            if (node instanceof TypeImportOndemandDecl){
                if(childScopes.get(node).search(simpleName) != null) return childScopes.get(node).search(simpleName);
            }
        }
        return null; // should not come here
    }

    public Pair<Referenceable, ScopeEnvironment> lookupImportOnDemandAndEnv(Token simpleName){
        assert simpleName.type == sym.ID;
        if (!isCompliationUnit()){
            return parent.lookupImportOnDemandAndEnv(simpleName);
        }
        for (ASTNode node : childScopes.keySet()){
            if (node instanceof TypeImportOndemandDecl){
                Pair<Referenceable, ScopeEnvironment> res = new Pair<Referenceable, ScopeEnvironment> (childScopes.get(node).search(simpleName), childScopes.get(node));
                if (res.first != null) return res;
            }
        }
        return new Pair<Referenceable, ScopeEnvironment>(null, null); // should not come here
    }

    public Referenceable lookup(Token simpleName){
        Referenceable res = lookupEnclosingAndSingleImport(simpleName);
        if (res == null) {
            res =lookupEnclosingPackage(simpleName);
            if (res == null){
                res = lookupImportOnDemand(simpleName);

            }
        }
        return res;
    }

    public Pair<Referenceable, ScopeEnvironment> lookupNameAndEnv(Token simpleName) {
        Pair<Referenceable, ScopeEnvironment> result = lookupEnclosingAndSingleImportAndEnv(simpleName);
        if (result == null || result.first == null) {
            result = lookupEnclosingPackageAndEnv(simpleName);
            if (result == null ||result.first == null) {
                result = lookupImportOnDemandAndEnv(simpleName);
            }
        }
        return result;
    }

    public Referenceable search(Token simpleName){
        String name = simpleName.value;
        for (String key : localDecls.keySet()){
            String simpleKey = tools.getSimpleName(key);
            if (simpleKey.equals(name)) return localDecls.get(key);
        }
        return null;
    }


    /** lookup a simpleName which is a typeDecl */
    public Referenceable lookupTypeDecl(Token simpleName){
        assert simpleName.type == sym.ID;
        Referenceable res = searchTypeDecl(simpleName);
        if (res == null && parent.parent!= root){
            res = parent.lookupTypeDecl(simpleName);
        }
        if (res == null){
            res = lookupEnclosingPackage(simpleName);
            if (res == null){
                res = lookupImportOnDemand(simpleName);

            }
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
        if (isLocalDecl) return null;
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
        if (isLocalDecl) return null;
        if (prefix.equals("")) return new Pair<Referenceable, ScopeEnvironment> (null, null);
        Referenceable res = search(name);
        if (res != null){
            return new Pair<Referenceable, ScopeEnvironment> (res, this);
        }
        for (ASTNode node : childScopes.keySet()){
            Pair<Referenceable, ScopeEnvironment> result = childScopes.get(node).rootLookupNameAndEnvHelper(name);
            if (result != null && result.first() != null) return result;
        }
        return new Pair<Referenceable, ScopeEnvironment> (null, null);
    }

    public Map<String,Referenceable> localDecls; // map prefix of Name to a decl
    public Map<ASTNode, ScopeEnvironment> childScopes;
    public Set<String> simpleNameSet; // set of all simple names; used for checking dup
    public String prefix;
    public boolean isLocalDecl;

    public ScopeEnvironment(Environment parent, RootEnvironment root, String prefix){
        this.parent = parent;
        this.root = root;
        this.localDecls = new HashMap<String, Referenceable>();
        this.childScopes = new HashMap<ASTNode, ScopeEnvironment>();
        this.simpleNameSet = new HashSet<String>();
        this.prefix = prefix;
        this.isLocalDecl = false;
    }

    @Override
    public String toString() {
        return "ScopeEnvironment{" + System.lineSeparator() +
                "localDecls=" + localDecls + System.lineSeparator() +
                "subScope=" + childScopes + System.lineSeparator() +
                "prefix=" + prefix +
                '}';
    }

    boolean isCompliationUnit(){
        if (parent != null){
            return (parent.parent == root);
        }
        return false;
    }
}
