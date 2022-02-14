package type;
import ast.*;
import lexer.*;
import exception.SemanticError;
import java.util.*;
import utils.tools;

public class TypeLinker {
    /** check whether the import name clash with class defined in scope*/
    static void checkClash(ScopeEnvironment scope, String importName) throws SemanticError{
        String simpleName = tools.getSimpleName(importName);
        if (scope.simpleNameSet.contains(simpleName)) throw new SemanticError("Import name "+ importName + " clash with type decl " + simpleName);
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

    static void addAllSelfTypeDecls(ScopeEnvironment targetScope, ScopeEnvironment packageScope){
        for (ScopeEnvironment compScope : packageScope.childScopes.values()){
            for (String qualifiedName : compScope.localDecls.keySet()){
                if (qualifiedName.startsWith(packageScope.prefix)){
                    targetScope.localDecls.put(qualifiedName, compScope.localDecls.get(qualifiedName));
                }
            }
        }
    }

    static void processTypeImportOndemandDecl(ScopeEnvironment scope, TypeImportOndemandDecl typeImportOndemandDecl) throws SemanticError{
        RootEnvironment env = scope.root;
        String packageNameStr = typeImportOndemandDecl.getName().getValue();
        if (!env.packageScopes.containsKey(packageNameStr)) throw new SemanticError("Cannot find package: " + packageNameStr);
        ScopeEnvironment packageScope = env.packageScopes.get(packageNameStr);
        if (!scope.childScopes.containsKey(typeImportOndemandDecl)){
            scope.childScopes.put(typeImportOndemandDecl, new ScopeEnvironment(scope, env, scope.prefix));
        }
        ScopeEnvironment targetScope = scope.childScopes.get(typeImportOndemandDecl);
        addAllSelfTypeDecls(targetScope, packageScope);   // add all self class or interface decls from package scope to import scope
    }

    static void autoImportJavaLang(ScopeEnvironment scope) throws SemanticError{
        TypeImportOndemandDecl javaLangDecl = new TypeImportOndemandDecl(new ArrayList<ASTNode>(){{add(tools.nameConstructor("java.lang"));}}, "");
        processTypeImportOndemandDecl(scope, javaLangDecl);
    }


    /** If prefix of the name is a type, then throw semantic error */
    static void checkPrefixNotType(RootEnvironment env, List<String> names, boolean strictOrNot) throws SemanticError{
        String nameStr = "";
        Referenceable res = null;
        int nameLen = names.size();
        if (strictOrNot) nameLen -= 1;
        for (int i = 0; i < nameLen; i++){
            String s = names.get(i);
            nameStr = nameStr + s;
            res = env.lookup(tools.nameConstructor(nameStr));
            if (res != null) throw new SemanticError("Prefix: " + nameStr + " cannot be resolved to type");
            nameStr = nameStr + '.';
        }
    }

    /** No package names—including their prefixes—of declared packages, single-type-import declarations or import-on-demand declarations
     * that are used may resolve to types, except for types in the default, unnamed package.*/
    static void processPackageDecl(RootEnvironment env, PackageDecl packageDecl) throws SemanticError{
        List<String> names = packageDecl.getName().getFullName();
        checkPrefixNotType(env, names, false);
        ScopeEnvironment scope = env.ASTNodeToScopes.get(packageDecl);
        scope.childScopes.put(packageDecl, new ScopeEnvironment(scope, env, scope.prefix)); // create a new scope for package classes
        ScopeEnvironment packageScope = env.packageScopes.get(packageDecl.getName().getValue());
        addAllSelfTypeDecls(scope.childScopes.get(packageDecl), packageScope);
    }

    static void resolveTypename(ScopeEnvironment env, ClassOrInterfaceType type) throws SemanticError{
        Name typeName = type.getName();
        if (typeName.isSimpleName()){
            Token simpleName = tools.simpleNameConstructor(tools.getSimpleName(typeName));
            Referenceable res = env.lookupTypeDecl(simpleName);
            if (res == null) throw new SemanticError("Cannot find symbol " + simpleName.value);
            type.typeDecl = res;
        }   else {
            Referenceable res = env.lookup(typeName);
            if (res == null) throw new SemanticError("Cannot find symbol " + typeName.getValue());
            checkPrefixNotType(env.root, typeName.getFullName(), true);
            type.typeDecl = res;
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
            autoImportJavaLang(env.ASTNodeToScopes.get(packageDecl));
        }   else if (node instanceof ClassOrInterfaceType){ // Above condition will all be checked before processing here
            ClassOrInterfaceType type = (ClassOrInterfaceType)node;
            resolveTypename(env.ASTNodeToScopes.get(node), type);
            //System.out.println(type.getName().getValue() + " is linked to " + type.typeDecl);
        }
        linkAll(env, node.children);
    }

    static void linkAll(RootEnvironment env, List<ASTNode> nodes) throws SemanticError{
        for (ASTNode node : nodes){
            link(env, node);
        }
    }
}
