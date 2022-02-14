package hierarchy;

import java.util.*;
import java.io.*;
import ast.*;
import type.*;
import utils.*;
import lexer.*;

public class HierarchyChecking {
    public static void checkRootEnvironment(RootEnvironment env) throws Exception{
        for (String packKey : env.packageScopes.keySet()) {
            ScopeEnvironment packScope = (ScopeEnvironment) env.packageScopes.get(packKey);
            for (ASTNode compileKey : packScope.childScopes.keySet()) {
                checkCompilationUnitScope(packScope.childScopes.get(compileKey));
            }
        }
    }

    public static void checkCompilationUnitScope(ScopeEnvironment env) throws Exception{
        List<ASTNode> nonImported = new ArrayList <ASTNode>(){};
        for (String key : env.localDecls.keySet()){

            if (env.childScopes.containsKey(env.localDecls.get(key))) {
                nonImported.add((ASTNode)env.localDecls.get(key));
            }
        }

        for (ASTNode node : nonImported) {
            if (node instanceof ClassDecl) {
                ClassDecl classDecl = (ClassDecl) node;
                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = checkExtendNode(classDecl, classDecl, env);
                checkImplementNode(classDecl, env);

                checkExtendDecl(classDecl, extendNodes);
            } else if (node instanceof InterfaceDecl) {
                InterfaceDecl interfaceDecl = (InterfaceDecl) node;

                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = checkExtendNode(interfaceDecl, interfaceDecl, env);
                checkExtendDecl(interfaceDecl, extendNodes);
            }

        }
    }

    private static boolean ifContainModifier(ASTNode modifiers, String name){
        if (modifiers == null) return false;
        for (ASTNode n : modifiers.children){
            if (n.value == name) return true;
        }
        return false;
    }

    public static void checkExtendDecl(ClassDecl classDecl, List<Pair<Referenceable, ScopeEnvironment>> parents) throws Exception {

        ASTNode myModifiers = classDecl.children.get(0);
        ASTNode myName = classDecl.children.get(1);
        for (Pair<Referenceable, ScopeEnvironment> node : parents) {
            if (node.first instanceof ClassDecl) {
                ClassDecl parent = (ClassDecl) node.first;
                ASTNode modifiers = parent.children.get(0);
                ASTNode name = parent.children.get(1);
                if (myName.value == name.value) {
                    throw new Exception("Acyclic class name or duplicated name");
                }
                if (ifContainModifier(modifiers, "final")) {
                    throw new Exception("Cannot Extend a final class");
                }
            } else {
                throw new Exception("Class can only extend a class");
            }

        }
    }

    public static void checkExtendDecl(InterfaceDecl interfaceDecl, List<Pair<Referenceable, ScopeEnvironment>> parents) throws Exception {
        ASTNode myName = interfaceDecl.children.get(1);
        for (Pair<Referenceable, ScopeEnvironment> node : parents) {
            if (node.first instanceof InterfaceDecl) {
                InterfaceDecl parent = (InterfaceDecl) node.first;
                ASTNode name = parent.children.get(1);

                if (myName.value == name.value) {
                    throw new Exception("Acyclic interface name or duplicated name");
                }
            } else {
                throw new Exception("Interface can only extend an interface");
            }
        }
    }


    public static List<Pair<Referenceable, ScopeEnvironment>> checkExtendNode(ClassDecl original, ClassDecl classDecl, ScopeEnvironment underEnv) throws Exception{
        List<ASTNode> children = classDecl.children;
        Super superNode = null;
        for (ASTNode node : children){
            if (node instanceof Super) {
                superNode = (Super) node;
                break;
            }
        }

        if (superNode == null) {
            return new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
        }

        Name extendName = (Name) superNode.children.get(0).children.get(0);
        List <Pair<Referenceable, ScopeEnvironment>> extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
        Pair<Referenceable, ScopeEnvironment> found;

        int size = extendName.children.size();
        if (size == 1) { // simple name
            Token id = (Token) extendName.children.get(0);

            found = underEnv.lookupNameAndEnv(id);
        } else { // qualified name
            found = underEnv.lookupNameAndEnv(extendName);
        }

        if (found == null || found.first == null || found.second == null) {
            return extendNodes;
        }
        if (!(found.first instanceof ClassDecl)) {
            throw new Exception("Class can only extends a class");
        }
        if (found.first == original) {
            throw new Exception("Acylic Extends in class");
        }

        ClassDecl classNode = (ClassDecl)found.first;

        extendNodes.add(found);
        extendNodes.addAll(checkExtendNode(original, classNode, found.second));
        return extendNodes;
    }

    public static List<Pair<Referenceable, ScopeEnvironment>> checkExtendNode(InterfaceDecl original, InterfaceDecl interfaceDecl, ScopeEnvironment underEnv) throws Exception {
        List <ASTNode> children = interfaceDecl.children;
        ExtendsInterfaces extendsInterfaces = null;
        for (ASTNode node : children) {
            if (node instanceof ExtendsInterfaces) {
                extendsInterfaces = (ExtendsInterfaces) node;
                break;
            }
        }
        if (extendsInterfaces == null) {
            return new ArrayList<Pair<Referenceable, ScopeEnvironment>>(){};
        }

        List <Pair<Referenceable, ScopeEnvironment>> extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
        List <InterfaceDecl> sameClauseNode = new ArrayList <InterfaceDecl>(){};
        for (ASTNode node : extendsInterfaces.children) {
            Pair<Referenceable, ScopeEnvironment> found;
            Name extendName = (Name) node.children.get(0).children.get(0);

            int size = extendName.children.size();
            if (size == 1) { // simple name
                Token id = (Token) extendName.children.get(0);
                found = underEnv.lookupNameAndEnv(id);
            } else { // qualified name
                found = underEnv.lookupNameAndEnv(extendName);
            }

            if (found == null || found.first == null || found.second == null) {
                continue;
            }
            if (!(found.first instanceof InterfaceDecl)) {
                throw new Exception("interface can only extend an interface");
            }

            if (found.first == original) {
                throw new Exception("Acyclic extend in interface");
            }

            InterfaceDecl parentInterface = (InterfaceDecl) found.first;
            extendNodes.add(found);
            sameClauseNode.add(parentInterface);
            extendNodes.addAll(checkExtendNode(original, parentInterface, found.second));
        }
        int listSize = sameClauseNode.size();
        for (int i = 0; i < listSize; i++){
            for (int j = i + 1; j < listSize; j++) {
                if (sameClauseNode.get(i) == sameClauseNode.get(j)) {
                    throw new Exception("Interface repeats in an extend clause of an interface.");
                }
            }
        }

        return extendNodes;
    }

    public static List<Pair<Referenceable, ScopeEnvironment>> checkImplementNode(ClassDecl classDecl, ScopeEnvironment underEnv) throws Exception{
        List<ASTNode> children = classDecl.children;
        Interfaces interfacesNode = null;
        for (ASTNode node : children){
            if (node instanceof Interfaces) {
                interfacesNode = (Interfaces) node;
                break;
            }
        }

        if (interfacesNode == null) {
            return new ArrayList<Pair<Referenceable, ScopeEnvironment>>(){};
        }

        InterfaceTypeList typeList = (InterfaceTypeList) interfacesNode.children.get(0);
        List <Pair<Referenceable, ScopeEnvironment>> extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
        List <InterfaceDecl> sameClauseNode = new ArrayList <InterfaceDecl>(){};
        for (ASTNode node : typeList.children) {
            Pair<Referenceable, ScopeEnvironment> found;
            Name extendName = (Name) node.children.get(0).children.get(0);

            int size = extendName.children.size();
            if (size == 1) { // simple name
                Token id = (Token) extendName.children.get(0);
                found = underEnv.lookupNameAndEnv(id);
            } else { // qualified name
                found = underEnv.lookupNameAndEnv(extendName);
            }
            if (found == null || found.first == null || found.second == null) {
                continue;
            }
            if (!(found.first instanceof InterfaceDecl)) {
                throw new Exception("class can only implements an interface");
            }

              InterfaceDecl interfaceNode = (InterfaceDecl) found.first;
              sameClauseNode.add(interfaceNode);
              extendNodes.add(found);
              extendNodes.addAll(checkExtendNode(interfaceNode, interfaceNode, found.second));
        }

        int listSize = sameClauseNode.size();
        for (int i = 0; i < listSize; i++){
            for (int j = i + 1; j < listSize; j++) {
                if (sameClauseNode.get(i) == sameClauseNode.get(j)) {
                    throw new Exception("Interface repeats in an implement clause of an interface.");
                }
            }
        }

        return extendNodes;
    }

    public List <Referenceable> declare(ClassDecl classDecl, ScopeEnvironment underEnv) {
        List <Referenceable> result = new ArrayList <Referenceable>(){};
        for (String key : underEnv.localDecls.keySet()){
            if (!(underEnv.localDecls.get(key) instanceof ConstructorList)) {
                result.add(underEnv.localDecls.get(key));
            }

        }
        return result;
    }

//    public List <Referenceable> declare(InterfaceDecl interfaceDecl, ScopeEnvironment underEnv) {
//        List <Referenceable> result = new ArrayList <Referenceable>(){};
//
//    }

    public List <Referenceable> inherit (ClassDecl classDecl, ScopeEnvironment underEnv) throws Exception {
        List <Pair<Referenceable, ScopeEnvironment>> extendNodes = checkExtendNode(classDecl, classDecl, underEnv);

        List <Referenceable> result = new ArrayList <Referenceable>(){};
        for (Pair<Referenceable, ScopeEnvironment> pair : extendNodes) {
            if (pair.first instanceof ClassDecl) {
                ClassDecl classPair = (ClassDecl) pair.first;
                result.addAll(declare(classPair, pair.second));
            } else if (pair.first instanceof InterfaceDecl) {
                // class should extends all class
            }
        }

        extendNodes = checkImplementNode(classDecl, underEnv);

        //for (Pair(Referenceable ))

        return result;
    }



}