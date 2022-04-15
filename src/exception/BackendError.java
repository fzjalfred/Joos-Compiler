package exception;

public class BackendError extends Error {
    public BackendError(String msg){
        super("Backend Error (in file " + currFile +  " ): " + msg);
    }

    public static String currFile = "";
}
