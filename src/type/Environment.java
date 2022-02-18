package type;
import ast.*;
import java.util.Map;
import lexer.*;
import utils.Pair;

public abstract class Environment {
    public Environment parent;
    public abstract Referenceable lookup(Name name);    // loop up the name in all related environments
    public abstract Referenceable search(Name name);    // search for the name in current environment
    public  Referenceable lookup(Token simpleName){return null;}    // loop up the name in all related environments
    public  Referenceable search(Token simpleName){return null;}
    protected  Referenceable rootLookupHelper(Name name){return null;}
    public  Pair<Referenceable, ScopeEnvironment> lookupNameAndEnv(Name name){return new Pair<Referenceable, ScopeEnvironment>(null,null);}
    protected  Pair<Referenceable, ScopeEnvironment> rootLookupNameAndEnvHelper(Name name){return new Pair<Referenceable, ScopeEnvironment>(null,null);}
    public Pair<Referenceable, ScopeEnvironment> lookupNameAndEnv(Token simpleName){return new Pair<Referenceable, ScopeEnvironment>(null,null);}

    public Referenceable lookupEnclosingAndSingleImport(Token simpleName){return null;}
    public Referenceable lookupEnclosingPackage(Token simpleName){return null;}
    public Referenceable lookupImportOnDemand(Token simpleName){return null;}
    public Referenceable lookupTypeDecl(Token simpleName){return null;}

    public Pair<Referenceable, ScopeEnvironment> lookupEnclosingAndSingleImportAndEnv(Token simpleName){return null;}
    public Pair<Referenceable, ScopeEnvironment> lookupEnclosingPackageAndEnv(Token simpleName){return null;}
    public Pair<Referenceable, ScopeEnvironment> lookupImportOnDemandAndEnv(Token simpleName){return null;}

    String prefix = "";
}
