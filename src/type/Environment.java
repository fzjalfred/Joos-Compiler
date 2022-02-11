package type;
import ast.*;
import java.util.Map;
import lexer.*;

public abstract class Environment {
    public Environment parent;
    public abstract Referenceable lookup(Name name);    // loop up the name in all related environments
    public abstract Referenceable search(Name name);    // search for the name in current environment
    public abstract Referenceable lookup(Token simpleName);    // loop up the name in all related environments
    public abstract Referenceable search(Token simpleName);
}
