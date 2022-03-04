package utils;
import ast.*;
import java.util.*;
import lexer.*;
import type.RootEnvironment;
import type.ScopeEnvironment;

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


    /** create classorinterface type based on nameStr and decl */
    public static ClassOrInterfaceType getClassType(String className, TypeDecl classDecl){
        ClassOrInterfaceType classType = new ClassOrInterfaceType(new ArrayList<ASTNode>(){{add(nameConstructor(className));};}, "");
        classType.typeDecl = classDecl;
        return classType;
    }

    /** create classorinterface type based on name and decl */
    public static ClassOrInterfaceType getClassType(Name className, TypeDecl classDecl){
        return getClassType(className.getValue(), classDecl);
    }

    public static boolean checkStatic(Modifiers m){
        return m.getModifiersSet().contains("static");
    }


    public static boolean compTypeListEqual(List<Type> list1, List<Type>list2){
        tools.println("matching " + list1 + " to " + list2, DebugID.zhenyan );
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        for (int idx = 0; idx< list1.size(); idx++){
            if (!list1.get(idx).equals(list2.get(idx))) return false;
        }
        return true;
    }

    public static ConstructorDecl fetchConstructor(List<Referenceable> refers, List<Type> types){
        for (Referenceable refer: refers){
            if (refer instanceof ConstructorList){
                return ((ConstructorList)refer).match(types);
            }
        }
        return null;
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


    public static String get_class_qualifed_name(ASTNode class_decl, RootEnvironment env) {
        ScopeEnvironment underenv = env.ASTNodeToScopes.get(class_decl);
        return underenv.prefix;
    }
    public static String get_class_qualifed_name(ClassOrInterfaceType class_decl, RootEnvironment env) {
        assert class_decl.typeDecl != null;
        return get_class_qualifed_name(class_decl.typeDecl, env);
    }

    public static NumericType intType(){
        return new NumericType(empty(), "int");
    }
}
