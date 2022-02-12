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
                    checkClash(targetScope, qualifiedName);
                    targetScope.localDecls.put(qualifiedName, compScope.localDecls.get(qualifiedName));
                }
            }
        }
    }

    static void processTypeImportOndemandDecl(RootEnvironment env, TypeImportOndemandDecl typeImportOndemandDecl) throws SemanticError{
        ScopeEnvironment scope = env.ASTNodeToScopes.get(typeImportOndemandDecl);
        String packageNameStr = typeImportOndemandDecl.getName().getValue();
        if (!env.packageScopes.containsKey(packageNameStr)) throw new SemanticError("Cannot find package: " + packageNameStr);
        ScopeEnvironment packageScope = env.packageScopes.get(packageNameStr);
        addAllSelfTypeDecls(scope, packageScope);   // add all self class or interface decls from package scope to import scope
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
            processTypeImportOndemandDecl(env, typeImportOndemandDecl);
        }   else if (node instanceof PackageDecl){
            PackageDecl packageDecl = (PackageDecl)node;
            processPackageDecl(env, packageDecl);
        }   else if (node instanceof ClassOrInterfaceType){
            ClassOrInterfaceType type = (ClassOrInterfaceType)node;
            resolveTypename(env.ASTNodeToScopes.get(node), type);
        }
        linkAll(env, node.children);
    }

    static void linkAll(RootEnvironment env, List<ASTNode> nodes) throws SemanticError{
        for (ASTNode node : nodes){
            link(env, node);
        }
    }
}
