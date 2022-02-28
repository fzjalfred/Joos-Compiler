package utils;
import ast.*;
import java.util.*;
import lexer.*;
public class tools {

    public static Name nameConstructor(String qualifiedName){
        String[] names = qualifiedName.split("\\.");
        List<ASTNode> tokens = new ArrayList<ASTNode>();
        for (String s : names){
            tokens.add(new Token(sym.ID, s));
        }
        return new Name(tokens, "");
    }

    public static Token simpleNameConstructor(String simpleName){
        return new Token(sym.ID, simpleName);
    }

    public static String getSimpleName(String qualifiedName){
        String[] names = qualifiedName.split("\\.");
        return names[names.length-1];
    }

    public static String getSimpleName(Name qualifiedName){
        String qualifiedNameStr = qualifiedName.getValue();
        return getSimpleName(qualifiedNameStr);
    }

    public static List<ASTNode> empty(){
        return new ArrayList<ASTNode>();
    }

    public static List<ASTNode> list(ASTNode node){
        return new ArrayList<ASTNode>(){{add(node);}};
    }

}
