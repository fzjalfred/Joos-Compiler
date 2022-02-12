package type;
import ast.*;
import lexer.parser;
import exception.SemanticError;
import java.util.*;
import utils.tools;

public class TypeLinker {
    static void checkClash(ScopeEnvironment scope, String importName) throws SemanticError{
        String simpleName = tools.getSimpleName(importName);
        if (scope.simpleNameSet.contains(simpleName)) throw new SemanticError("Import name "+ importName + " clash with type decl " + simpleName);
    }

    static void checkExist(ScopeEnvironment scope, Name importName) throws SemanticError{
        Referenceable importDecl = scope.root.lookup(importName);
        if (importDecl == null) throw new SemanticError("Import cannot find type decl " + importName);
    }

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

    static void link(RootEnvironment env, ASTNode node) throws SemanticError{
        if (node == null) return;
        if (node instanceof SingleTypeImportDecl){
            SingleTypeImportDecl singleTypeImportDecl = (SingleTypeImportDecl)node;
            processSingleTypeImportDecl(env, singleTypeImportDecl);
        }   else if (node instanceof TypeImportOndemandDecl){
            TypeImportOndemandDecl typeImportOndemandDecl = (TypeImportOndemandDecl)node;
            processTypeImportOndemandDecl(env, typeImportOndemandDecl);
        }
        linkAll(env, node.children);
    }

    static void linkAll(RootEnvironment env, List<ASTNode> nodes) throws SemanticError{
        for (ASTNode node : nodes){
            link(env, node);
        }
    }
}
