package type;
import ast.*;
import lexer.*;
import exception.SemanticError;
import java.util.*;

import utils.DebugID;
import utils.tools;

public class TypeLinker {
    /** check whether the import name clash with class defined in scope*/
    static void checkClash(ScopeEnvironment scope, String importName) throws SemanticError{
        String simpleName = tools.getSimpleName(importName);
        //System.out.println(scope.simpleNameSet + "env is " + scope);
        if (scope.simpleNameSet.contains(simpleName) && !scope.localDecls.containsKey(importName)) throw new SemanticError("Import name "+ importName + " clash with type decl " + simpleName);
        scope.simpleNameSet.add(tools.getSimpleName(importName));
    }

    static void checkClashTypeOnDemandImport(ScopeEnvironment scope, String importName) throws SemanticError{
        for (ASTNode node : scope.childScopes.keySet()){
            if (node instanceof TypeImportOndemandDecl){
               // System.out.println("check " + importName + " in " + scope);
                checkClash(scope.childScopes.get(node), importName);
            }
        }
    }

    /** check whether the import class exist or not*/
    static void checkExist(ScopeEnvironment scope, Name importName) throws SemanticError{
        Referenceable importDecl = scope.root.lookup(importName);
        if (importDecl == null) throw new SemanticError("Import cannot find type decl " + importName);
    }

    /** add the imported typedecl to the scope*/
    static void addImportRefer(ScopeEnvironment scope, Name importName) throws SemanticError{
        if (!scope.localDecls.containsKey(importName.getValue())){  // if not contained in here
            Referenceable importDecl = scope.root.lookup(importName);
            scope.localDecls.put(importName.getValue(), importDecl);
        }
    }


    static void processSingleTypeImportDecl(RootEnvironment env, SingleTypeImportDecl singleTypeImportDecl){
        ScopeEnvironment scope = env.ASTNodeToScopes.get(singleTypeImportDecl);
        Name importName = singleTypeImportDecl.getName();
        checkExist(scope, importName);
        checkClash(scope, importName.getValue());
        addImportRefer(scope, importName);
    }

    static boolean qualifiedNameStartsWith(String s1, String s2){

        String[] s1List = s1.split("\\.");

        String str = "";
        if (str.equals(s2)) return true;
        for (String s1Str : s1List){
            str += s1Str;
            //System.out.println("base is " + str + " prefix is " + s2 + " res is " + str.equals(s2));
            if (str.equals(s2)) return true;
            str += '.';
        }
        return false;
    }

    static void addAllSelfTypeDecls(ScopeEnvironment targetScope, ScopeEnvironment packageScope, Map<String, Referenceable>localDecl){
        if (targetScope == null) return;
        for (ScopeEnvironment compScope : packageScope.childScopes.values()){
            for (String qualifiedName : compScope.localDecls.keySet()){
                if (qualifiedName.startsWith(packageScope.prefix)){
                    String simpleName = tools.getSimpleName(qualifiedName);
                    if (!localDecl.containsKey(qualifiedName)){
                        assert targetScope.parent instanceof ScopeEnvironment;
                        ScopeEnvironment targetParent = (ScopeEnvironment)targetScope.parent;
                        //checkClashTypeOnDemandImport(targetParent, qualifiedName);
                        //System.out.println("local decl is " + localDecl + " qualified name is " + qualifiedName);
                        targetScope.localDecls.put(qualifiedName, compScope.localDecls.get(qualifiedName));
                        targetScope.simpleNameSet.add(simpleName);
                    }
                }
            }
        }
    }

    static List<String> findPackages(RootEnvironment env,String packageNameStr) throws SemanticError{
        List<String> res = new ArrayList<String>();
        for (String s : env.packageScopes.keySet()){
            if (qualifiedNameStartsWith(s, packageNameStr)) res.add(s);
        }
        if (res.isEmpty()) throw new SemanticError("Cannot find package " + packageNameStr);
        return res;
    }

    static boolean existTypeOnDemand(ScopeEnvironment env, String name){
        for (ASTNode node : env.childScopes.keySet()){
            if (node instanceof TypeImportOndemandDecl){
                TypeImportOndemandDecl typeImportOndemandDecl = (TypeImportOndemandDecl)node;
                if (name.equals(typeImportOndemandDecl.getName().getValue())) return true;
            }
        }
        return false;
    }

    static void processTypeImportOndemandDecl(ScopeEnvironment scope, TypeImportOndemandDecl typeImportOndemandDecl) throws SemanticError{
        assert scope.isCompliationUnit();
        RootEnvironment env = scope.root;
        String packageNameStr = typeImportOndemandDecl.getName().getValue();
        checkPrefixNotType(scope.root, typeImportOndemandDecl.getName().getFullName(), false, scope);
        List<String> relatedPackages = findPackages(env, packageNameStr);
        for (String packageName : relatedPackages){
            ScopeEnvironment packageScope = env.packageScopes.get(packageName);
            if (!existTypeOnDemand(scope, packageNameStr)){
                scope.childScopes.put(typeImportOndemandDecl, new ScopeEnvironment(scope, env, scope.prefix, scope.typeDecl));
            }
            ScopeEnvironment targetScope = scope.childScopes.get(typeImportOndemandDecl);
            addAllSelfTypeDecls(targetScope, packageScope, scope.localDecls);   // add all self class or interface decls from package scope to import scope
        }

    }

    static void autoImportJavaLang(ScopeEnvironment scope) throws SemanticError{
        TypeImportOndemandDecl javaLangDecl = new TypeImportOndemandDecl(new ArrayList<ASTNode>(){{add(tools.nameConstructor("java.lang"));}}, "");
        processTypeImportOndemandDecl(scope, javaLangDecl);
    }


    static boolean emptyPackageCase(Referenceable res, RootEnvironment env){
        if (res == null)return false;
        assert env.ASTNodeToScopes.get(res).parent instanceof ScopeEnvironment;
        ScopeEnvironment scopeEnvironment = (ScopeEnvironment)env.ASTNodeToScopes.get(res).parent;
        //System.out.println(scopeEnvironment.prefix);
        if (scopeEnvironment.prefix.equals("")){
            return true;
        }
        return false;
    }

    /** If prefix of the name is a type, then throw semantic error */
    static void checkPrefixNotType(RootEnvironment env, List<String> names, boolean strictOrNot, ScopeEnvironment scope) throws SemanticError{
        String nameStr = "";
        Referenceable res = null;
        int nameLen = names.size();
        if (strictOrNot) nameLen -= 1;
        for (int i = 0; i < nameLen; i++){
            String s = names.get(i);
            nameStr = nameStr + s;
            if (i == 0 && strictOrNot){
                Referenceable simpleRes = scope.lookupTypeDecl(tools.simpleNameConstructor(nameStr));
                if (simpleRes != null && !emptyPackageCase(res, env)) throw new SemanticError("Prefix: " + nameStr + " cannot be resolved to type in ");
            }
            res = env.lookup(tools.nameConstructor(nameStr));
            //System.out.println("name is " + nameStr + " res is " + res + " empty? " + emptyPackageCase(res, env));
            if (res != null && !emptyPackageCase(res, env)) throw new SemanticError("Prefix: " + nameStr + " cannot be resolved to type in ");
            nameStr = nameStr + '.';
        }
    }

    static void processEmptyPackage(RootEnvironment env, ScopeEnvironment scopeEnvironment) {
        assert env.packageScopes.containsKey("");
        ScopeEnvironment packageScope = env.packageScopes.get("");
        ScopeEnvironment targetScope = new ScopeEnvironment(packageScope, env, packageScope.prefix, null);
        scopeEnvironment.childScopes.put(new PackageDecl(new ArrayList<>(), ""), targetScope);
        addAllSelfTypeDecls(targetScope, packageScope, scopeEnvironment.localDecls);
    }

    /** No package names—including their prefixes—of declared packages, single-type-import declarations or import-on-demand declarations
     * that are used may resolve to types, except for types in the default, unnamed package.*/
    static void processPackageDecl(RootEnvironment env, PackageDecl packageDecl) throws SemanticError{
        List<String> names = packageDecl.getName().getFullName();
        ScopeEnvironment scope = env.ASTNodeToScopes.get(packageDecl);
        checkPrefixNotType(env, names, false, scope);

        scope.childScopes.put(packageDecl, new ScopeEnvironment(scope, env,"", scope.typeDecl)); // create a new scope for package classes
        ScopeEnvironment packageScope = env.packageScopes.get(packageDecl.getName().getValue());
        addAllSelfTypeDecls(scope.childScopes.get(packageDecl), packageScope, scope.localDecls);
    }

    static void resolveTypename(ScopeEnvironment env, ClassOrInterfaceType type) throws SemanticError{
        Name typeName = type.getName();
        if (typeName.isSimpleName()){
            Token simpleName = tools.simpleNameConstructor(tools.getSimpleName(typeName));
            Referenceable res = env.lookupTypeDecl(simpleName);
            if (res == null) throw new SemanticError("Cannot find symbol " + simpleName+  " scope " );
            type.typeDecl = (TypeDecl)res;
        }   else {
            Referenceable res = env.lookup(typeName);
            if (res == null) throw new SemanticError("Cannot find symbol " + typeName.getValue() + " res " + res);
            checkPrefixNotType(env.root, typeName.getFullName(), true, env);
            type.typeDecl = (TypeDecl)res;
        }
    }

    static void processType(ScopeEnvironment scope, Type type){
        if (type == null) return;
        if (type instanceof ClassOrInterfaceType){
            resolveTypename(scope, (ClassOrInterfaceType)type);
        }   else if (type instanceof ArrayType){
            /** process array of ClassOrInterfaceType*/
            if (((ArrayType)type).getType() instanceof ClassOrInterfaceType){
                resolveTypename(scope, (ClassOrInterfaceType)((ArrayType)type).getType());
                tools.println("link array type " + (ClassOrInterfaceType)((ArrayType)type).getType() + " " + ((ClassOrInterfaceType)((ArrayType)type).getType()).typeDecl, DebugID.zhenyan);
            }
        }
    }

    static void link(RootEnvironment env, ASTNode node) throws SemanticError{
        if (node == null) return;
        if (node instanceof SingleTypeImportDecl){
            SingleTypeImportDecl singleTypeImportDecl = (SingleTypeImportDecl)node;
            processSingleTypeImportDecl(env, singleTypeImportDecl);
        }   else if (node instanceof TypeImportOndemandDecl){
            TypeImportOndemandDecl typeImportOndemandDecl = (TypeImportOndemandDecl)node;
            processTypeImportOndemandDecl(env.ASTNodeToScopes.get(typeImportOndemandDecl), typeImportOndemandDecl);
        }   else if (node instanceof PackageDecl){
            PackageDecl packageDecl = (PackageDecl)node;
            processPackageDecl(env, packageDecl);
        }   else if (node instanceof Super){
            Super superNode = (Super)node;
            ScopeEnvironment localScope = env.ASTNodeToScopes.get(superNode);
            Type type = superNode.getType();
            processType(localScope,type);
        }   else if (node instanceof ExtendsInterfaces){
            ExtendsInterfaces extendsInterfaces = (ExtendsInterfaces)node;
            ScopeEnvironment localScope = env.ASTNodeToScopes.get(extendsInterfaces);
            List<ClassOrInterfaceType> interfaceTypes = extendsInterfaces.getInterfaceTypeList();
            for (ClassOrInterfaceType i : interfaceTypes){
                processType(localScope, i);
            }
        }   else if (node instanceof Interfaces){
            Interfaces interfaces = (Interfaces)node;
            ScopeEnvironment localScope = env.ASTNodeToScopes.get(interfaces);
            List<ClassOrInterfaceType> interfaceTypes = interfaces.getInterfaceTypeList().getInterfaceTypeList();
            for (ClassOrInterfaceType i : interfaceTypes){
                processType(localScope, i);
            }
        }   else if (node instanceof FieldDecl){
            FieldDecl fieldDecl = (FieldDecl)node;
            ScopeEnvironment localScope = env.ASTNodeToScopes.get(fieldDecl);
            processType(localScope, fieldDecl.getType());
        }   else if (node instanceof ClassInstanceCreateExpr ){
            ClassInstanceCreateExpr classInstanceCreateExpr = (ClassInstanceCreateExpr)node;
            ScopeEnvironment localScope = env.ASTNodeToScopes.get(classInstanceCreateExpr);
            Type type = classInstanceCreateExpr.getType();
            processType(localScope,type);

        } else if (node instanceof MethodDecl){ // Above condition will all be checked before processing here
            MethodDecl methodDecl = (MethodDecl)node;
            ScopeEnvironment methodScope = env.ASTNodeToScopes.get(methodDecl);
            Type returnType = methodDecl.getMethodHeader().getType();
            processType(methodScope, returnType);
            if (methodDecl.getMethodHeader().getMethodDeclarator().getParameterList() != null){
                for (Parameter p : methodDecl.getMethodHeader().getMethodDeclarator().getParameterList().getParams()){
                    Type type = p.getType();
                    processType(methodScope, type);
                }
            }
        }   else if (node instanceof AbstractMethodDecl){
            AbstractMethodDecl methodDecl = (AbstractMethodDecl)node;
            ScopeEnvironment methodScope = env.ASTNodeToScopes.get(methodDecl);
            Type returnType = methodDecl.getType();
            processType(methodScope, returnType);
            if (methodDecl.getMethodDeclarator().getParameterList() != null){
                for (Parameter p : methodDecl.getMethodDeclarator().getParameterList().getParams()){
                    Type type = p.getType();
                    processType(methodScope, type);
                }
            }
        } else if (node instanceof ConstructorDecl){
            ConstructorDecl constructorDecl = (ConstructorDecl)node;
            ScopeEnvironment methodScope = env.ASTNodeToScopes.get(constructorDecl);
            if (constructorDecl.getConstructorDeclarator().getParameterList() != null){
                for (Parameter p : constructorDecl.getConstructorDeclarator().getParameterList().getParams()){
                    Type type = p.getType();
                    processType(methodScope, type);
                }
            }
        }   else if (node instanceof LocalVarDecl){
            LocalVarDecl localVarDecl = (LocalVarDecl)node;
            ScopeEnvironment varScope = env.ASTNodeToScopes.get(localVarDecl);
            Type type = localVarDecl.getType();
            processType(varScope, type);
        }   else if (node instanceof ArrayCreationExpr){
            ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr)node;
            ScopeEnvironment localScope = env.ASTNodeToScopes.get(arrayCreationExpr);
            Type type = arrayCreationExpr.getType();
            processType(localScope, type);
        }   else if (node instanceof CastExpr){
            CastExpr castExpr = (CastExpr)node;
            ScopeEnvironment localScope = env.ASTNodeToScopes.get(castExpr);
            castExpr.changePrefixExprToType(); //
            Type type = castExpr.getType();
            tools.println(type, DebugID.zhenyan);
            processType(localScope, type);
        }   else if (node instanceof RelationExpr){
            RelationExpr relationExpr = (RelationExpr)node;
            if (relationExpr.isInstanceOf()){
                Type type = relationExpr.getType();
                ScopeEnvironment localScope = env.ASTNodeToScopes.get(relationExpr);
                processType(localScope, type);
            }
        }
        linkAll(env, node.children);
    }

    static void linkAll(RootEnvironment env, List<ASTNode> nodes) throws SemanticError{
        for (ASTNode node : nodes){
            link(env, node);
        }
    }

    static void linkAllCompilationUnit(RootEnvironment env, List<CompilationUnit> nodes) throws SemanticError{
        for (CompilationUnit node : nodes){
            autoImportJavaLang(env.ASTNodeToScopes.get(node));
            if (env.ASTNodeToScopes.get(node).prefix.equals("")){
                processEmptyPackage(env, env.ASTNodeToScopes.get(node));
            }
            link(env, node);
        }
    }
}
