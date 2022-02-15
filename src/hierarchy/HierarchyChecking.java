package hierarchy;

import java.util.*;
import java.io.*;
import ast.*;
import type.*;
import utils.*;
import lexer.*;

public class HierarchyChecking {
    public List <Referenceable> generalBaseMethodClass;
    public List <Referenceable> generalBaseMethodInterface;
    public Map<ASTNode, List<Referenceable>> declareMap;
    public Map<ASTNode, List<Referenceable>> parentMap;
    public Map<Referenceable, List<String>> sigMap;
    public Map<ASTNode, List<Referenceable>> containMap;

    public HierarchyChecking() {
        this.generalBaseMethodClass = new ArrayList <Referenceable>(){};
        this.generalBaseMethodInterface = new ArrayList <Referenceable>(){};
        this.declareMap =  new HashMap<ASTNode, List<Referenceable>>();
        this.parentMap = new HashMap<ASTNode, List<Referenceable>>();
    }

    public void checkNoDuplicateMethod() throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int i = 0; i<declareMap.get(T).size(); i++) {
                for (int j = i; j<declareMap.get(T).size(); j++) {
                    if (sigMap.get(declareMap.get(T).get(i)) == sigMap.get(declareMap.get(T).get(j))){
                        throw new Exception("A class or interface must not declare two methods with the same signature (name and parameter types). (JLS 8.4, 9.4)");
                    }
                }
            }
        }
    }

    public void checkContainSameSigDiffReturn() throws Exception {
        for (ASTNode T: containMap.keySet()) {
            for (int i = 0; i<containMap.get(T).size(); i++) {
                for (int j = i; j<containMap.get(T).size(); j++) {
                    if (sigMap.get(containMap.get(T).get(i)) == sigMap.get(containMap.get(T).get(j))){
                        // check types
                        MethodHeader mh1 = (MethodHeader) containMap.get(T).get(i);
                        MethodHeader mh2 = (MethodHeader) containMap.get(T).get(j);
                        if (mh1.children.get(1).value == mh2.children.get(1).value) {
                            throw new Exception("A class or interface must not contain (declare or inherit) two methods with the same signature but different return types");
                        }
                    }
                }
            }
        }
    }

    public void checkAbstrictMethod() throws Exception {
        for (ASTNode T: containMap.keySet()) {
            for (int i = 0; i<containMap.get(T).size(); i++) {
                for (int j = i; j<containMap.get(T).size(); j++) {
                    if (sigMap.get(containMap.get(T).get(i)) == sigMap.get(containMap.get(T).get(j))){
                        // check types
                        MethodHeader mh1 = (MethodHeader) containMap.get(T).get(i);
                        MethodHeader mh2 = (MethodHeader) containMap.get(T).get(j);
                        if (mh1.children.get(1).value == mh2.children.get(1).value) {
                            throw new Exception("A class or interface must not contain (declare or inherit) two methods with the same signature but different return types");
                        }
                    }
                }
                List<String> method_mods;
                MethodHeader mh1 = (MethodHeader) containMap.get(T).get(i);
                method_mods = get_mods(mh1.children.get(0));
                List<String> T_mods = new ArrayList<String>();
                if (T instanceof ClassDecl || T instanceof InterfaceDecl ||
                T instanceof ConstructorDecl || T instanceof AbstractMethodDecl) {
                    T_mods = get_mods(T.children.get(0));
                }
                if (method_mods.contains("abstract") ^ T_mods.contains("abstract")) {
                    throw new Exception("A class that contains (declares or inherits) any abstract methods must be abstract.");
                }
            }
        }
    }

    public List<String> get_mods(ASTNode modifiers){
        assert (modifiers instanceof Modifiers);
        List<String> res = new ArrayList<String>();
        if (modifiers.children.size() == 1) {
            res.add(modifiers.children.get(0).value);
            return res;
        } else {
            res = get_mods(modifiers.children.get(0));
            res.add(modifiers.children.get(1).value);
            return res;
        }
    }



    public void checkRootEnvironment(RootEnvironment env) throws Exception{
        for (String packKey : env.packageScopes.keySet()) {
            ScopeEnvironment packScope = (ScopeEnvironment) env.packageScopes.get(packKey);
            for (ASTNode compileKey : packScope.childScopes.keySet()) {
                checkCompilationUnitScope(packScope.childScopes.get(compileKey));
            }
        }
    }

    public void checkCompilationUnitScope(ScopeEnvironment env) throws Exception{
        List<Pair<String, ASTNode>> nonImported = new ArrayList <Pair<String, ASTNode>>(){};


        for (String key : env.localDecls.keySet()){

            if (env.childScopes.containsKey(env.localDecls.get(key))) {
                nonImported.add(new Pair<String, ASTNode> (key, (ASTNode)env.localDecls.get(key)));
            }
        }

        for (Pair<String, ASTNode> node : nonImported) {
            if (node.first.contains("java.lang.")) { // general base class
                if (node.second instanceof ClassDecl) {
                    ClassDecl classDecl = (ClassDecl) node.second;
                    generalBaseMethodClass.addAll(declare(classDecl, env));
                } else if (node.second instanceof InterfaceDecl) {
                    InterfaceDecl interfaceDecl = (InterfaceDecl) node.second;
                    generalBaseMethodInterface.addAll(declare(interfaceDecl, env));
                }
                continue; // No need to check base class correctness(?
            }
//            System.out.println("");
//            System.out.println(node.first);
            if (node.second instanceof ClassDecl) {
                ClassDecl classDecl = (ClassDecl) node.second;
                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = checkExtendNode(classDecl, classDecl, env);
                checkImplementNode(classDecl, env);

                checkExtendDecl(classDecl, extendNodes);
                generateParentMap(node.second, extendNodes);

            } else if (node.second instanceof InterfaceDecl) {
                InterfaceDecl interfaceDecl = (InterfaceDecl) node.second;

                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = checkExtendNode(interfaceDecl, interfaceDecl, env);
                checkExtendDecl(interfaceDecl, extendNodes);
                generateParentMap(node.second, extendNodes);
            }
            Referenceable ref = (Referenceable) node.second;
            declareMap.put(node.second, declare(ref, env));
        }
    }

    public void generateParentMap(ASTNode node, List<Pair<Referenceable, ScopeEnvironment>> parents) {
        if (parentMap.containsKey(node)) {
            return;
        }
        // System.out.println("parent");
        List <Referenceable> extendNodes = new ArrayList <Referenceable>(){};
        int size = parents.size();
        for (int i = 0; i < size; i++) {
            boolean dup = false;
            for (int j = i + 1; j < size; j++) {
                if (parents.get(i).first == parents.get(j).first) {
                    dup = true;
                }
            }
            if (!dup) {
                // System.out.println(parents.get(i).first.toString());
                extendNodes.add(parents.get(i).first);
            }
        }
        parentMap.put(node, extendNodes);
        //System.out.println("");
    }


    private boolean ifContainModifier(ASTNode modifiers, String name){
        if (modifiers == null) return false;
        for (ASTNode n : modifiers.children){
            if (n.value == name) return true;
        }
        return false;
    }

    public void checkExtendDecl(ClassDecl classDecl, List<Pair<Referenceable, ScopeEnvironment>> parents) throws Exception {

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

    public void checkExtendDecl(InterfaceDecl interfaceDecl, List<Pair<Referenceable, ScopeEnvironment>> parents) throws Exception {
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


    public List<Pair<Referenceable, ScopeEnvironment>> checkExtendNode(ClassDecl original, ClassDecl classDecl, ScopeEnvironment underEnv) throws Exception{
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
            // System.out.println(underEnv.toString());
            throw new Exception("Acylic Extends in class");
        }

        ClassDecl classNode = (ClassDecl)found.first;

        extendNodes.add(found);
        extendNodes.addAll(checkExtendNode(original, classNode, found.second));
        return extendNodes;
    }

    public List<Pair<Referenceable, ScopeEnvironment>> checkExtendNode(InterfaceDecl original, InterfaceDecl interfaceDecl, ScopeEnvironment underEnv) throws Exception {
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

    public List<Pair<Referenceable, ScopeEnvironment>> checkImplementNode(ClassDecl classDecl, ScopeEnvironment underEnv) throws Exception{
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

    public List <Referenceable> declare(Referenceable node, ScopeEnvironment underEnv) {
        List <Referenceable> result = new ArrayList <Referenceable>(){};
        ScopeEnvironment env = underEnv.childScopes.get(node);
//        System.out.println("");
//        System.out.println(node.toString());
        for (String key : env.localDecls.keySet()){
            if (!(env.localDecls.get(key) instanceof ConstructorList)) {
                // System.out.println(key);
                result.add(env.localDecls.get(key));
            }

        }
        return result;
    }

}