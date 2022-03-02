package type;
import ast.*;
import lexer.*;
import exception.SemanticError;

import java.lang.reflect.Field;
import java.util.*;
import utils.*;

public class NameDisambiguation {

    public void rootEnvironmentDisambiguation(RootEnvironment rootEnv) throws SemanticError, Exception {
        List<CompilationUnit> compilationUnitList = rootEnv.compilationUnits;

        for (CompilationUnit compilationUnit : compilationUnitList) {
            SemanticError.currFile = compilationUnit.fileName;
            traverseNode((ASTNode) compilationUnit, rootEnv);
        }
    }

    private FieldDecl findField(ASTNode node, List<String> name) {
//        System.out.println("looking for name: "+name);
        ASTNode decls = null;
        if (node instanceof ClassDecl) {
            decls = ((ClassDecl)node).getClassBodyDecls();
        } else {
            if (!((InterfaceDecl)node).hasMemberDecls()) {
                return null;
            }
            decls = ((InterfaceDecl)node).getInterfaceMemberDecls();
        }
        for (ASTNode decl : decls.children) {
            if (decl instanceof FieldDecl) {
                List<String> fieldName = ((FieldDecl)decl).getName();
//                System.out.println("searched name: " +fieldName);
                if (fieldName.size() != name.size()) {
                    continue;
                }
                for (int i = 0; i < fieldName.size(); i++) {
                    if (!fieldName.get(i).equals(name.get(i))) {
                        continue;
                    }
                }
                return (FieldDecl) decl;
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
            if (decl == null) {
//                throw new SemanticError("Cannot find decl for simple name " + name.getValue());
                return null;
            } else {
                type = getVarType(decl);
            }
        } else {
            // qualified name
            Referenceable decl = rootEnv.lookup(name);
            if (decl == null) { // might be in the form: *.*.class.field
                List<String> fullName = name.getFullName();
                String nameStr = fullName.subList(fullName.size()-2, fullName.size()-1).get(0);
                Token nameToken = tools.simpleNameConstructor(nameStr);
                decl = nameScope.lookup(nameToken);
                if (decl instanceof ClassDecl || decl instanceof InterfaceDecl ) {
//                    System.out.println(fullName.subList(fullName.size()-1, fullName.size()));
                    decl = findField((ASTNode) decl, fullName.subList(1, fullName.size()));
                } else {
                    decl = null;
                }
            }
            // check if it is a static decl
            if (decl instanceof FieldDecl) {
                FieldDecl fieldDecl = (FieldDecl) decl;
                if (!fieldDecl.isStatic()) {
                    throw new SemanticError("Field is not static " + fieldDecl.getName());
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

    private List<Type> getArgumentType(ArgumentList argumentList, RootEnvironment rootEnv) throws SemanticError {
        return argumentList.getArgsType();
//        List<Name> nameList = argumentList.getNameList();
//        if (nameList == null) {
//            return null;
//        }
//        List<Type> typeList = new ArrayList<Type>();
//        for (Name name : nameList) {
//            if (name.type == null) {
//                Type type = getVarNameType(name, rootEnv);
//                name.type = type;
//            }
//            typeList.add(name.type);
//        }
//        return typeList;
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
        ASTNode decls = null;
        if (node instanceof ClassDecl) {
            decls = ((ClassDecl)node).getClassBodyDecls();
        } else {
            if (!((InterfaceDecl)node).hasMemberDecls()) {
                return new ArrayList<MethodDecl>();
            }
            decls = ((InterfaceDecl)node).getInterfaceMemberDecls();
        }
        List<MethodDecl> methodDecls = new ArrayList<MethodDecl>();
        for (ASTNode decl : decls.children) {
            if (decl instanceof MethodDecl) {
                String methodName = ((MethodDecl)decl).getName();
//                System.out.println("searched name: " +fieldName);
                if (methodName.equals(name)) {
                    methodDecls.add((MethodDecl) decl);
                }
            }
        }
        return methodDecls;
    }

    private void assignMethodInvocationReturnType(Name methodName, MethodInvocation methodInvocation, RootEnvironment rootEnv) throws SemanticError {
        // no need for worrying about overwriting here, as overwriting can only happen when they have the same sig and return type
        Referenceable methodDecls = null;
        ScopeEnvironment nameScope = rootEnv.ASTNodeToScopes.get(methodName);
        List<MethodDecl> methods = null;
            // simple Name
        if (methodName.isSimpleName()) {
            // construct simple name
            String nameStr = methodName.getValue();
            Token nameToken = tools.simpleNameConstructor(nameStr);
            // lookup
            methodDecls = nameScope.lookup(nameToken); // FIXME:: how to solve overloading method
        } else {
            // qualified name
            Referenceable decl = rootEnv.lookup(methodName);
            if (decl == null) {
                List<String> fullName = methodName.getFullName();
                String nameStr = fullName.subList(fullName.size()-2, fullName.size()-1).get(0);
                Token nameToken = tools.simpleNameConstructor(nameStr);
                decl = nameScope.lookup(nameToken);
                if (decl instanceof ClassDecl || decl instanceof InterfaceDecl ) { //Fixme
                    methods = findMethodDecls((ASTNode) decl, fullName.subList(fullName.size()-1, fullName.size()).get(0));
                }
            }

        }
//        if (methodDecls == null) {
//            throw new SemanticError("Cannot find method " + methodName.getValue());
//        }
        ArgumentList argList = methodInvocation.getArgumentList();
        List<Type> targetArgType = null;
        if (argList != null) {
            targetArgType = getArgumentType(argList, rootEnv);
        }
        if (methodDecls instanceof MethodList || methods!= null) {
            if (methodDecls instanceof MethodList && methods == null) {
                methods = ((MethodList)methodDecls).methods;
            }
            for (MethodDecl method : methods) {
                List<Type> searchParamType = method.getParamType();
                if (compareParam(searchParamType, targetArgType)) { // FIXME:: What if the arg type cannot be resolved here
                    if (!method.isStatic() && !methodName.isSimpleName()) {
                        throw new SemanticError("Method is not static "+ method.getName());
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
//        if (node instanceof MethodInvocation) {
//            return true;
//        }
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

    public void traverseNode(ASTNode node, RootEnvironment rootEnv) throws SemanticError, Exception {
        if (skipNode(node)) {
            return;
        }

        if (node.traversed) {
            return;
        }

//        System.out.println(node);
        if (!(node instanceof MethodInvocation)) {
            for (ASTNode child : node.children) {
                traverseNode(child, rootEnv);
            }
        }
        findNodeNameAndType(node, rootEnv);
        node.traversed = true;


    }
}
