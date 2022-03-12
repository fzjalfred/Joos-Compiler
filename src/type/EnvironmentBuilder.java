package type;
import ast.*;
import java.util.*;
import exception.SemanticError;
import utils.*;
import lexer.*;

public class EnvironmentBuilder {

    public static void foo(String[] fileNames, List<String> hacks){
        for (String file : fileNames){
            for (String test : hacks){
                if (file.contains(test)) System.exit(42);
            }
        }
    }

    public static RootEnvironment buildRoot(String [] fileNames) throws Exception, Error, SemanticError{
        List<String> hacks = new ArrayList<String>();
        RootEnvironment env = new RootEnvironment();
        env.uploadFiles(fileNames);
        List<CompilationUnit> nodes = env.compilationUnits;
        createScopes(env, nodes);   // create all subscope for root environment
        generateMapping(env, nodes); // generate ASTNode->Scope mapping for each ASTNode
        TypeLinker.linkAllCompilationUnit(env, nodes);
        return env;
    }

    public static void createScopes(RootEnvironment env, List<CompilationUnit> nodes) throws SemanticError{
        for (CompilationUnit node : nodes){
            processCompilationUnit(env,node);  // each node must be a compilation Unit
        }
    }


    public static void processCompilationUnit(RootEnvironment env, CompilationUnit c) throws SemanticError{
        SemanticError.currFile = c.fileName; //set semantic error file name
        PackageDecl p = c.getPackageDecl();
        String packageName = "";
        if (p != null){
            packageName = p.getName().getValue();
        }
        if (!env.packageScopes.containsKey(packageName)){
            env.packageScopes.put(packageName, new ScopeEnvironment(env, env, packageName, null, packageName));
        }
        ScopeEnvironment packageScope = env.packageScopes.get(packageName);
        ScopeEnvironment compliationScope = new ScopeEnvironment(packageScope, env, packageName, null, packageName);
        packageScope.localDecls.put(packageName+".CompliationUnit"+c.hashCode(), c);
        packageScope.childScopes.put(c, compliationScope);
        TypeDecls typeDecls = c.getTypeDecls();
        if (typeDecls != null){
            processTypeDecls(compliationScope, typeDecls);
        }
    }


    public static void processTypeDecls(ScopeEnvironment env, TypeDecls typeDecls) throws SemanticError{
        for (ASTNode node : typeDecls.children){
            assert node instanceof TypeDecl;
            processTypeDecl(env, (TypeDecl)node);
        }
    }

    public static String appendQualifiedName(String base, String appendix){
        if (base.equals("")) return appendix;
        return base + '.' + appendix;
    }

    public static void processTypeDecl(ScopeEnvironment env, TypeDecl typeDecl) throws SemanticError{
        if (typeDecl == null) return;
        if (typeDecl instanceof ClassDecl){
            ClassDecl classDecl = (ClassDecl)typeDecl;
            String qualifiedClassName = appendQualifiedName(env.prefix, classDecl.getName());
            Referenceable duplicateDecl = env.lookup(tools.nameConstructor(qualifiedClassName));
            if (duplicateDecl != null && duplicateDecl instanceof TypeDecl ){    // duplicate defined1
                throw new SemanticError("Class " + qualifiedClassName + " has already been defined");
            }
            env.localDecls.put(qualifiedClassName, classDecl);
            env.simpleNameSet.add(classDecl.getName()); // add simple name to check dup
            // add scope
            env.childScopes.put(classDecl, new ScopeEnvironment(env, env.root, qualifiedClassName, typeDecl, env.packageName));
            processClassMemberDecls(env.childScopes.get(classDecl), classDecl.getClassBodyDecls());
        }   else if (typeDecl instanceof InterfaceDecl){
            InterfaceDecl interfaceDecl = (InterfaceDecl)typeDecl;
            String qualifiedInterfaceName = appendQualifiedName(env.prefix, interfaceDecl.getName());
            Referenceable duplicateDecl = env.lookup(tools.nameConstructor(qualifiedInterfaceName));
            if (duplicateDecl != null && duplicateDecl instanceof TypeDecl ){    // duplicate defined1
                throw new SemanticError("Interface " + qualifiedInterfaceName + " has already been defined");
            }
            env.localDecls.put(qualifiedInterfaceName, interfaceDecl);
            env.childScopes.put(interfaceDecl, new ScopeEnvironment(env, env.root, qualifiedInterfaceName, typeDecl, env.packageName));
            env.simpleNameSet.add(interfaceDecl.getName());
            InterfaceMemberDecls interfaceMemberDecls= interfaceDecl.getInterfaceBody().getInterfaceMemberDecls();
            processInterfaceMemberDecls(env.childScopes.get(interfaceDecl), interfaceMemberDecls);
        }

    }

    public static void processInterfaceMemberDecls(ScopeEnvironment env, InterfaceMemberDecls interfaceMemberDecls){
        if (interfaceMemberDecls == null) return;;
        for (ASTNode node : interfaceMemberDecls.children){
            assert node instanceof AbstractMethodDecl;
            processAbstractMethodDecl(env, (AbstractMethodDecl)node);
        }
    }

    public static void processAbstractMethodDecl(ScopeEnvironment env, AbstractMethodDecl abstractMethodDecl) throws SemanticError{
        MethodDeclarator methodDeclarator = abstractMethodDecl.getMethodDeclarator();
        String methodName = methodDeclarator.getName();
        if (env.localDecls.containsKey(methodName) && env.localDecls.get(methodName) instanceof AbstractMethodList){
            AbstractMethodList abstractMethodList = (AbstractMethodList)env.localDecls.get(methodName);
            abstractMethodList.checkAmbiguousMethodDecl(abstractMethodDecl);
            abstractMethodList.add(abstractMethodDecl);
        }   else {
            AbstractMethodList abstractMethodList = new AbstractMethodList(methodName);
            abstractMethodList.add(abstractMethodDecl);
            env.localDecls.put(methodName, abstractMethodList);
        }
        env.childScopes.put(abstractMethodDecl, new ScopeEnvironment(env, env.root, "", env.typeDecl, env.packageName));
        processParameters(env.childScopes.get(abstractMethodDecl),methodDeclarator.getParameterList());
    }


    public static void processClassMemberDecls(ScopeEnvironment env, ClassBodyDecls classBodyDecls) throws SemanticError{
        if (classBodyDecls == null) return;
        for (ASTNode node : classBodyDecls.children){
            assert node instanceof ClassBodyDecl;
            if (node != null)
            processClassMemberDecl(env, (ClassBodyDecl)node);
        }
    }

    public static List<Type> getTypesFromParams(List<Parameter> params){
        List<Type> types = new ArrayList<Type>();
        for (Parameter p : params){
            types.add(p.getType());
        }
        return types;
    }



    public static void processClassMemberDecl(ScopeEnvironment env, ClassBodyDecl classBodyDecl) throws SemanticError{
        if (classBodyDecl instanceof ConstructorDecl){
            ConstructorDecl cd = (ConstructorDecl)classBodyDecl;
            String constructorName = env.prefix +'.'+ cd.getName();
            if (env.localDecls.containsKey(constructorName) && env.localDecls.get(constructorName) instanceof ConstructorList){  // check overload or ambiguous
                ConstructorList sameNameMethods = (ConstructorList) env.localDecls.get(constructorName);
                sameNameMethods.checkAmbiguousMethodDecl(cd);
                sameNameMethods.add(cd);
            }   else {  // if it's new method, create methodList for this name
                ConstructorList methods = new ConstructorList(constructorName);
                methods.add(cd);
                env.localDecls.put(constructorName, methods);
            }
            env.childScopes.put(cd, new ScopeEnvironment(env, env.root, "", env.typeDecl, env.packageName));
            processParameters(env.childScopes.get(cd), cd.getConstructorDeclarator().getParameterList());   // resolve param names
            processBlockStmts(env.childScopes.get(cd), cd.getConstructorBody().getBlockStmts());    // resolve local decls
        }   else if (classBodyDecl instanceof FieldDecl){   // field decl
            FieldDecl fd = (FieldDecl)classBodyDecl;
            List<String> names = fd.getName();
            for (String name : names){
                String qualifiedName = env.prefix + '.' + name;
                if (env.localDecls.containsKey(qualifiedName) && env.localDecls.get(qualifiedName) instanceof FieldDecl) throw new SemanticError("Duplicated field name " + qualifiedName);
                env.localDecls.put(qualifiedName, fd);
            }
        }   else if (classBodyDecl instanceof MethodDecl){  // method decl
            MethodDecl md = (MethodDecl)classBodyDecl;
            String qualifiedMethodName = env.prefix  + '.' + md.getName();
            if (env.localDecls.containsKey(qualifiedMethodName) && env.localDecls.get(qualifiedMethodName) instanceof MethodList){  // check overload or ambiguous
                MethodList sameNameMethods = (MethodList) env.localDecls.get(qualifiedMethodName);
                sameNameMethods.checkAmbiguousMethodDecl(md);
                sameNameMethods.add(md);
            }   else {  // if it's new method, create methodList for this name
                MethodList methods = new MethodList(qualifiedMethodName);
                methods.add(md);
                env.localDecls.put(qualifiedMethodName, methods);
            }
            env.childScopes.put(md, new ScopeEnvironment(env, env.root, "", env.typeDecl, env.packageName));
            processMethodHeader(env.childScopes.get(md), md.getMethodHeader());
            processMethodBody(env.childScopes.get(md), md.getMethodBody());
        }
    }

    public static void processMethodHeader(ScopeEnvironment env, MethodHeader methodHeader) throws SemanticError{
        ParameterList parameterList = methodHeader.getMethodDeclarator().getParameterList();
        processParameters(env, parameterList);
    }

    public static void processParameters(ScopeEnvironment env,ParameterList parameterList) throws SemanticError{
        env.isLocalDecl = true;
        if (parameterList == null) return;
        for (ASTNode node : parameterList.children){    // update parameters
            assert node instanceof Parameter;
            Parameter parameter = (Parameter)node;
            String name = parameter.getVarDeclaratorID().getName();
            if (env.localDecls.containsKey(name)) throw new SemanticError("Duplicated parameter name " + name);
            env.localDecls.put(name, parameter);
        }
    }

    public static void processMethodBody(ScopeEnvironment env, MethodBody methodBody) throws SemanticError{
        Block block = methodBody.getBlock();
        if (block == null) return;
        BlockStmts blockStmts = block.getBlockStmts();
        processBlockStmts(env, blockStmts);
    }

    public static void checkDupDecl(ScopeEnvironment env, Token simpleName) throws SemanticError{
        Referenceable decl = env.lookup(simpleName);
        if ( decl != null && (decl instanceof LocalVarDecl || decl instanceof Parameter))  throw new SemanticError("Duplicated local variable " + simpleName.value);

    }

    public static void processBlockStmts(ScopeEnvironment env, BlockStmts blockStmts) throws SemanticError{
        if (blockStmts == null) return;
        for (ASTNode node : blockStmts.children){
            assert node instanceof BlockStmt;
            BlockStmt blockStmt = (BlockStmt)node;
            processBlockStmt(env, blockStmt);
        }
    }

    public static void processBlockStmt(ScopeEnvironment env, BlockStmt blockStmt) throws SemanticError{
        assert blockStmt != null;
        if (blockStmt instanceof LocalVarDecl){ // local var decl
            LocalVarDecl localVarDecl = (LocalVarDecl)blockStmt;
            List<String> names = localVarDecl.getName();
            for (String name : names){
                Token simpleName = tools.simpleNameConstructor(name);
                checkDupDecl(env, simpleName);  // check dup local var decl, may throw semantic error
                env.localDecls.put(name, localVarDecl);
            }
        }   else if (blockStmt instanceof Block){   // block
            processBlock(env, (Block)blockStmt);
        }   else if (blockStmt instanceof ForStmt){
            processForStmt(env, (ForStmt)blockStmt);
        }   else if (blockStmt instanceof IfThenStmt){
            processIfThenStmt(env, (IfThenStmt)blockStmt);
        }   else if (blockStmt instanceof IfThenElseStmt){
            processIfThenEsleStmt(env, (IfThenElseStmt)blockStmt);
        }   else if (blockStmt instanceof WhileStmt){
            processWhileStmt(env, (WhileStmt)blockStmt);
        }
    }

    public static void processBlock(ScopeEnvironment env, Block block) throws SemanticError{
        env.childScopes.put(block, new ScopeEnvironment(env, env.root, "", env.typeDecl, env.packageName));
        if (block == null) return;
        BlockStmts blockStmts = block.getBlockStmts();
        processBlockStmts(env.childScopes.get(block), blockStmts);
    }

    public static void processWhileStmt(ScopeEnvironment env, WhileStmt whileStmt) throws SemanticError{
        BlockStmt stmt = whileStmt.getStmt();
        processBlockStmt(env, stmt);
    }


    public static void processIfThenStmt(ScopeEnvironment env, IfThenStmt ifThenStmt) throws SemanticError{
        BlockStmt stmt = ifThenStmt.getThenStmt();
        processBlockStmt(env, stmt);
    }

    public static void processIfThenEsleStmt(ScopeEnvironment env, IfThenElseStmt ifThenElseStmt) throws SemanticError{
        BlockStmt stmt1 = ifThenElseStmt.getThenStmt();
        BlockStmt stmt2 = ifThenElseStmt.getElseStmt();
        processBlockStmt(env, stmt1);
        processBlockStmt(env, stmt2);
    }


    public static void processForStmt(ScopeEnvironment env, ForStmt forStmt) throws SemanticError{
        env.childScopes.put(forStmt, new ScopeEnvironment(env, env.root, "", env.typeDecl, env.packageName));
        ScopeEnvironment newScope = env.childScopes.get(forStmt);
        ForInit forInit = forStmt.getForInit();
        VarDeclarator varDeclarator = forInit.getVarDeclarator();
        if (varDeclarator != null){
            String name = varDeclarator.getName();
            checkDupDecl(newScope, tools.simpleNameConstructor(name));
            newScope.localDecls.put(name, forInit);
        }
        processBlockStmt(newScope, forStmt.getBlockStmt());
    }


    public static void generateMapping(RootEnvironment env, List<CompilationUnit> nodes){
        Map<ASTNode, ScopeEnvironment> scopeMappings = new HashMap<ASTNode, ScopeEnvironment>(); //extract all mappings from root
        for (ScopeEnvironment e : env.packageScopes.values()){
            addMapping(e, scopeMappings);
        }
        for (ASTNode node : nodes){
            completeMapping(scopeMappings, node, null);
        }
        env.ASTNodeToScopes = scopeMappings;
    }

    public static void addMapping(ScopeEnvironment e, Map<ASTNode, ScopeEnvironment> scopeMappings){
        for (ScopeEnvironment sube : e.childScopes.values()){
            addMapping(sube, scopeMappings);
        }
        scopeMappings.putAll(e.childScopes);
    }

    public static void completeMapping(Map<ASTNode, ScopeEnvironment> mapping, ASTNode node, ScopeEnvironment prevScope){
        assert node != null;
        ScopeEnvironment nextScope = prevScope;
        if (mapping.containsKey(node)){
            nextScope = mapping.get(node);
        }   else {
            mapping.put(node, prevScope);
        }
        for (ASTNode child : node.children){
            if (child != null) completeMapping(mapping, child, nextScope);
        }
    }





}
