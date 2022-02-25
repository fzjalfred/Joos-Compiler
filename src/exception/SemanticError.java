package exception;

public class SemanticError extends Error {
    public SemanticError(String msg){
        super("Semantic Error (in file " + currFile +  " ): " + msg);
    }

    public static String currFile = "";
}
