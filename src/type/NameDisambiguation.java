package type;
import ast.*;
import lexer.*;
import exception.SemanticError;

import java.lang.reflect.Field;
import java.util.*;
import utils.*;

public class NameDisambiguation {
    public Map<ASTNode, List<Referenceable>> parentMap;
    public Map<ASTNode, Map<String, List<ASTNode>>> containMap;

    public void rootEnvironmentDisambiguation(RootEnvironment rootEnv, boolean checkMethodInvoc) throws SemanticError {
        List<CompilationUnit> compilationUnitList = rootEnv.compilationUnits;

        for (CompilationUnit compilationUnit : compilationUnitList) {
            SemanticError.currFile = compilationUnit.fileName;
            traverseNode((ASTNode) compilationUnit, rootEnv, checkMethodInvoc);
        }
    }

    private boolean isSameTypeOrSuperType(String searchName, ASTNode decl) throws SemanticError {
        if (decl instanceof ClassDecl) {
            if (searchName.equals(((ClassDecl)decl).getName())) {
                return true;
            }
        } else {
            if (searchName.equals(((InterfaceDecl)decl).getName())) {
                return true;
            }
        }

        if (parentMap.containsKey(decl)) {
            List<Referenceable> parents = parentMap.get(decl);
            for (Referenceable parent : parents) {
                String parentName = "";
                if (parent instanceof ClassDecl) {
                    parentName = ((ClassDecl)parent).getName();
                } else {
                    parentName = ((InterfaceDecl)parent).getName();
                }
                if (searchName.equals(parentName)){
                    return true;
                }
            }

        } else {
            throw new SemanticError("Cannot find classdecl in the parent map "+decl);
        }
        return false;
    }

    private FieldDecl findField(ASTNode node, String name) {
        List<ASTNode> sameNameNodes;
        if (containMap.containsKey(node)) {
            if (containMap.get(node).containsKey(name)) {
                sameNameNodes = containMap.get(node).get(name);
                for (ASTNode containNode : sameNameNodes) {
                    if (containNode instanceof FieldDecl) {
                        return (FieldDecl) containNode;
                    }
                }
            }
        }
        return null;
    }

    private Type getVarNameType(Name name, RootEnvironment rootEnv) throws SemanticError {
        Type type = null;
        ScopeEnvironment nameScope = rootEnv.ASTNodeToScopes.get(name);
        if (name.isSimpleName()) {
            // simple name
            // construct simple name
            String nameStr = name.getValue();
            Token nameToken = tools.simpleNameConstructor(nameStr);
            // lookup
            Referenceable decl = nameScope.lookup(nameToken); // FIXME:: how to solve overloading method
            if (decl instanceof FieldDecl) {
                if (((FieldDecl)decl).isStatic()) {
                    throw new SemanticError("Calls a static field without naming the class " + ((FieldDecl)decl).getName());
                }
            }
            if (decl == null) {
//                throw new SemanticError("Cannot find decl for simple name " + name.getValue());
                return null;
            } else {
                type = getVarType(decl);
            }
        } else {
            // qualified name
            Referenceable decl = rootEnv.lookup(name);
            List<String> fullName = name.getFullName();
            String nameStr = fullName.get(0);
            if (decl == null && fullName.size() == 2) { // might be in the form: class.field
                Token nameToken = tools.simpleNameConstructor(nameStr);
                decl = nameScope.lookupTypeDecl(nameToken);
                if (decl instanceof ClassDecl || decl instanceof InterfaceDecl ) {
                    decl = findField((ASTNode) decl, fullName.get(1));
                } else {
                    decl = null;
                }
            }
            // check if it is a static decl
            if (decl instanceof FieldDecl) {
                FieldDecl fieldDecl = (FieldDecl) decl;
                if (!fieldDecl.isStatic()) {
                    // check if the field contain in the class
                    if (!isSameTypeOrSuperType(nameStr, nameScope.typeDecl)) {
                        throw new SemanticError("Field is not static " + fieldDecl.getName());
                    }

                }
                type = getVarType(decl);
            } else {
                if (decl == null) {
                    return null;
                }
                System.out.println("Temp Error for debugging purpose: Not Var Decl " + decl.toString());
            }

        }
        return type;
    }

    private void assignNameType(Name name, RootEnvironment rootEnv) throws SemanticError {
        if (name.type != null) {
            return;
        }
        Type type = getVarNameType(name, rootEnv);
        name.type = type;
    }

    private Type getVarType(Referenceable ref) throws SemanticError {
        if (ref == null) {
            return null;
        }
        Type type = null;
        if (ref instanceof Parameter) {
            type = ((Parameter)ref).getType();
        } else if (ref instanceof FieldDecl) {
            type = ((FieldDecl)ref).getType();
        } else if (ref instanceof  LocalVarDecl) {
            type = ((LocalVarDecl) ref).getType();
        }
//        } else {
//            // temp debug exception
///            System.out.println("Cannot identify node type " + ref.toString());
//        }
        return type;
    }

    private boolean compareParam(List<Type> paramTypes, List<Type> argTypes) {
        if (paramTypes == null && argTypes == null) { // both take 0 argument
            return true;
        }
        if (paramTypes == null || argTypes == null) {
            return false;
        }

        if (paramTypes.size() != argTypes.size()) {
            return false;
        }

        int size = paramTypes.size();

        for (int i = 0; i < size; i++) {
            if (!(paramTypes.get(i).equals(argTypes.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private List<MethodDecl> findMethodDecls(ASTNode node, String name) {
        List<ASTNode> sameNameNodes;
        List<MethodDecl> methodDecls = new ArrayList<MethodDecl>();
        if (containMap.containsKey(node)) {
            if (containMap.get(node).containsKey(name)) {
                sameNameNodes = containMap.get(node).get(name);
                for (ASTNode containNode : sameNameNodes) {
                    if (containNode instanceof MethodDecl) {
                        methodDecls.add((MethodDecl)containNode);
                    }
                }
            }
        }
        return methodDecls;
    }

    private void assignMethodInvocationReturnType(Name methodName, MethodInvocation methodInvocation, RootEnvironment rootEnv) throws SemanticError {
        // no need for worrying about overwriting here, as overwriting can only happen when they have the same sig and return type
//        System.out.println("");
//        System.out.println(methodName);
        Referenceable methodDecls = null;
        ScopeEnvironment nameScope = rootEnv.ASTNodeToScopes.get(methodName);
        List<MethodDecl> methods = null;
        String classOrInterfaceName = "";
            // simple Name
        if (methodName.isSimpleName()) {
//            System.out.println("simple");
            // construct simple name
            String nameStr = methodName.getValue();
            Token nameToken = tools.simpleNameConstructor(nameStr);
            // lookup
            methodDecls = nameScope.lookup(nameToken); // FIXME:: how to solve overloading method
//            System.out.println(methodDecls);
        } else {
            // qualified name
            Referenceable decl = rootEnv.lookup(methodName);
            List<String> fullName = methodName.getFullName();
            String nameStr = fullName.get(0);
            classOrInterfaceName = nameStr;
            if (decl == null && fullName.size() == 0) {
                Token nameToken = tools.simpleNameConstructor(nameStr);
                decl = nameScope.lookupTypeDecl(nameToken);
                if (decl instanceof ClassDecl || decl instanceof InterfaceDecl ) { //Fixme
                    methods = findMethodDecls((ASTNode) decl, fullName.get(1));
                }
            }

        }
        ArgumentList argList = methodInvocation.getArgumentList();
        List<Type> targetArgType = null;
        if (argList != null) {
            targetArgType = argList.getArgsType();
        }
        if (methodDecls instanceof MethodList || methods!= null) {
            if (methodDecls instanceof MethodList && methods == null) {
                methods = ((MethodList)methodDecls).methods;
            }
            for (MethodDecl method : methods) {
                List<Type> searchParamType = method.getParamType();
                if (compareParam(searchParamType, targetArgType)) { // FIXME:: What if the arg type cannot be resolved here
                    if (!method.isStatic() && !methodName.isSimpleName()) {
                        if (classOrInterfaceName != "" && !isSameTypeOrSuperType(classOrInterfaceName, nameScope.typeDecl)) {
                            throw new SemanticError("Naming an irrelevant class but the calling method is not static "+ method.getName());
                        }
                    } else if (method.isStatic() && methodName.isSimpleName()) {
                        throw new SemanticError("Calls a static method without naming the class " + method.getName());
                    }
                    methodName.type = method.getReturnType();
                    methodInvocation.type = method.getReturnType();
                    break;
                }
            }
        }
    }

    private void findNodeNameAndType(ASTNode node, RootEnvironment rootEnv) throws SemanticError {
        Name name = null;
        if (node instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation) node;
            if (methodInvocation.hasName()) { // case 1 A()
                name = methodInvocation.getName();
                if (name.type != null) {
                    return;
                }
                assignMethodInvocationReturnType(name, methodInvocation, rootEnv);
            }
            return;
        } else if (node instanceof PostFixExpr) {
            PostFixExpr postFixExpr = (PostFixExpr) node;
            if (postFixExpr.hasName()) {
                name = postFixExpr.getName();
            }

        } else if (node instanceof LHS) {
            LHS lhs = (LHS) node;
            if (lhs.hasName()) {
                name = lhs.getName();
            }
        } else if (node instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) node;
            name = classOrInterfaceType.getName();
            name.type = classOrInterfaceType;
            return;
        } else if (node instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) node;
            if (arrayAccess.hasName()){
                name = arrayAccess.getName();
            }
        } else if (node instanceof CastExpr) {
            CastExpr castExpr = (CastExpr) node;
            if (castExpr.hasName()) {
                name = castExpr.getName();
                if (name.type != null) {
                    return;
                }
                name.type =  castExpr.getType();
            }
            return;
        }

        if (name != null) {
            assignNameType(name, rootEnv);
        }
    }

    private boolean skipNode(ASTNode node) { // temp soln
        if (node == null) {
            return true;
        }
        if (node instanceof InterfaceDecl) {
            return true;
        }
        if (node instanceof Parameter) {
            return true;
        }
        if (node instanceof ClassInstanceCreateExpr) {
            return true;
        }
        if (node instanceof ForInit) {
            return true;
        }
        if (node instanceof FieldDecl) {
            return true;
        }
        return false;
    }

    public void traverseNode(ASTNode node, RootEnvironment rootEnv, boolean checkMethodInvoc) throws SemanticError {
        if (skipNode(node)) {
            return;
        }
        if (checkMethodInvoc) {
            //        System.out.println(node);
            for (ASTNode child : node.children) {
                traverseNode(child, rootEnv, checkMethodInvoc);
            }
            if (node instanceof MethodInvocation) {
                findNodeNameAndType(node, rootEnv);
            }
        } else {
//                    System.out.println(node);
            for (ASTNode child : node.children) {
                traverseNode(child, rootEnv, checkMethodInvoc);
            }
            if (!(node instanceof MethodInvocation)) {
                findNodeNameAndType(node, rootEnv);
            }

        }


    }
}
