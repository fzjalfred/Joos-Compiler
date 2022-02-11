package exception;

public class SemanticError extends Error {
    public SemanticError(String msg){
        super("Semantic Error: " + msg);
    }
}
