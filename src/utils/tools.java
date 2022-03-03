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

    public static ClassOrInterfaceType getClassType(String className, TypeDecl classDecl){
        ClassOrInterfaceType classType = new ClassOrInterfaceType(new ArrayList<ASTNode>(){{add(nameConstructor(className));};}, "");
        classType.typeDecl = classDecl;
        return classType;
    }
    public static boolean checkStatic(Modifiers m){
        return m.getModifiersSet().contains("static");
    }


    public static boolean compTypeListEqual(List<Type> list1, List<Type>list2){
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        for (int idx = 0; idx< list1.size(); idx++){
            if (list1.get(idx) != list2.get(idx)) return false;
        }
        return true;
    }

    public static FieldDecl fetchField(List<ASTNode> refers){
        for (ASTNode refer : refers){
            if (refer instanceof FieldDecl){
                return (FieldDecl)refer;
            }
        }
        return null;
    }

    public static MethodDecl fetchMethod(List<ASTNode> refers, List<Type> argTypes){
        for (ASTNode refer : refers){
            if (refer instanceof MethodDecl){
                MethodDecl method = (MethodDecl)refer;
                if (compTypeListEqual(method.getParamType(), argTypes)){
                    return method;
                }
            }
        }
        return null;
    }

    public static boolean containClass(List<Referenceable> refers, TypeDecl typeDecl){
        if (refers == null) return false;
        for (Referenceable refer: refers){
            if (typeDecl == refer) return true;
        }
        return false;
    }

    /** print a message if debug is on*/
    public static void println(Object msg, DebugID id){
        if (id == Main.id){
            System.out.println(msg);
        }
    }
}
