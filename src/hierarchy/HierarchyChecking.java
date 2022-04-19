package hierarchy;

import java.util.*;

import java.io.*;
import ast.*;
import tir.src.joosc.ir.ast.Node;
import type.*;
import utils.*;
import lexer.*;

public class HierarchyChecking {
    public ASTNode objectClass;
    public List <Referenceable> generalBaseObjectClass;
    // public List <Referenceable> generalBaseMethodInterface;
    public Map<ASTNode, List<Referenceable>> declareMap;
    // <ClassDecl, MethodList|ConstructorList|fieldDecl|AbstractMethodList>
    public Map<ASTNode, List<Referenceable>> parentMap;
    public Map<ASTNode, List<Referenceable>> directParentMap;
    public Map<ASTNode, List<Referenceable>> inheritMap;
    


    //rebuild map for A3
    public Map<ASTNode, Map<String, List<ASTNode>>> inheritMapRe;
    public Map<ASTNode, Map<String, List<ASTNode>>> declareMapRe;
    public Map<ASTNode, Map<String, List<ASTNode>>> containMap;


    RootEnvironment env;
    public HierarchyChecking() {
        this.generalBaseObjectClass = new ArrayList <Referenceable>(){};
        // this.generalBaseMethodInterface = new ArrayList <Referenceable>(){};
        this.declareMap =  new HashMap<ASTNode, List<Referenceable>>();
        this.parentMap = new HashMap<ASTNode, List<Referenceable>>();
        this.directParentMap = new HashMap<ASTNode, List<Referenceable>>();
        this.inheritMap = new HashMap<ASTNode, List<Referenceable>>();
    }

    public void rebuildMaps() {
        declareMapRe = new HashMap<ASTNode, Map<String, List<ASTNode>>>();

        
        for(ASTNode T: declareMap.keySet()) {
            Map<String, List<ASTNode>> inclass_map = new HashMap<String, List<ASTNode>>();
            for (Referenceable l: declareMap.get(T)) {
                if (l instanceof MethodList){
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    for (MethodDecl single: ((MethodList)l).methods) {
                        method_lst_buff.add(single);
                    }
                    inclass_map.put(((MethodList)l).getSimpleName(), method_lst_buff);
                }
                if (l instanceof AbstractMethodList){
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    for (AbstractMethodDecl single: ((AbstractMethodList)l).methods) {
                        method_lst_buff.add(single);
                    }
                    inclass_map.put(((AbstractMethodList)l).getSimpleName(), method_lst_buff);
                }
                if (l instanceof FieldDecl){
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    method_lst_buff.add((FieldDecl)l);
                    inclass_map.put(((FieldDecl)l).getFirstVarName(), method_lst_buff);
                }
                if (l instanceof ConstructorList) {
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    for (ConstructorDecl single: ((ConstructorList)l).cons) {
                        method_lst_buff.add(single);
                    }
                    inclass_map.put(((ConstructorList)l).getSimpleName(), method_lst_buff);
                }
            }
            declareMapRe.put(T, inclass_map);
        }
        inheritMapRe = new HashMap<ASTNode, Map<String, List<ASTNode>>>();
        for(ASTNode T: inheritMap.keySet()) {
            Integer itable_offset_counter = 0;
            Map<String, List<ASTNode>> inclass_map = new HashMap<String, List<ASTNode>>();
            for (Referenceable l: inheritMap.get(T)) {
                if (l instanceof MethodList){
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    for (MethodDecl single: ((MethodList)l).methods) {
                        method_lst_buff.add(single);
                    }
                    inclass_map.put(((MethodList)l).getSimpleName(), method_lst_buff);
                }
                if (l instanceof AbstractMethodList){
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    for (AbstractMethodDecl single: ((AbstractMethodList)l).methods) {
                        method_lst_buff.add(single);
                    }
                    inclass_map.put(((AbstractMethodList)l).getSimpleName(), method_lst_buff);
                    
                    // interfaceMethodMap has all interface's method for itable.
                    System.out.println("======= interfaceMethodMap put ======");
                    System.out.println(((ClassDecl)T).getName());
                    for (ASTNode i:method_lst_buff) {
                        if (T instanceof ClassDecl) {
                            ClassDecl class_decl = (ClassDecl)T;
                            System.out.println(((AbstractMethodDecl)i).getName());
                            class_decl.interfaceMethodMap.put((AbstractMethodDecl)i, class_decl.itable_offset_counter);
                            class_decl.itable_offset_counter+=4;
                        } else if (T instanceof InterfaceDecl) {
                            InterfaceDecl class_decl = (InterfaceDecl)T;
                            class_decl.interfaceMethodMap.put((AbstractMethodDecl)i, class_decl.itable_offset_counter);
                            class_decl.itable_offset_counter+=4;
                        }
                    }
                    System.out.println("===================================");
                }
                if (l instanceof FieldDecl){
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    method_lst_buff.add((FieldDecl)l);
                    inclass_map.put(((FieldDecl)l).getFirstVarName(), method_lst_buff);
                }
                if (l instanceof ConstructorList) {
                    List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                    for (ConstructorDecl single: ((ConstructorList)l).cons) {
                        method_lst_buff.add(single);
                    }
                    inclass_map.put(((ConstructorList)l).getSimpleName(), method_lst_buff);
                } 
            }
            inheritMapRe.put(T, inclass_map);
        }
        createContainMap();
        /**DEBUG: Print inheritMapRe, declareMapRe, containMap */
        // for(ASTNode T: inheritMapRe.keySet()) {
        //     for (String name: inheritMapRe.get(T).keySet()) {
        //         System.out.println(name);
        //         for(ASTNode l: inheritMapRe.get(T).get(name)) {
        //             if (l instanceof MethodDecl){
        //                 System.out.println(get_full_sig(l));
        //             }
        //         }
        //     }
        // }
        // for(ASTNode T: declareMapRe.keySet()) {
        //     for (String name: declareMapRe.get(T).keySet()) {
        //         System.out.println(name);
        //         for(ASTNode l: declareMapRe.get(T).get(name)) {
        //             if (l instanceof MethodDecl){
        //                 System.out.println(get_full_sig(l));
        //             }
        //         }
        //     }
        // }
        // for(ASTNode T: containMap.keySet()) {
        //     for (String name: containMap.get(T).keySet()) {
        //         System.out.println(name);
        //         for(ASTNode l: containMap.get(T).get(name)) {
        //             if (l instanceof MethodDecl){
        //                 System.out.println(get_full_sig(l));
        //             }
        //         }
        //     }
        // }
    }

    public void createContainMap() {
        containMap =  new HashMap<ASTNode, Map<String, List<ASTNode>>>();
        for (ASTNode T: declareMapRe.keySet()) {
            Map<String, List<ASTNode>> inclass_map = new HashMap<String, List<ASTNode>>();
            for (String name: declareMapRe.get(T).keySet()) {
                List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                for (ASTNode e: declareMapRe.get(T).get(name)){
                    method_lst_buff.add(e);
                }
                inclass_map.put(name, method_lst_buff);
            }
            containMap.put(T, inclass_map);
        }
        for (ASTNode T: inheritMapRe.keySet()) {
            Map<ASTNode, ASTNode> replace_set = get_replace(T);
            Map<String, List<ASTNode>> inclass_map = containMap.get(T);
            for (String name: inheritMapRe.get(T).keySet()) {
                List<ASTNode> method_lst_buff = new ArrayList<ASTNode> ();
                if (inclass_map.containsKey(name)) {
                    method_lst_buff = inclass_map.get(name);
                }
                for (ASTNode e: inheritMapRe.get(T).get(name)){
                    if (!replace_set.containsValue(e)) {
                        method_lst_buff.add(e);
                    }
                }
                inclass_map.put(name, method_lst_buff);
            }
            containMap.put(T, inclass_map);
        }

        // Integer itable_offset_counter = 0;
        // class containMap has all implemented methods including parents'method replacement.
        for (ASTNode T: containMap.keySet()) {
            if (T instanceof InterfaceDecl) {
                InterfaceDecl class_decl = (InterfaceDecl)T;
                class_decl.containMap = containMap.get(T);
                // // interfaceMethodMap
                // for (String method_name: containMap.get(T).keySet()) {
                //     List<ASTNode> methods = containMap.get(T).get(method_name);
                //     for (ASTNode i: methods) {
                //         if (i instanceof AbstractMethodDecl) {
                //             class_decl.interfaceMethodMap.put((AbstractMethodDecl)i, itable_offset_counter);
                //             itable_offset_counter+=4;
                //             System.out.println("itable_offset_counter put "+((AbstractMethodDecl)i).getName());
                //         }
                //     }
                // }
            } else if (T instanceof ClassDecl) {
                ClassDecl class_decl = (ClassDecl)T;
                class_decl.containMap = containMap.get(T);
            }
        }

        // interfaceMethodMap creation is in inheritMapRe!!!
    }


    public void checkClassHierary(RootEnvironment env) throws Exception {
        checkContainSameSigDiffReturn(env);
        checkAbstractMethod(env);
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
        Type type = method_decl.getMethodHeader().getType();
        if (type == null) {
            return "void";
        }
        return type.getNameString();
    }
    private String get_type(AbstractMethodDecl method_decl) {
        Type type = method_decl.getType();
        if (type == null) {
            return "void";
        }
        return type.getNameString();
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
                    if (get_mods(m_base).contains("public") && (get_mods(m).contains("protected"))) {
                        throw new Exception("A protected method \'"+ get_sig(m).get(0) + "\' must not replace a public method.");
                    }
                    // if (get_mods(m_base).contains("protected") && (get_mods(m).contains("public") && m instanceof AbstractMethodDecl)) {
                    //     throw new Exception("A protected method \'"+ get_sig(m).get(0) + "\' must not replace a public method.");
                    // }
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
                    //System.out.println(get_full_sig((MethodDecl)m_base));
                    if (get_mods(m_base).contains("final") ) {
                        throw new Exception("A method \'"+ get_sig(m).get(0) + "\' must not replace a final method.");
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
        return get_mods((Modifiers)modifiers.children.get(0).children.get(0));
    }
    public List<String> get_mods(AbstractMethodDecl modifiers){
        return get_mods((Modifiers)modifiers.children.get(0));
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
            if (i.getType() instanceof ClassOrInterfaceType) {
                Referenceable class_decl_of_param = ((ClassOrInterfaceType)i.getType()).typeDecl;
                String qualified_name = "";
                if (class_decl_of_param instanceof ClassDecl) {
                    qualified_name = get_class_qualifed_name((ClassDecl)class_decl_of_param, env);
                }
                if (class_decl_of_param instanceof InterfaceDecl) {
                    qualified_name = get_class_qualifed_name((InterfaceDecl)class_decl_of_param, env);
                }
                res.add(qualified_name);
            } else {
                res.add(i.children.get(0).toString());
            }
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
            if (i.getType() instanceof ClassOrInterfaceType) {
                Referenceable class_decl_of_param = ((ClassOrInterfaceType)i.getType()).typeDecl;
                String qualified_name = "";
                if (class_decl_of_param instanceof ClassDecl) {
                    qualified_name = get_class_qualifed_name((ClassDecl)class_decl_of_param, env);
                }
                if (class_decl_of_param instanceof InterfaceDecl) {
                    qualified_name = get_class_qualifed_name((InterfaceDecl)class_decl_of_param, env);
                }
                res.add(qualified_name);
            } else {
                res.add(i.children.get(0).toString());
            }
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

    public void checkAbstractMethod(RootEnvironment env) throws Exception {
        // signature, Class_decl
        Map<List<String>, ASTNode> checkbuff = new HashMap<List<String>, ASTNode>();
        // declareMap + inheritMap = contains
        for (ASTNode T: declareMap.keySet()) {
            List<String> T_mods = get_mods((Modifiers)T.children.get(0));
            for (int l = 0; l<declareMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (declareMap.get(T).get(l) instanceof MethodList) {
                    List<MethodDecl> method_decl = ((MethodList)declareMap.get(T).get(l)).methods;
                    for (MethodDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i);
                        List<String> signature = get_sig(i);
                        signature.add(get_class_qualifed_name(T, env));
                        checkbuff.put(signature, T);
                        if (m.contains("abstract")) {
                            if (!T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class \'" + T.children.get(1).value +"\' that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
                if (declareMap.get(T).get(l) instanceof AbstractMethodList) {
                    List<AbstractMethodDecl> method_decl = ((AbstractMethodList)declareMap.get(T).get(l)).methods;
                    for (AbstractMethodDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i);
                        m.add("abstract");
                        List<String> signature = get_sig(i);
                        signature.add(get_class_qualifed_name(T, env));
                        checkbuff.put(signature, T);
                        if (m.contains("abstract")) {
                            if (!T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class \'" + T.children.get(1).value +"\' that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
            }
        }
        for (ASTNode T: inheritMap.keySet()) {
            List<String> T_mods = get_mods((Modifiers)T.children.get(0));
            for (int l = 0; l<inheritMap.get(T).size(); l++) {
                // MethodList|ConstructorList|fieldDecl|AbstractMethodList
                if (inheritMap.get(T).get(l) instanceof MethodList) {
                    List<MethodDecl> method_decl = ((MethodList)inheritMap.get(T).get(l)).methods;
                    for (MethodDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i);
                        List<String> signature = get_sig(i);
                        signature.add(get_class_qualifed_name(T, env));
                        if (m.contains("abstract")) {
                            Boolean isAbstractImplement = checkbuff.containsKey(signature) && (checkbuff.get(signature) == T);
                            if (!isAbstractImplement && !T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class \'" + T.children.get(1).value +"\' that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
                if (inheritMap.get(T).get(l) instanceof AbstractMethodList) {
                    List<AbstractMethodDecl> method_decl = ((AbstractMethodList)inheritMap.get(T).get(l)).methods;
                    for (AbstractMethodDecl i: method_decl) {
                        // method_decl -> method_header -> modifiers
                        List<String> m = get_mods(i);
                        m.add("abstract");
                        List<String> signature = get_sig(i);
                        signature.add(get_class_qualifed_name(T, env));
                        if (m.contains("abstract")) {
                            Boolean isAbstractImplement = checkbuff.containsKey(signature) && (checkbuff.get(signature) == T);
                            if (!isAbstractImplement && !T_mods.contains("abstract") && !(T instanceof InterfaceDecl)) {
                                throw new Exception("A class \'" + T.children.get(1).value +"\' that contains (declares or inherits) any abstract methods must be abstract.");
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkRootEnvironment(RootEnvironment env) throws Exception{
        this.env = env;
        for (String packKey : env.packageScopes.keySet()) {
            ScopeEnvironment packScope = (ScopeEnvironment) env.packageScopes.get(packKey);
            for (ASTNode compileKey : packScope.childScopes.keySet()) {
                checkCompilationUnitScope(packScope.childScopes.get(compileKey));
            }
        }
        createParentMap();
        printParent();
        createInheritanceMap();
//        System.out.println("===============direct parent====================");
//        printDirectParent();
//        System.out.println("===============parent=================");
//        printParent();;
//        System.out.println("===============declare=================");
//        printDeclare();
//        System.out.println("===============inherit=================");
//        printInherit();
        buildSubclasses();
        rebuildMaps();
        checkClassHierary(env);
    }

    public void buildSubclasses() {
        for (ASTNode node : parentMap.keySet()){
            if (node instanceof ClassDecl) {
                ClassDecl decl = (ClassDecl) node;
//                System.out.println(decl.getName());
                for (ASTNode otherNode : parentMap.keySet()){
                    if (!node.equals(otherNode) && otherNode instanceof ClassDecl
                    && parentMap.get(otherNode).contains(node)) {
                        decl.subclasses.add((ClassDecl) otherNode);
//                        System.out.println(((ClassDecl)otherNode).getName());
                    }
                }
//                System.out.println();
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
            directParentMap.put(node.second, new ArrayList <Referenceable>());
            if (node.first.contains("java.lang.Object")) { // general base class
                ClassDecl classDecl = (ClassDecl) node.second;
                generalBaseObjectClass.addAll(declare((Referenceable)classDecl, env));
                objectClass = node.second;
                declareMap.put(node.second, generalBaseObjectClass);
                continue; // No need to check base class correctness(?
            }

            if (node.second instanceof ClassDecl) {
                ClassDecl classDecl = (ClassDecl) node.second;
                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
                extendNodes = checkExtendNode(classDecl, classDecl, env, extendNodes);
                checkExtendDecl(classDecl, extendNodes);
                extendNodes.addAll(checkImplementNode(classDecl, env));

            } else if (node.second instanceof InterfaceDecl) {
                InterfaceDecl interfaceDecl = (InterfaceDecl) node.second;
                List<Pair<Referenceable, ScopeEnvironment>> extendNodes = new ArrayList <Pair<Referenceable, ScopeEnvironment>>(){};
                extendNodes = checkExtendNode(interfaceDecl, interfaceDecl, env, extendNodes);
                checkExtendDecl(interfaceDecl, extendNodes);
            }
            Referenceable ref = (Referenceable) node.second;
            declareMap.put(node.second, declare(ref, env));
        }
    }

    private boolean ifContainModifier(ASTNode modifiers, String name){
        if (modifiers == null) return false;
        for (ASTNode n : modifiers.children){
            if (n.value == name) return true;
        }
        return false;
    }

    public void checkExtendDecl(ClassDecl classDecl, List<Pair<Referenceable, ScopeEnvironment>> parents) throws Exception {

        ASTNode myModifiers = classDecl.getModifiers();
        String myName = classDecl.getName();
        for (Pair<Referenceable, ScopeEnvironment> node : parents) {
            if (node.first instanceof ClassDecl) {
                ClassDecl parent = (ClassDecl) node.first;
                ASTNode modifiers = parent.getModifiers();
                String name = parent.getName();
                if (myName == name) {
                    throw new Exception("Acyclic class name or duplicated name: "+ myName+ " "+ name);
                }
                if (ifContainModifier(modifiers, "final")) {
                    throw new Exception("Cannot Extend a final class"+ myName+ " "+ name );
                }
            }

        }
    }

    public void checkExtendDecl(InterfaceDecl interfaceDecl, List<Pair<Referenceable, ScopeEnvironment>> parents) throws Exception {
        String myName = interfaceDecl.getName();
        for (Pair<Referenceable, ScopeEnvironment> node : parents) {
            if (node.first instanceof InterfaceDecl) {
                InterfaceDecl parent = (InterfaceDecl) node.first;
                String name = parent.getName();
                if (myName == name) {
                    throw new Exception("Acyclic interface name or duplicated name"+ myName+ " "+ name);
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

        if (superNode == null) {
            return extendNodes;
        }

        Name extendName = (Name) ((ClassOrInterfaceType)superNode.children.get(0)).getName();

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
            throw new Exception("Acylic Extends in class with the original node "+ classDecl.getName());
        }
        if (extendNodes != null) {
            for (Pair<Referenceable, ScopeEnvironment> inNode : extendNodes) {
                if (found.first == inNode.first) {
                    throw new Exception("Acylic Extends in class for: "+ ((ClassDecl)found.first).getName() + " " + ((ClassDecl)inNode.first).getName());
                }
            }
        }

        ClassDecl classNode = (ClassDecl)found.first;

        classDecl.parentClass = classNode;
//        System.out.println("Parent: " + classNode.getName());
//        System.out.println("Child: " + classDecl.getName());
//        System.out.println();
        extendNodes.add(found);
        extendNodes.addAll(checkExtendNode(original, classNode, found.second, extendNodes));

        // adding notes to direct parent map
        if (directParentMap.containsKey(classDecl) && !directParentMap.get(classDecl).contains(classNode)){
            directParentMap.get(classDecl).add(classNode);
        } else if (!directParentMap.containsKey(classDecl)) {
            directParentMap.put(classDecl, new ArrayList <Referenceable>());
            directParentMap.get(classDecl).add(classNode);
        }

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
            currentBranch.addAll(extendNodes);
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
        if (listSize > 0){
            // adding nodes to direct parent map
            if (!directParentMap.containsKey(interfaceDecl)) {
                directParentMap.put(interfaceDecl, new ArrayList <Referenceable>());
            }
            directParentMap.get(interfaceDecl).addAll(sameClauseNode);
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
            sameClauseNode.add(interfaceNode);
            currentBranch.add(found);
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
        if (listSize > 0) {
            if (!(directParentMap.containsKey(classDecl))) {
                directParentMap.put(classDecl, new ArrayList<Referenceable>());
            }
            directParentMap.get(classDecl).addAll(sameClauseNode);
        }

        return extendNodes;
    }

    public List <Referenceable> declare(Referenceable node, ScopeEnvironment underEnv) {
        List <Referenceable> result = new ArrayList <Referenceable>(){};
        ScopeEnvironment env = underEnv.childScopes.get(node);
        for (String key : env.localDecls.keySet()){
                result.add(env.localDecls.get(key));
        }
        return result;
    }

    private List <Referenceable> createParentListHelper(ASTNode node) {
        if (parentMap.containsKey(node)) {
            return parentMap.get(node);
        } else if (directParentMap.containsKey(node)){
            parentMap.put(node, new ArrayList <Referenceable>());
            parentMap.get(node).addAll(directParentMap.get(node));
            for (Referenceable parent : directParentMap.get(node)) {
                ASTNode parentNode = (ASTNode) parent;
                List <Referenceable> parentList = createParentListHelper(parentNode);
                for (Referenceable singleParent : parentList) {
                    if (!(parentMap.get(node).contains(singleParent))) {
                        parentMap.get(node).add(singleParent);
                    }
                }
            }
            return parentMap.get(node);
        }
        return new ArrayList <Referenceable>(){};
    }

    public void createParentMap() {
        for (ASTNode node: directParentMap.keySet()) {
            if (node != objectClass && directParentMap.get(node).size() == 0) {
                directParentMap.get(node).add((Referenceable)objectClass);
                if (node instanceof ClassDecl) {
                    ((ClassDecl)node).parentClass = (ClassDecl) objectClass;
//                    System.out.println("Parent: Object" );
//                    System.out.println("Child: " + ((ClassDecl)node).getName());
//                    System.out.println();
                }
            }
        }
        for (ASTNode node : directParentMap.keySet()) {
            if (!(parentMap.containsKey(node)) && directParentMap.containsKey(node)) {
                parentMap.put(node, new ArrayList <Referenceable> ());
                parentMap.get(node).addAll(directParentMap.get(node));
                for (Referenceable parent : directParentMap.get(node)) {
                    ASTNode parentNode = (ASTNode) parent;
                    List <Referenceable> parentList = createParentListHelper(parentNode);
                    for (Referenceable singleParent : parentList) {
                        if (!(parentMap.get(node).contains(singleParent))) {
                            parentMap.get(node).add(singleParent);
                        }
                    }
                }

            } else if (!(parentMap.containsKey(node))) {
                parentMap.put(node, new ArrayList <Referenceable> ());
            }
        }
    }
    public void printDirectParent() {
        for (ASTNode node : directParentMap.keySet()) {
            if (node instanceof ClassDecl) {
                ClassDecl pdecl = (ClassDecl) node;
                System.out.println("direct parent of " + pdecl.getName());
            } else {
                InterfaceDecl pdecl = (InterfaceDecl) node;
                System.out.println("direct parent of " + pdecl.getName());
            }
            for (Referenceable ref : directParentMap.get(node)) {
                if (ref instanceof ClassDecl) {
                    ClassDecl decl = (ClassDecl) ref;
                    System.out.println(decl.getName());
                } else {
                    InterfaceDecl decl = (InterfaceDecl) ref;
                    System.out.println(decl.getName());
                }
            }
            System.out.println("");
        }
    }

    public void printDeclare() {
        for (ASTNode node : declareMap.keySet()) {
            if (node instanceof ClassDecl) {
                ClassDecl pdecl = (ClassDecl) node;
                System.out.println("declare " + pdecl.getName());
            } else {
                InterfaceDecl pdecl = (InterfaceDecl) node;
                System.out.println("declare " + pdecl.getName());
            }
            for (Referenceable ref : declareMap.get(node)) {
                System.out.println(ref);
            }
            System.out.println("");
        }
    }

    public void printInherit() {
        for (ASTNode node : inheritMap.keySet()) {
            if (node instanceof ClassDecl) {
                ClassDecl pdecl = (ClassDecl) node;
                System.out.println("inherit "+pdecl.getName());
            } else {
                InterfaceDecl pdecl = (InterfaceDecl) node;
                System.out.println("inherit "+pdecl.getName());
            }
            for (Referenceable ref : inheritMap.get(node)) {
                System.out.println(ref.toString());
            }
            System.out.println("");
        }
    }

    public void printParent() {
        for (ASTNode node : parentMap.keySet()) {
            if (node instanceof ClassDecl) {
                ClassDecl pdecl = (ClassDecl) node;
                System.out.println("parent of " + pdecl.getName());
            } else {
                InterfaceDecl pdecl = (InterfaceDecl) node;
                System.out.println("parent of " +  pdecl.getName());
            }
            for (Referenceable ref : parentMap.get(node)) {
                if (ref instanceof ClassDecl) {
                    ClassDecl decl = (ClassDecl) ref;
                    System.out.println(decl.getName());
                } else {
                    InterfaceDecl decl = (InterfaceDecl) ref;
                    System.out.println(decl.getName());
                }
            }
            System.out.println("");
        }
    }

    public void createInheritanceMap() throws Exception{
        for (ASTNode node : parentMap.keySet()){
            List <Referenceable> inherited = new ArrayList <Referenceable>(){};
//            System.out.println("class");
//            System.out.println(node);
            for (Referenceable parentNode: parentMap.get(node)) {
                if (node instanceof InterfaceDecl && parentNode instanceof ClassDecl && ((ClassDecl)parentNode).getName().equals("Object")) continue;
//                System.out.println(parentNode);
                List<Referenceable> adding = new ArrayList<Referenceable>();
                for (Referenceable ref : declareMap.get((ASTNode)parentNode)){
                    if (ref instanceof AbstractMethodList) {
                        AbstractMethodList temp = (AbstractMethodList)  ref;
                        AbstractMethodList absDecls = new AbstractMethodList(temp.methods.get(0).getName());

                        for (AbstractMethodDecl abs : temp.methods) {
                            absDecls.methods.add(abs);
                        }
                        adding.add(absDecls);
                    } else if (ref instanceof MethodList) {
                        MethodList temp = (MethodList)  ref;
                        MethodList methodDecls = new MethodList(temp.methods.get(0).getName());
                        for (MethodDecl abs : temp.methods) {
                            methodDecls.methods.add(abs);
                        }
                        adding.add(methodDecls);

                    } else {
                        adding.add(ref);
                    }
                }
//                System.out.println(adding);

                if (adding != null) {
                    List <Pair <Referenceable, Referenceable>> addingReplaceList = new ArrayList <Pair <Referenceable, Referenceable>> () {};
                    List <Pair <Referenceable, Referenceable>> inheritReplaceList = new ArrayList <Pair <Referenceable, Referenceable>> () {};
                    for (Referenceable ref : adding) {
                        if (ref instanceof MethodList || ref instanceof AbstractMethodList) { //check if there exists an abstract method
                            for (Referenceable inNode : inherited) {
                                if (inNode instanceof MethodList && ref instanceof  AbstractMethodList) {
                                    MethodList methodList = (MethodList) inNode;
                                    AbstractMethodList abstractMethodList = (AbstractMethodList) ref;
                                    if (methodList.getSimpleName().equals(abstractMethodList.getSimpleName())) {
                                        AbstractMethodList newAbs = popAbstract(methodList, abstractMethodList);
                                        if (newAbs != null){
                                            Referenceable replace = null;
                                            if (newAbs.methods.size() != 0) {
                                                replace = (Referenceable)newAbs;
                                            }
                                            addingReplaceList.add(new Pair <Referenceable, Referenceable> (ref, replace));
                                        }
                                    }

                                } else if (inNode instanceof AbstractMethodList && ref instanceof MethodList) {
                                    MethodList methodList = (MethodList) ref;
                                    AbstractMethodList abstractMethodList = (AbstractMethodList) inNode;
                                    if (methodList.getSimpleName().equals(abstractMethodList.getSimpleName())) {
                                        AbstractMethodList newAbs = popAbstract(methodList, abstractMethodList);
                                        if (newAbs != null){
                                            Referenceable replace = null;
                                            if (newAbs.methods.size() != 0) {
                                                replace = (Referenceable)newAbs;
                                            }
                                            inheritReplaceList.add(new Pair <Referenceable, Referenceable> (ref, replace));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (Pair <Referenceable, Referenceable> pair : inheritReplaceList) {
                        inherited.remove(pair.first);
                        if (pair.second != null) {
                            inherited.add(pair.second);
                        }
                    }
                    for (Pair <Referenceable, Referenceable> pair : addingReplaceList) {
                        adding.remove(pair.first);
                        if (pair.second != null) {
                            adding.add(pair.second);
                        }
                    }
//                    System.out.println(adding);
//                    System.out.println("");
                    inherited.addAll(adding);
                }
            }
            inheritMap.put(node, inherited);
        }
    }

    AbstractMethodList popAbstract(MethodList methodList, AbstractMethodList abstractMethodList) throws Exception{
        boolean change = false;
        List <AbstractMethodDecl> removeList = new ArrayList<AbstractMethodDecl>(){};
        for (MethodDecl method : methodList.methods){
            for (AbstractMethodDecl abstractMethod : abstractMethodList.methods) {
                List <String> abSig = get_sig(abstractMethod);
                List <String> meSig = get_sig(method);

                if (abSig.size() == meSig.size()) {
                    boolean equal = true;
                    for (int i = 0; i < abSig.size(); i++) {
                        if (!(abSig.get(i).equals(meSig.get(i)))) {
                            equal = false;
                            break;
                        }
                    }
                    if (equal) {
//                        System.out.println(abstractMethod);
//                        if (abstractMethod.isPublic()) {
//                            System.out.println("public abstract " + abstractMethod);
//                        }
//                        System.out.println(method);
//                        if (method.isNonAbAndProtected()) {
//                            System.out.println("protected public " + method);
//                        }
                        if (method.isAbstract()) {
                            continue;
                        } else if (abstractMethod.isPublic() && method.isProtected()) {
                            //throw new Exception("Public method is replaced by protected because of abstract "+ abstractMethod.getName() + " " + method.getName());
                        }

                        if (!get_type(abstractMethod).equals(get_type(method)) ) {
                            throw new Exception("Same sig diff return being overriden");
                        }
                        change = true;
                        removeList.add(abstractMethod);
                    }
                }
            }
        }
        for (AbstractMethodDecl abstractMethod : removeList) {
            abstractMethodList.methods.remove(abstractMethod);
        }

        if (!change) {
            return null;
        }
        return abstractMethodList;
    }


}