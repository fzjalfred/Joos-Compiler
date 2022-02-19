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
    RootEnvironment env;
    public HierarchyChecking() {
        this.generalBaseObjectClass = new ArrayList <Referenceable>(){};
        // this.generalBaseMethodInterface = new ArrayList <Referenceable>(){};
        this.declareMap =  new HashMap<ASTNode, List<Referenceable>>();
        this.parentMap = new HashMap<ASTNode, List<Referenceable>>();
        this.inheritMap = new HashMap<ASTNode, List<Referenceable>>();
    }


    public void checkClassHierary(RootEnvironment env) throws Exception {
        this.env = env;
        checkContainSameSigDiffReturn(env);
        checkAbstrictMethod(env);
        checkStaticMethod(env);
        checkReplaceType(env);
        checkProtectedMethod(env);
        checkFinalMethod(env);
    }

    public String get_class_qualifed_name(ASTNode class_decl, RootEnvironment env) {
        ScopeEnvironment underenv = env.ASTNodeToScopes.get(class_decl);
        return underenv.prefix+"."+class_decl.children.get(1).value;
    }

    private Map<ASTNode, ASTNode> get_replace(ASTNode T) {
        Map<ASTNode, ASTNode> res = new HashMap<ASTNode, ASTNode> ();
        List<Referenceable> base_all_method_list = inheritMap.get(T);
        if (base_all_method_list == null) {
            return new HashMap<ASTNode, ASTNode> ();
        }
        // <List<String>, MethodDecl|AbstractMethodDecl>
        Map<List<String>, ASTNode> match_buff = new HashMap<List<String>, ASTNode>();
        for (int l = 0; l<inheritMap.get(T).size(); l++) {
            if (inheritMap.get(T).get(l) instanceof MethodList) {
                List<MethodDecl> method_list = ((MethodList) inheritMap.get(T).get(l)).methods;
                for (MethodDecl i: method_list) {
                    List<String> signature = get_sig(i);
                    match_buff.put(signature, i);
                }
            }
            if (inheritMap.get(T).get(l) instanceof AbstractMethodList) {
                List<AbstractMethodDecl> method_list = ((AbstractMethodList) inheritMap.get(T).get(l)).methods;
                for (AbstractMethodDecl i: method_list) {
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
                    //System.out.println(get_class_qualifed_name(T,env)+" declare: "+ signature);                
                    if (match_buff.containsKey(signature)) {
                        res.put(i, match_buff.get(signature));
                    }
                }
            }
            if (declareMap.get(T).get(l) instanceof AbstractMethodList) {
                List<AbstractMethodDecl> method_list = ((AbstractMethodList) declareMap.get(T).get(l)).methods;
                for (AbstractMethodDecl i: method_list) {
                    List<String> signature = get_sig(i);
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
        // if (method_decl.children.get(0).children.get(1).children.isEmpty()) {
        //     return method_decl.children.get(0).children.get(1).value;
        // }
        ASTNode tmp = method_decl.children.get(0).children.get(1);
        while (!tmp.children.isEmpty()) {
            tmp = tmp.children.get(0);
        }
        return tmp.value;
    }
    private String get_type(AbstractMethodDecl method_decl) {
        // method_decl -> type -> primitive_type|... -> 
        // if (method_decl.children.get(0).children.get(1).children.isEmpty()) {
        //     return method_decl.children.get(0).children.get(1).value;
        // }
        ASTNode tmp = method_decl.children.get(1);
        if (tmp == null) {
            return "void";
        }
        while (!tmp.children.isEmpty()) {
            tmp = tmp.children.get(0);
        }
        return tmp.value;
    }
    private String get_type(ASTNode method_decl) {
        if (method_decl instanceof MethodDecl) {
            return get_type((MethodDecl)method_decl);
        }
        else {
            return get_type((AbstractMethodDecl)method_decl);
        }
    }

    private void checkReplaceType(RootEnvironment env) throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                Map<ASTNode, ASTNode> replace_set = get_replace(T);
                for (ASTNode m: replace_set.keySet()) {
                    ASTNode m_base = replace_set.get(m);
                    // System.out.println(get_full_sig(m));
                    // System.out.println(get_full_sig(m_base));
                    if (!get_type(m).equals(get_type(m_base))) {
                        throw new Exception("A method must not replace a method with a different return type.");
                    }
                }
            }
            
        }
    }

    public void checkProtectedMethod(RootEnvironment env) throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                Map<ASTNode, ASTNode> replace_set = get_replace(T);
                for (ASTNode m: replace_set.keySet()) {
                    ASTNode m_base = replace_set.get(m);
                    // System.out.println(get_class_qualifed_name(T,env) + get_full_sig(m)+ get_mods(m));
                    // System.out.println(get_class_qualifed_name(T,env) + get_full_sig(m_base)+ get_mods(m_base));
                    if (get_mods(m_base).contains("public") && (get_mods(m).contains("protected"))) {
                        throw new Exception("A protected method \'"+ get_sig(m).get(0) + "\' must not replace a public method.");
                    }
                    if (get_mods(m_base).contains("protected") && (get_mods(m).contains("public"))) {
                        throw new Exception("A protected method \'"+ get_sig(m).get(0) + "\' must not replace a public method.");
                    }
                }
            }
        }
    }

    public void checkFinalMethod(RootEnvironment env) throws Exception {
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                Map<ASTNode, ASTNode> replace_set = get_replace(T);
                for (ASTNode m: replace_set.keySet()) {
                    ASTNode m_base = replace_set.get(m);
                    if (get_mods(m_base).contains("final") ) {
                        throw new Exception("A method must not replace a final method.");
                    }
                }
            }
        }
    }

    public void checkStaticMethod(RootEnvironment env) throws Exception {
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
                    if (get_mods(m_base).contains("static") && !(get_mods(m).contains("static"))) {
                        throw new Exception("A nonstatic method \'"+ get_sig(m_base).get(0) + "\' must not replace a static method");
                    }
                    if (!(get_mods(m_base).contains("static")) && (get_mods(m).contains("static"))) {
                        throw new Exception(" A static method \'"+ get_sig(m_base).get(0) + "\' must not replace an instance method * ");
                    }
                }
            }
        }
    }

    public List<String> get_mods(Modifiers modifiers){
        List<String> res = new ArrayList<String>();
        
        for(ASTNode modifier: modifiers.children) {
            res.add(modifier.value);
        }
        return res;
    }
    public List<String> get_mods(MethodDecl modifiers){
        return get_mods(modifiers.children.get(0).children.get(0));
    }
    public List<String> get_mods(AbstractMethodDecl modifiers){
        return get_mods(modifiers.children.get(0));
    }
    public List<String> get_mods(ASTNode modifiers){
        if (modifiers instanceof MethodDecl) {
            return get_mods((Modifiers)modifiers.children.get(0).children.get(0));
        } else {
            return get_mods((Modifiers)modifiers.children.get(0));
        }
    }
    
    public List<String> get_sig(MethodDecl method_decl){
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
    public List<String> get_sig(AbstractMethodDecl method_decl){
        List<String> res = new ArrayList<String>();
        // MethodDecl->method_declarator->ID->value
        res.add(method_decl.children.get(2).children.get(0).value);
        if (method_decl.children.get(2).children.get(1) == null) {
            return res;
        }
        //->method_declarator->parameter_list
        ParameterList parameter_list = (ParameterList)method_decl.children.get(2).children.get(1);
        List<Parameter> paras = parameter_list.getParams();
        for (Parameter i: paras) {
            res.add(i.children.get(0).toString());
        }
        return res;
    }
    public List<String> get_sig(ASTNode method_decl){
        if (method_decl instanceof MethodDecl) {
            return get_sig((MethodDecl)method_decl);
        } else {
            return get_sig((AbstractMethodDecl)method_decl);
        }
    }
    
    public List<String> get_full_sig(MethodDecl method_decl){
        List<String> res = new ArrayList<String>();
        // MethodDecl ->MethodHeader->type->value
        res.add(get_type(method_decl));
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
    public List<String> get_full_sig(AbstractMethodDecl method_decl){
        List<String> res = new ArrayList<String>();
        // MethodDecl ->MethodHeader->type->value
        res.add(get_type(method_decl));
        // MethodDecl->method_declarator->ID->value
        res.add(method_decl.children.get(2).children.get(0).value);
        if (get_type(method_decl).equals("")) {
            System.out.println("AbstractMethodDecl");
            System.out.println(res.get(1));
        }
        if (method_decl.children.get(2).children.get(1) == null) {
            return res;
        }
        //->method_declarator->parameter_list
        ParameterList parameter_list = (ParameterList)method_decl.children.get(2).children.get(1);
        List<Parameter> paras = parameter_list.getParams();
        for (Parameter i: paras) {
            res.add(i.children.get(0).toString());
        }
        return res;
    }
    public List<String> get_full_sig(ASTNode method_decl){
        if (method_decl instanceof MethodDecl) {
            return get_full_sig((MethodDecl)method_decl);
        } else {
            return get_full_sig((AbstractMethodDecl)method_decl);
        }
    }
    

    public void checkNoDuplicateMethod(RootEnvironment env) throws Exception {
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

    public void checkContainSameSigDiffReturn(RootEnvironment env) throws Exception {
        // T: class_decl
        // declare + inherit = contains
        Map<List<String>, ASTNode> checkbuff = new HashMap<List<String>, ASTNode>();
        for (ASTNode T: declareMap.keySet()) {
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (declareMap.get(T).get(l) instanceof MethodList) {
                    MethodList method_list = (MethodList) declareMap.get(T).get(l);
                    for (int i = 0; i<method_list.methods.size(); i++) {
                        MethodDecl mh1 = (MethodDecl) method_list.methods.get(i);
                        List<String> signature = get_sig(mh1);
                        signature.add(get_class_qualifed_name(T, env));
                        if (checkbuff.containsKey(signature)) {
                            ASTNode mh2 = (ASTNode)checkbuff.get(signature);
                            if (!get_type(mh1).equals(get_type(mh2))) {
                                throw new Exception("A class or interface \'"+ get_sig(mh1).get(0) +"\' must not contain (declare or inherit) two methods with the same signature but different return types");
                            }
                        } else {
                            checkbuff.put(signature, mh1);
                        }
                    }
                }
                if (declareMap.get(T).get(l) instanceof AbstractMethodList) {
                    AbstractMethodList method_list = (AbstractMethodList) declareMap.get(T).get(l);
                    for (int i = 0; i<method_list.methods.size(); i++) {
                        AbstractMethodDecl mh1 = (AbstractMethodDecl) method_list.methods.get(i);
                        List<String> signature = get_sig(mh1);
                        signature.add(get_class_qualifed_name(T, env));
                        if (checkbuff.containsKey(signature)) {
                            ASTNode mh2 = (ASTNode) checkbuff.get(signature);
                            if (!get_type(mh1).equals(get_type(mh2))) {
                                throw new Exception("A class or interface \'"+ get_sig(mh1).get(0) +"\' must not contain (declare or inherit) two methods with the same signature but different return types");
                            }
                        } else {
                            checkbuff.put(signature, mh1);
                        }
                    }
                }
            }
        }
        for (ASTNode T: inheritMap.keySet()) {
            for (int l = 0; l<inheritMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (inheritMap.get(T).get(l) instanceof MethodList) {
                    MethodList method_list = (MethodList) inheritMap.get(T).get(l);
                    for (int i = 0; i<method_list.methods.size(); i++) {
                        MethodDecl mh1 = (MethodDecl) method_list.methods.get(i);
                        List<String> signature = get_sig(mh1);
                        signature.add(get_class_qualifed_name(T, env));
                        if (checkbuff.containsKey(signature)) {
                            ASTNode mh2 = (ASTNode)checkbuff.get(signature);
                            if (!get_type(mh1).equals(get_type(mh2))) {
                                throw new Exception("A class or interface \'"+ get_sig(mh1).get(0) +"\' must not contain (declare or inherit) two methods with the same signature but different return types");
                            }
                        } else {
                            checkbuff.put(signature, mh1);
                        }
                    }
                }
                if (inheritMap.get(T).get(l) instanceof AbstractMethodList) {
                    AbstractMethodList method_list = (AbstractMethodList) inheritMap.get(T).get(l);
                    for (int i = 0; i<method_list.methods.size(); i++) {
                        AbstractMethodDecl mh1 = (AbstractMethodDecl) method_list.methods.get(i);
                        List<String> signature = get_sig(mh1);
                        signature.add(get_class_qualifed_name(T, env));
                        if (checkbuff.containsKey(signature)) {
                            ASTNode mh2 = (ASTNode)checkbuff.get(signature);
                            if (!get_type(mh1).equals(get_type(mh2))) {
                                throw new Exception("A class or interface \'"+ get_sig(mh1).get(0) +"\' must not contain (declare or inherit) two methods with the same signature but different return types");
                            }
                        } else {
                            checkbuff.put(signature, mh1);
                        }
                    }
                }
            }
        }
    }

    public void checkAbstrictMethod(RootEnvironment env) throws Exception {
        // declareMap + inheritMap = contains
        for (ASTNode T: declareMap.keySet()) {
            List<String> T_mods = get_mods((Modifiers)T.children.get(0));
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (declareMap.get(T).get(l) instanceof MethodList) {
                    List<MethodDecl> method_decl = ((MethodList)declareMap.get(T).get(l)).methods;
                    for (MethodDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods((Modifiers)i.children.get(0).children.get(0));
                        // System.out.println("===========");
                        // System.out.println(m);
                        // System.out.println(get_full_sig(i));
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
        checkClassHierary(env);
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
            }
//            } else if (node.first.contains("java.lang") || node.first.contains("java.io")) {
//                continue;
//            }
            if (node.second instanceof ClassDecl) {
                ClassDecl classDecl = (ClassDecl) node.second;
                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
                extendNodes = checkExtendNode(classDecl, classDecl, env, extendNodes);
                checkExtendDecl(classDecl, extendNodes);

                // extendNodes.addAll(checkImplementNode(classDecl, env));
//                System.out.println("class and parent: " + classDecl.getName());
//                for (Pair<Referenceable, ScopeEnvironment> pair : extendNodes) {
//                    if (pair.first instanceof ClassDecl) {
//                        System.out.println(((ClassDecl)pair.first).getName());
//                    } else if (pair.first instanceof InterfaceDecl) {
//                        System.out.println(((InterfaceDecl)pair.first).getName());
//                    }
//                }
                generateParentMap(node.second, extendNodes);

            } else if (node.second instanceof InterfaceDecl) {
                InterfaceDecl interfaceDecl = (InterfaceDecl) node.second;

                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
                extendNodes = checkExtendNode(interfaceDecl, interfaceDecl, env, extendNodes);
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
                    throw new Exception("Acyclic class name or duplicated name: "+ myName.value+ " "+ name.value);
                }
                if (ifContainModifier(modifiers, "final")) {
                    throw new Exception("Cannot Extend a final class"+ myName.value+ " "+ name.value );
                }
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
                    throw new Exception("Acyclic interface name or duplicated name"+ myName.value+ " "+ name.value);
                }
            } else {
                throw new Exception("Interface can only extend an interface");
            }
        }
    }


    public List<Pair<Referenceable, ScopeEnvironment>> checkExtendNode(ClassDecl original, ClassDecl classDecl, ScopeEnvironment underEnv,
                                                                       List<Pair<Referenceable, ScopeEnvironment>> extendNodes) throws Exception{
        List<ASTNode> children = classDecl.children;
        Super superNode = null;
        for (ASTNode node : children){
            if (node instanceof Super) {
                superNode = (Super) node;
                break;
            }
        }
//        System.out.println("enter: " + original.getName());
        if (superNode == null) {
//            System.out.println("quit");
//            System.out.println("");
            extendNodes.addAll(checkImplementNode(classDecl, underEnv));
            return extendNodes;
        }

        Name extendName = (Name) ((ClassOrInterfaceType)superNode.children.get(0)).getName();
//        if (extendNodes == null) {
//            extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
//        }

        Pair<Referenceable, ScopeEnvironment> found;

        int size = extendName.children.size();
        if (size == 1) { // simple name
            Token id = (Token) extendName.children.get(0);

            if (id.value.equals(original.getName())){
                throw new Exception("repeated name under the same environment, might be self dependency: " + original.getName());
            }
            found = underEnv.lookupNameAndEnv(id);
        } else { // qualified name
            found = underEnv.lookupNameAndEnv(extendName);
        }

        if (found == null || found.first == null || found.second == null) {
            return extendNodes;
        }
        if (!(found.first instanceof ClassDecl)) {
            throw new Exception("Class can only extends a class" + classDecl.getName());
        }

        // checking Acylic
        if (found.first == original) {
            // System.out.println(underEnv.toString());
            throw new Exception("Acylic Extends in class with the original node "+ classDecl.getName());
        }
//        System.out.println("original: " + original.getName());
//        System.out.println("new: " + ((ClassDecl)found.first).getName());
        if (extendNodes != null) {
            for (Pair<Referenceable, ScopeEnvironment> inNode : extendNodes) {
//                System.out.println(((ClassDecl)found.first).getName() + " " + ((ClassDecl)inNode.first).getName());
                if (found.first == inNode.first) {
                    throw new Exception("Acylic Extends in class for: "+ ((ClassDecl)found.first).getName() + " " + ((ClassDecl)inNode.first).getName());
                }
            }
        }

        ClassDecl classNode = (ClassDecl)found.first;

        extendNodes.add(found);

        extendNodes.addAll(checkExtendNode(original, classNode, found.second, extendNodes));

        // check implement nodes
        extendNodes.addAll(checkImplementNode(classDecl, underEnv));
        return extendNodes;
    }

    public List<Pair<Referenceable, ScopeEnvironment>> checkExtendNode(InterfaceDecl original, InterfaceDecl interfaceDecl, ScopeEnvironment underEnv,
                                                                       List <Pair<Referenceable, ScopeEnvironment>> extendNodes) throws Exception {
        List <ASTNode> children = interfaceDecl.children;
        ExtendsInterfaces extendsInterfaces = null;
        for (ASTNode node : children) {
            if (node instanceof ExtendsInterfaces) {
                extendsInterfaces = (ExtendsInterfaces) node;
                break;
            }
        }
        if (extendsInterfaces == null) {
            return extendNodes;
        }

        List <InterfaceDecl> sameClauseNode = new ArrayList <InterfaceDecl>(){};
        List<List <Pair<Referenceable, ScopeEnvironment>>> extendsNodeBranch = new ArrayList<List <Pair<Referenceable, ScopeEnvironment>>> (){};
        for (ASTNode node : extendsInterfaces.children) {

            Pair<Referenceable, ScopeEnvironment> found;
            ClassOrInterfaceType interfaceType = (ClassOrInterfaceType) node;
            Name extendName = (Name) interfaceType.getName();

            int size = extendName.children.size();
            if (size == 1) { // simple name
                Token id = (Token) extendName.children.get(0);
                if (id.value.equals(original.getName())){
                    throw new Exception("repeated name under the same environment, might be self dependency: " + original.getName());
                }
                found = underEnv.lookupNameAndEnv(id);
            } else { // qualified name
                found = underEnv.lookupNameAndEnv(extendName);
            }

            if (found == null || found.first == null || found.second == null) {
                continue;
            }
            if (!(found.first instanceof InterfaceDecl)) {
                throw new Exception("interface can only extend an interface: " + ((InterfaceDecl)found.first).getName());
            }

            // checking Acylic
            if (found.first == original) {
                throw new Exception("Acyclic extend in interface for: " + ((InterfaceDecl)found.first).getName() + " " + original.getName());
            }

            if (extendNodes != null) {
                for (Pair<Referenceable, ScopeEnvironment> inNode : extendNodes) {
                    if (found.first == inNode.first) {
                        throw new Exception("Acylic Extends in class for: "+ ((InterfaceDecl)found.first).getName() + " " +
                                ((InterfaceDecl)inNode.first).getName());
                    }
                }
            }

            InterfaceDecl parentInterface = (InterfaceDecl) found.first;
            List <Pair<Referenceable, ScopeEnvironment>> currentBranch = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
            currentBranch.add(found);
//            System.out.println("extends " + parentInterface.getName());
            currentBranch.addAll(extendNodes);
//            for (Pair<Referenceable, ScopeEnvironment> pair : currentBranch) {
//                System.out.println("in extends: " + (((InterfaceDecl)pair.first).getName()));
//            }

            extendsNodeBranch.add(checkExtendNode(original, parentInterface, found.second, currentBranch));

            sameClauseNode.add(parentInterface);
        }

        for (List <Pair<Referenceable, ScopeEnvironment>> branch : extendsNodeBranch) {
            extendNodes.addAll(branch);
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
        List<List <Pair<Referenceable, ScopeEnvironment>>> extendsNodeBranch = new ArrayList<List <Pair<Referenceable, ScopeEnvironment>>> (){};
        List <InterfaceDecl> sameClauseNode = new ArrayList <InterfaceDecl>(){};
        for (ASTNode node : typeList.children) {
            Pair<Referenceable, ScopeEnvironment> found;
            ClassOrInterfaceType interfaceType = (ClassOrInterfaceType) node;
            Name extendName = (Name) interfaceType.getName();

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
            List <Pair<Referenceable, ScopeEnvironment>> currentBranch = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};

//            System.out.println("implement " + interfaceNode.getName());
            sameClauseNode.add(interfaceNode);
            currentBranch.add(found);
//            for (Pair<Referenceable, ScopeEnvironment> pair : currentBranch) {
//                System.out.println("in implements: " + (((InterfaceDecl)pair.first).getName()));
//            }
            extendsNodeBranch.add(checkExtendNode(interfaceNode, interfaceNode, found.second, currentBranch));
        }

        for (List <Pair<Referenceable, ScopeEnvironment>> branch : extendsNodeBranch) {
            extendNodes.addAll(branch);
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
//        System.out.println("Declare");
//        if (node instanceof ClassDecl) {
//            System.out.println(((ClassDecl)node).getName());
//        } else {
//            System.out.println(((InterfaceDecl)node).getName());
//        }
        for (String key : env.localDecls.keySet()){
//                 System.out.println(key);
                result.add(env.localDecls.get(key));
        }
        return result;
    }

    public void createInheritanceMap(){
        for (ASTNode node : parentMap.keySet()){
            List <Referenceable> inherited = new ArrayList <Referenceable>(){};
//            System.out.println("");
//            if (node instanceof ClassDecl) {
//                System.out.println(((ClassDecl)node).getName());
//            } else {
//                System.out.println(((InterfaceDecl)node).getName());
//            }
//
//            System.out.println("children");
            for (Referenceable parentNode: parentMap.get(node)) {
//                System.out.println("super" + parentNode.toString());
                List<Referenceable> adding = declareMap.get((ASTNode)parentNode);
                if (adding != null) {
//                    for (Referenceable ref : adding) {
//                        System.out.println("adding" + ref.toString());
//                    }
                    inherited.addAll(declareMap.get((ASTNode)parentNode));
                }

            }
            if (node instanceof InterfaceDecl && parentMap.get(node).size() == 0) {
                inherited.addAll(generalBaseObjectClass);
            }
//            System.out.println("after");
//            for (Referenceable ref : inherited){
//                System.out.println(ref.toString());
//            }
            inheritMap.put(node, inherited);
        }
    }


}