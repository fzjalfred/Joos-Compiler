package type;
import ast.*;
import java.util.Map;
import lexer.*;
import utils.Pair;

public abstract class Environment {
    public Environment parent;
    public abstract Referenceable lookup(Name name);    // loop up the name in all related environments
    public abstract Referenceable search(Name name);    // search for the name in current environment
    public abstract Referenceable lookup(Token simpleName);    // loop up the name in all related environments
    public abstract Referenceable search(Token simpleName);
    protected abstract Referenceable rootLookupHelper(Name name);
    public abstract Pair<Referenceable, ScopeEnvironment> lookupNameAndEnv(Name name);
    protected abstract Pair<Referenceable, ScopeEnvironment> rootLookupNameAndEnvHelper(Name name);
    public abstract ScopeEnvironment lookupEnv(ASTNode node);
}
