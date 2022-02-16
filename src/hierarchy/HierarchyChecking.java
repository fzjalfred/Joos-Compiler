package hierarchy;

import java.util.*;
import java.io.*;
import ast.*;
import type.*;
import utils.*;
import lexer.*;

public class HierarchyChecking {
    public List <Referenceable> generalBaseObjectClass;
    // public List <Referenceable> generalBaseMethodInterface;
    public Map<ASTNode, List<Referenceable>> declareMap;
    // <ClassDecl, MethodList|ConstructorList|fieldDecl|AbstractMethodList>
    public Map<ASTNode, List<Referenceable>> parentMap;
    public Map<ASTNode, List<Referenceable>> inheritMap;

    public HierarchyChecking() {
        this.generalBaseObjectClass = new ArrayList <Referenceable>(){};
        // this.generalBaseMethodInterface = new ArrayList <Referenceable>(){};
        this.declareMap =  new HashMap<ASTNode, List<Referenceable>>();
        this.parentMap = new HashMap<ASTNode, List<Referenceable>>();
        this.inheritMap = new HashMap<ASTNode, List<Referenceable>>();
    }

    public void createInheritanceMap(){
        for (ASTNode node : parentMap.keySet()){
            // System.out.println("");
            List <Referenceable> inherited = new ArrayList <Referenceable>(){};
            inherited.addAll(generalBaseObjectClass);
            for (Referenceable parentNode: parentMap.get(node)) {
                inherited.addAll(declareMap.get((ASTNode)parentNode));
            }
//            for (Referenceable ref : inherited){
//                System.out.println(ref.toString());
//            }
            inheritMap.put(node, inherited);
        }
    }

    public void checkClassHierary() throws Exception {
        checkContainSameSigDiffReturn();
        checkAbstrictMethod();
        checkStaticMethod();
        checkReplaceType();
        checkPublicMethod();
        checkFinalMethod();
    }

    private Map<ASTNode, ASTNode> get_replace(ASTNode T) {
        Map<ASTNode, ASTNode> res = new HashMap<ASTNode, ASTNode> ();
        List<Referenceable> base_all_method_list = inheritMap.get(T);
        if (base_all_method_list == null) {
            return new HashMap<ASTNode, ASTNode> ();
        }
        Map<List<String>, MethodDecl> match_buff = new HashMap<List<String>, MethodDecl>();
        for (int l = 0; l<inheritMap.get(T).size(); l++) {
            if (inheritMap.get(T).get(l) instanceof MethodList) {
                List<MethodDecl> method_list = ((MethodList) inheritMap.get(T).get(l)).methods;
                for (MethodDecl i: method_list) {
                    List<String> signature = get_sig(i);
                    match_buff.put(signature, i);
                }
            }
        }
        for (int l = 0; l<declareMap.get(T).size(); l++) {
            if (declareMap.get(T).get(l) instanceof MethodList) {
                List<MethodDecl> method_list = ((MethodList) declareMap.get(T).get(l)).methods;
                for (MethodDecl i: method_list) {
                    List<String> signature = get_sig(i);
                    //System.out.println(signature);
                    if (match_buff.containsKey(signature)) {
                        res.put(i, match_buff.get(signature));
                    }
                }
            }
        }
        return res;
    }

    private String get_type(MethodDecl method_decl) {
        // method_decl ->method_header -> type -> primitive_type|... -> 
        return method_decl.children.get(0).children.get(1).value;
    }

    private void checkReplaceType() throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                Map<ASTNode, ASTNode> replace_set = get_replace(T);
                for (ASTNode m: replace_set.keySet()) {
                    ASTNode m_base = replace_set.get(m);
                    if (get_type((MethodDecl)m) != get_type((MethodDecl)m_base)) {
                        throw new Exception("A method must not replace a method with a different return type.");
                    }
                }
            }
            
        }
    }

    public void checkPublicMethod() throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                Map<ASTNode, ASTNode> replace_set = get_replace(T);
                for (ASTNode m: replace_set.keySet()) {
                    ASTNode m_base = replace_set.get(m);
                    if (get_mods(m_base.children.get(0).children.get(0)).contains("public") && !(get_mods(m.children.get(0).children.get(0)).contains("public"))) {
                        throw new Exception("A protected method must not replace a public method.");
                    }
                }
            }
        }
    }

    public void checkFinalMethod() throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                Map<ASTNode, ASTNode> replace_set = get_replace(T);
                for (ASTNode m: replace_set.keySet()) {
                    ASTNode m_base = replace_set.get(m);
                    if (!get_mods(m_base.children.get(0).children.get(0)).contains("final") ) {
                        throw new Exception("A method must not replace a final method.");
                    }
                }
            }
        }
    }

    public void checkStaticMethod() throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                Map<ASTNode, ASTNode> replace_set = get_replace(T);
                for (ASTNode m: replace_set.keySet()) {
                    ASTNode m_base = replace_set.get(m);
                    // System.out.println(m.children.get(0).children.get(2).children.get(0));
                    // System.out.println(m_base.children.get(0).children.get(2).children.get(0));
                    // System.out.println(get_mods(m.children.get(0).children.get(0)).contains("static"));
                    // System.out.println("-----------");
                    // System.out.println(get_mods(m_base.children.get(0).children.get(0)));
                    // MethodDecl -> headr -> modifiers
                    if (get_mods(m_base.children.get(0).children.get(0)).contains("static") && !(get_mods(m.children.get(0).children.get(0)).contains("static"))) {
                        throw new Exception("A nonstatic method must not replace a static method");
                    }
                }
            }
        }
    }

    public List<String> get_mods(ASTNode modifiers){
        assert (modifiers instanceof Modifiers);
        List<String> res = new ArrayList<String>();
        
        for(ASTNode modifier: modifiers.children) {
            res.add(modifier.value);
        }
        return res;
    }
    
    public List<String> get_sig(ASTNode method_decl){
        
        assert (method_decl instanceof MethodDecl);
        List<String> res = new ArrayList<String>();
        // MethodDecl ->MethodHeader->method_declarator->ID->value
        res.add(method_decl.children.get(0).children.get(2).children.get(0).value);
        if (method_decl.children.get(0).children.get(2).children.get(1) == null) {
            return res;
        }
        // ->method_header->method_declarator->parameter_list
        ParameterList parameter_list = (ParameterList)method_decl.children.get(0).children.get(2).children.get(1);
        List<Parameter> paras = parameter_list.getParams();
        for (Parameter i: paras) {
            res.add(i.children.get(0).toString());
        }
        return res;
    }

    // first two string will be return type and ID.
    public List<String> get_full_sig(ASTNode method_decl){
        
        assert (method_decl instanceof MethodDecl);
        List<String> res = new ArrayList<String>();
        // MethodDecl ->MethodHeader->type->value
        res.add(method_decl.children.get(0).children.get(1).value);
        // MethodDecl ->MethodHeader->method_declarator->ID->value
        res.add(method_decl.children.get(0).children.get(2).children.get(0).value);
        if (method_decl.children.get(0).children.get(2).children.get(1) == null) {
            return res;
        }
        // ->method_header->method_declarator->parameter_list
        ParameterList parameter_list = (ParameterList)method_decl.children.get(0).children.get(2).children.get(1);
        List<Parameter> paras = parameter_list.getParams();
        for (Parameter i: paras) {
            res.add(i.children.get(0).toString());
        }
        return res;
    }

    public void checkNoDuplicateMethod() throws Exception {
        // T: class_decl
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (declareMap.get(T).get(l) instanceof MethodList) {
                    MethodList method_list = (MethodList) declareMap.get(T).get(l);
                    method_list.checkAmbiguousMethodDecl(method_list.methods.get(0));
                }
            }
        }
    }

    public void checkContainSameSigDiffReturn() throws Exception {
        // T: class_decl
        // declare + inherit = contains
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (declareMap.get(T).get(l) instanceof MethodList) {
                    MethodList method_list = (MethodList) declareMap.get(T).get(l);
                    for (int i = 0; i<method_list.methods.size(); i++) {
                        for (int j = i; j<method_list.methods.size(); j++) {
                            // check types
                            MethodDecl mh1 = (MethodDecl) method_list.methods.get(i);
                            MethodDecl mh2 = (MethodDecl) method_list.methods.get(j);
                            if (get_sig(mh1) == get_sig(mh1)){
                                if (mh1.children.get(0).children.get(1).children.get(0).toString() != mh2.children.get(0).children.get(1).children.get(0).toString()) {
                                    throw new Exception("A class or interface must not contain (declare or inherit) two methods with the same signature but different return types");
                                }
                            }
                        }
                    }
                }
            }
        }
        for (ASTNode T: inheritMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (declareMap.get(T).get(l) instanceof MethodList) {
                    MethodList method_list = (MethodList) declareMap.get(T).get(l);
                    for (int i = 0; i<method_list.methods.size(); i++) {
                        for (int j = i; j<method_list.methods.size(); j++) {
                            // check types
                            MethodDecl mh1 = (MethodDecl) method_list.methods.get(i);
                            MethodDecl mh2 = (MethodDecl) method_list.methods.get(j);
                            if (get_sig(mh1) == get_sig(mh1)){
                                // method_decl -> method_header -> type -> numeric_type
                                if (mh1.children.get(0).children.get(1).children.get(0).toString() != mh2.children.get(0).children.get(1).children.get(0).toString()) {
                                    throw new Exception("A class or interface must not contain (declare or inherit) two methods with the same signature but different return types");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkAbstrictMethod() throws Exception {
        // declareMap + inheritMap = contains
        for (ASTNode T: declareMap.keySet()) {
            List<String> T_mods = get_mods(T.children.get(0));
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (declareMap.get(T).get(l) instanceof MethodList) {
                    List<MethodDecl> method_decl = ((MethodList)declareMap.get(T).get(l)).methods;
                    for (MethodDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i.children.get(0).children.get(0));
                        if (m.contains("abstract")) {
                            if (!T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
                if (declareMap.get(T).get(l) instanceof ConstructorList) {
                    List<ConstructorDecl> method_decl = ((ConstructorList)declareMap.get(T).get(l)).cons;
                    for (ConstructorDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i.children.get(0).children.get(0));
                        if (m.contains("abstract")) {
                            if (!T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
            }
        }
        for (ASTNode T: inheritMap.keySet()) {
            List<String> T_mods = get_mods(T.children.get(0));
            for (int l = 0; l<inheritMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (inheritMap.get(T).get(l) instanceof MethodList) {
                    List<MethodDecl> method_decl = ((MethodList)inheritMap.get(T).get(l)).methods;
                    for (MethodDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i.children.get(0).children.get(0));
                        if (m.contains("abstract")) {
                            if (!T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
                if (inheritMap.get(T).get(l) instanceof ConstructorList) {
                    List<ConstructorDecl> method_decl = ((ConstructorList)inheritMap.get(T).get(l)).cons;
                    for (ConstructorDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i.children.get(0).children.get(0));
                        if (m.contains("abstract")) {
                            if (!T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkRootEnvironment(RootEnvironment env) throws Exception{
        for (String packKey : env.packageScopes.keySet()) {
            ScopeEnvironment packScope = (ScopeEnvironment) env.packageScopes.get(packKey);
            for (ASTNode compileKey : packScope.childScopes.keySet()) {
                checkCompilationUnitScope(packScope.childScopes.get(compileKey));
            }
        }
        createInheritanceMap();
        checkClassHierary();
    }

    public void checkCompilationUnitScope(ScopeEnvironment env) throws Exception{
        List<Pair<String, ASTNode>> nonImported = new ArrayList <Pair<String, ASTNode>>(){};


        for (String key : env.localDecls.keySet()){

            if (env.childScopes.containsKey(env.localDecls.get(key))) {
                nonImported.add(new Pair<String, ASTNode> (key, (ASTNode)env.localDecls.get(key)));
            }
        }

        for (Pair<String, ASTNode> node : nonImported) {
//            System.out.println("");
//            System.out.println(node.first);
            if (node.first.contains("java.lang.Object")) { // general base class
                ClassDecl classDecl = (ClassDecl) node.second;
                generalBaseObjectClass.addAll(declare(classDecl, env));
                continue; // No need to check base class correctness(?
            } else if (node.first.contains("java.lang") || node.first.contains("java.io")) {
                continue;
            }
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
        // System.out.println("");
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
            //if (!(env.localDecls.get(key) instanceof ConstructorList)) {
            //     System.out.println(key);
                result.add(env.localDecls.get(key));
            //}

        }
        return result;
    }

}