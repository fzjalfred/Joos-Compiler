package visitors;
import lexer.Token;
import lexer.sym;
import ast.*;
import exception.SemanticError;
import hierarchy.HierarchyChecking;
import lexer.Token;
import type.*;
import utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class TypeCheckVisitor extends Visitor{ //TODO: static method/field use Je_6_StaticAccessToNontatic_Method.java
    public Context context;
    public RootEnvironment env;
    public HierarchyChecking hierarchyChecker;
    public Type returnType;
    public TypeDecl currTypeDecl;
    public List<Type> numsTypes;
    public boolean isStatic = false;
    public Type fieldType;
    public ClassBodyDecl classBodyDecl;
    public LocalVarDecl localVarDecl;



    public TypeCheckVisitor(RootEnvironment env, HierarchyChecking hierarchyChecker){
        context = new Context();
        this.env = env;
        this.hierarchyChecker = hierarchyChecker;
        this.returnType = null;
        this.currTypeDecl = null;
        this.fieldType = null;
        this.classBodyDecl = null;
    }

    private void checkProtected(Referenceable refer, TypeDecl exprType){
        if (refer == null) return;
        ScopeEnvironment thisScope = env.ASTNodeToScopes.get(currTypeDecl);
        ScopeEnvironment referScope = env.ASTNodeToScopes.get(refer);
        /** check protected access */
        if (refer instanceof FieldDecl){
            FieldDecl fieldDecl = (FieldDecl)refer;
            if (fieldDecl.getModifiers().getModifiersSet().contains("protected")) {
                Referenceable accessClass = referScope.typeDecl;
                if (exprType != null){
                    if (exprType != currTypeDecl&&!hierarchyChecker.parentMap.get(exprType).contains(currTypeDecl)){
                        throw new SemanticError("\' field " + fieldDecl.getFirstVarName() + "\' is protected.");
                    }
                }
                if (currTypeDecl!=accessClass&&!hierarchyChecker.parentMap.get(currTypeDecl).contains(accessClass) && !thisScope.packageName.equals(referScope.packageName)) {
                    throw new SemanticError("\' field " + fieldDecl.getFirstVarName() + "\' is protected.");
                }
            }
        }   else if (refer instanceof MethodDecl){
            MethodDecl methodDecl = (MethodDecl)refer;
            if (methodDecl.getMethodHeader().getModifiers().getModifiersSet().contains("protected")) {
                Referenceable accessClass = referScope.typeDecl;
                if (exprType != null){
                    if (exprType != currTypeDecl&&!hierarchyChecker.parentMap.get(exprType).contains(currTypeDecl)){
                        throw new SemanticError("\' method " + methodDecl.getName() + "\' is protected.");
                    }
                }
                if (currTypeDecl!=accessClass&&!hierarchyChecker.parentMap.get(currTypeDecl).contains(accessClass) && !thisScope.packageName.equals(referScope.packageName)) {
                    throw new SemanticError("\' method " + methodDecl.getName() + "\' is protected.");
                }
            }
        }   else if (refer instanceof AbstractMethodDecl){
            AbstractMethodDecl methodDecl = (AbstractMethodDecl)refer;
            if (methodDecl.getModifiers().getModifiersSet().contains("protected")) {
                Referenceable accessClass = referScope.typeDecl;
                if (exprType != null){
                    if (exprType != currTypeDecl&&!hierarchyChecker.parentMap.get(exprType).contains(currTypeDecl)){
                        throw new SemanticError("\' method " + methodDecl.getName() + "\' is protected.");
                    }
                }
                if (currTypeDecl!=accessClass&&!hierarchyChecker.parentMap.get(currTypeDecl).contains(accessClass) && !thisScope.packageName.equals(referScope.packageName)) {
                    throw new SemanticError("\' method " + methodDecl.getName() + "\' is protected.");
                }
            }
        }   else if (refer instanceof ConstructorDecl) {
            ConstructorDecl constructorDecl = (ConstructorDecl) refer;
            if (constructorDecl.getModifiers().getModifiersSet().contains("protected")) {
                //System.out.println("curr package name is " + thisScope.packageName + " refer package name is " + referScope.packageName);
                if (!thisScope.packageName.equals(referScope.packageName))
                    throw new SemanticError("Constructor " + ((ConstructorDecl) refer).getName() + " is protected");
            }
        }

    }

    private boolean checkStaticUse(Referenceable decl){
        if (isStatic){
            if (decl instanceof FieldDecl){
                FieldDecl fieldDecl = (FieldDecl)decl;
                return tools.checkStatic(fieldDecl.getModifiers());
            }   else if (decl instanceof MethodDecl){
                MethodDecl methodDecl = (MethodDecl)decl;
                return tools.checkStatic(methodDecl.getMethodHeader().getModifiers());
            }   else if (decl instanceof AbstractMethodDecl){
                return false;
            }
            return true;
        }   else {
            return true;
        }
    }

    private boolean checkObjectStatic(Referenceable decl){
        if (decl instanceof FieldDecl){
            FieldDecl fieldDecl = (FieldDecl)decl;
            return tools.checkStatic(fieldDecl.getModifiers());
        }   else if (decl instanceof MethodDecl){
            MethodDecl methodDecl = (MethodDecl)decl;
            return tools.checkStatic(methodDecl.getMethodHeader().getModifiers());
        }   else if (decl instanceof AbstractMethodDecl){
            return false;
        }
        return false;
    }


    private Referenceable evalMethod(Type currType, Name name, int idx, List<Type>types, boolean isObject){
        Map<String, List<ASTNode>> containMap = null;
        List<String> names = name.getFullName();
        Referenceable resMethod = null;
        for (; idx < names.size(); idx++){
            if (currType instanceof PrimitiveType) return null;
            String str = names.get(idx);
            if (isLastIdx(idx, names.size()) && currType instanceof ClassOrInterfaceType){
                ClassOrInterfaceType classType = (ClassOrInterfaceType)currType;
                assert classType.typeDecl != null;
                containMap = hierarchyChecker.containMap.get(classType.typeDecl);
                if (containMap.containsKey(str)){
                    resMethod = tools.fetchMethod(containMap.get(str),types);
                    checkProtected(resMethod, classType.typeDecl);
                    if (resMethod == null){
                        resMethod = tools.fetchAbstractMethod(containMap.get(str), types);
                        if (isObject) checkProtected(resMethod, classType.typeDecl);
                        else checkProtected(resMethod, null);
                        if (isObject &&  checkObjectStatic(resMethod)){
                            throw new SemanticError("cannot read static method on Object " + classType);
                        }   else if (!isObject && !checkStaticUse(resMethod)){
                            throw new SemanticError("cannot use non-static method " + resMethod + " in static method");
                        }
                    }   else{
                        if (isObject) checkProtected(resMethod, classType.typeDecl);
                        else checkProtected(resMethod, null);
                        if (isObject &&  checkObjectStatic(resMethod)){
                            throw new SemanticError("cannot read static method on Object " + classType);
                        }   else if (!isObject && !checkStaticUse(resMethod)){
                            throw new SemanticError("cannot use non-static method " + resMethod + " in static method");
                        }
                    }

                } // if
            }   else if (currType instanceof ArrayType && str.equals("length")){
                currType = new NumericType(tools.empty(), "int");
            }   else if (currType instanceof ClassOrInterfaceType){
                ClassOrInterfaceType classType = (ClassOrInterfaceType)currType;
                assert classType.typeDecl != null;
                containMap = hierarchyChecker.containMap.get(classType.typeDecl);
                if (containMap.containsKey(str)){
                    FieldDecl  field = tools.fetchField(containMap.get(str));
                    if (isObject &&  checkObjectStatic(field)){
                        throw new SemanticError("cannot read static field on Object " + field.getFirstVarName());
                    }   else if (!isObject && !checkStaticUse(field)){
                        throw new SemanticError("cannot use non-static field " + field.getFirstVarName() + " in static method");
                    }
                    if (field != null){
                        isObject = true;
                        checkProtected(field, classType.typeDecl);
                        currType = field.getType();
                        continue;
                    } // if
                } // if
                return null;
            }   else {
                return null;
            }
        } // for
        return resMethod;
    }


    private Referenceable fetchMethodOrAbsMethod(List<ASTNode> refers, List<Type> types){
        Referenceable res = null;
        res = tools.fetchMethod(refers, types);
        if (res == null) res = tools.fetchAbstractMethod(refers, types);
        return res;
    }

    private Referenceable checkStaticMethodUse(Referenceable refer, TypeDecl typeDecl){
        //TODO
        if (refer instanceof MethodDecl) {
            MethodDecl methodDecl = (MethodDecl)refer;
            if (methodDecl != null && (tools.checkStatic(methodDecl.getModifiers()) || (typeDecl == currTypeDecl && checkStaticUse(refer)))) return refer;
        }
        if (refer instanceof AbstractMethodDecl) {
            AbstractMethodDecl methodDecl = (AbstractMethodDecl)refer;
            if (methodDecl != null && (tools.checkStatic(methodDecl.getModifiers()) || (typeDecl == currTypeDecl && checkStaticUse(refer)))) return refer;
        }
        return null;
    }

    private Referenceable findStaticMethodfix(ScopeEnvironment scopeEnvironment, Name name, List<Type> types){
        //System.out.println(name.getValue());
        List<String> nameStrs = name.getFullName();
        Token classSimpleName = tools.simpleNameConstructor(nameStrs.get(0));
        if (context.get(classSimpleName.value) != null) return null;
        Referenceable res = null;

        if (name.children.size() == 2){

            res = scopeEnvironment.lookupTypeDecl(classSimpleName);
            if (res != null){
                TypeDecl typeDecl = (TypeDecl)res;
                Map<String, List<ASTNode>> contain = hierarchyChecker.containMap.get(typeDecl);
                Referenceable methodDecl = fetchMethodOrAbsMethod(contain.get(nameStrs.get(1)), types);
                methodDecl = checkStaticMethodUse(methodDecl, typeDecl);
                checkProtected(methodDecl, null);
                if (methodDecl!=null) return methodDecl;
                //TODO
                throw new SemanticError(name.getValue() + "is non static");
            }
            throw new SemanticError("cannot resolve " + name.getValue() );
        }
        if (name.children.size() > 2){
            String method = nameStrs.get(nameStrs.size()-1);
            Name qualifiedName = tools.nameConstructor(tools.joinList(nameStrs, 0, nameStrs.size()-1));
            res = env.lookup(qualifiedName);
            //System.out.println(res);
            if (res != null && res instanceof TypeDecl){
                TypeDecl typeDecl = (TypeDecl)res;
                Map<String, List<ASTNode>> contain = hierarchyChecker.containMap.get(typeDecl);
                Referenceable methodDecl = fetchMethodOrAbsMethod(contain.get(method), types);
                methodDecl = checkStaticMethodUse(methodDecl, typeDecl);
                checkProtected(methodDecl, null);
                if (methodDecl!=null) return methodDecl;
                throw new SemanticError(name.getValue() + "is non static");
            }
        }
        return null;
    }

    private FieldDecl findStaticField(ScopeEnvironment scopeEnvironment, Name name){
        //System.out.println(name.getValue());
        List<String> nameStrs = name.getFullName();
        Token classSimpleName = tools.simpleNameConstructor(nameStrs.get(0));
        //if (scopeEnvironment.lookupTypeDecl(classSimpleName) != null) return null;
        if (context.get(classSimpleName.value) != null) return null;
        Referenceable res = null;
        if (name.children.size() == 2){
            res = scopeEnvironment.lookupTypeDecl(classSimpleName);
            if (res != null){
                TypeDecl typeDecl = (TypeDecl)res;
                Map<String, List<ASTNode>> contain = hierarchyChecker.containMap.get(typeDecl);
                FieldDecl fieldDecl = tools.fetchField(contain.get(nameStrs.get(1)));
                checkProtected(fieldDecl, null);
                if (fieldDecl != null && (tools.checkStatic(fieldDecl.getModifiers()) || (typeDecl == currTypeDecl && checkStaticUse(fieldDecl)))) return fieldDecl;
                else throw new SemanticError(name.getValue() + "is non static");
            }
            throw new SemanticError("cannot resolve " + name.getValue() );
        }
        if (name.children.size() > 2){
            String field = nameStrs.get(nameStrs.size()-1);
            Name qualifiedName = tools.nameConstructor(tools.joinList(nameStrs, 0, nameStrs.size()-1));
            res = env.lookup(qualifiedName);
            if (res != null && res instanceof TypeDecl){
                TypeDecl typeDecl = (TypeDecl)res;
                Map<String, List<ASTNode>> contain = hierarchyChecker.containMap.get(typeDecl);
                FieldDecl fieldDecl = tools.fetchField(contain.get(field));
                checkProtected(fieldDecl, null);
                if (fieldDecl != null && (tools.checkStatic(fieldDecl.getModifiers()) || (typeDecl == currTypeDecl && checkStaticUse(fieldDecl)))) return fieldDecl;
                else throw new SemanticError(name.getValue() + "is non static");
            }
        }
        return null;
    }

    private void disAmbiguousNameField(Name name, Expr node, Map<String, List<ASTNode>> containMap){
        /** first check whether it's static */
        ScopeEnvironment scopeEnvironment = env.ASTNodeToScopes.get(node);
        Referenceable res = findStaticField(scopeEnvironment, name);
        if (res != null) {
            checkProtected(res, null);
            node.type = res.getType();
            return;
        }

        /** derive first expr's type  */
        List<String> names = name.getFullName();
        String nameStr = "";
        Type currType = null;
        Referenceable first_reciever = null;
        List<FieldDecl> fields = new ArrayList<FieldDecl>();
        int idx = 0;
        for (;idx < names.size();idx++){
            String str = names.get(idx);
            nameStr += str;
            Referenceable currRes = context.get(nameStr);
            //tools.println("look up field" + nameStr + " get " + currType, DebugID.zhenyan);
            if (currRes != null) {
                if (localVarDecl != null && nameStr.equals(localVarDecl.getVarDeclarators().getFirstName())) throw new SemanticError("Local variable decl " + nameStr + " cannot use it self");
                currType = currRes.getType();
                checkProtected(currRes, null);
                res = currRes;
                if (currRes instanceof FieldDecl){
                    first_reciever = new ThisLiteral(tools.empty(), "this");
                    fields.add((FieldDecl)currRes);
                }   else {
                    first_reciever = currRes;
                }

                /** check static field access*/
                //System.out.println(nameStr + " has decl "  + context.get(nameStr) + " scope find " + scopeEnvironment.lookup(tools.simpleNameConstructor(nameStr)));
                if (!checkStaticUse(currRes)) throw new SemanticError("Cannot use non-static field " + nameStr + " in static class member decl");
                break;
            }

            // second check inherit map
            if (containMap.containsKey(nameStr)){
                first_reciever = new ThisLiteral(tools.empty(), "this");
                FieldDecl  field = tools.fetchField(containMap.get(nameStr));
                if (field != null){
                    checkProtected(field, null);
                    currType = field.getType();
                    res = field;
                    fields.add(field);
                    break;
                }
            }


            // third check static field, which is env lookup: A.B.C
            Referenceable nameRefer = env.lookup(tools.nameConstructor(nameStr));
            if (nameRefer != null && nameRefer instanceof FieldDecl){
                TypeDecl typeDecl = env.ASTNodeToScopes.get(nameRefer).typeDecl;
                checkProtected(nameRefer, typeDecl);
                if (!tools.checkStatic(((FieldDecl) nameRefer).getModifiers())) throw new SemanticError(nameStr + " is non static");
                FieldDecl field = (FieldDecl)nameRefer;
                currType = field.getType();
                res = field;
                first_reciever = field;
                break;
            }
            nameStr += '.';

        }

        /** process remaining name */
        if (!isLastIdx(idx, names.size())) nameStr += '.';
        idx++;
        for (; idx < names.size(); idx++){
            if (currType instanceof PrimitiveType) throw new SemanticError(nameStr.substring(0, nameStr.length()-1)+ " has been inferred to type " + currType + "; so " + name.getValue() + " cannot be resolved to type");
            String str = names.get(idx);
            if (currType instanceof ArrayType && str.equals("length")){
                fields.add(new FieldDecl(tools.empty(), "length"));
                nameStr += str;
                currType = new NumericType(tools.empty(), "int");
                nameStr += '.';
                res = tools.arrayLen();
                if (node instanceof LHS) {
                    ((LHS)node).isAssignable = false;
                }
            }   else if (currType instanceof ClassOrInterfaceType){
                ClassOrInterfaceType classType = (ClassOrInterfaceType)currType;
                assert classType.typeDecl != null;
                containMap = hierarchyChecker.containMap.get(classType.typeDecl);
                if (containMap.containsKey(str)){
                    FieldDecl  field = tools.fetchField(containMap.get(str));
                    if (checkObjectStatic(field)){
                        throw new SemanticError("cannot read static field on Object " + field.getFirstVarName());
                    }
                    if (field != null){
                        checkProtected(field, classType.typeDecl);
                        fields.add(field);
                        nameStr += str;
                        res = field;
                        currType = field.getType();
                        nameStr += '.';
                        continue;
                    } // if
                } // if
                throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + name.getValue() + " cannot be resolved to type");
            }   else {
                throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + name.getValue() + " cannot be resolved to type");
            } // if
        } // for

        if (res!= null){
            node.type = currType;
            if (node instanceof PostFixExpr){
                PostFixExpr _postfixexpr = ((PostFixExpr)node);
                _postfixexpr.refer = res;
                _postfixexpr.first_receiver = first_reciever;
                _postfixexpr.subfields = fields;
            } else if (node instanceof LHS){
                LHS _lhs = (LHS)node;
                _lhs.refer = res;
                _lhs.first_receiver = first_reciever;
                _lhs.subfields = fields;
            } else {
                throw new SemanticError(node + " is none of postfix, lhs or arrayaccess");
            }
        }   else {
            throw new SemanticError(nameStr + " cannot be resolved to type");
        }
    }

    private boolean checkUpCast(Type t1, Type t2){
        if (t2 instanceof ClassOrInterfaceType && ((ClassOrInterfaceType)t2).typeDecl == env.lookup(tools.nameConstructor("java.lang.Object"))){
            return true;
        }   else if (t1 instanceof ClassOrInterfaceType && t2 instanceof ClassOrInterfaceType){
            TypeDecl t1Decl = ((ClassOrInterfaceType)t1).typeDecl;
            TypeDecl t2Decl = ((ClassOrInterfaceType)t2).typeDecl;
            //tools.println(hierarchyChecker.parentMap.get(t2Decl) + " p for t1 " + t1Decl, DebugID.zhenyan);
            return tools.containClass(hierarchyChecker.parentMap.get(t1Decl), t2Decl);
        }   else if (t1 instanceof ArrayType && t2 instanceof ArrayType){
            return  (checkUpCast(((ArrayType) t1).getType(), ((ArrayType) t2).getType()));
        }
        return false;
    }

    private boolean checkDownCast(Type t1, Type t2){
        if (t1 instanceof NullType) return true;
        if (t1 instanceof ClassOrInterfaceType && ((ClassOrInterfaceType)t1).typeDecl == env.lookup(tools.nameConstructor("java.lang.Object"))){
            return true;
        }   else if (t1 instanceof ClassOrInterfaceType && t2 instanceof ClassOrInterfaceType){
            TypeDecl t1Decl = ((ClassOrInterfaceType)t1).typeDecl;
            TypeDecl t2Decl = ((ClassOrInterfaceType)t2).typeDecl;
            //tools.println(hierarchyChecker.parentMap.get(t1Decl) + " p for t2 " + t2Decl, DebugID.zhenyan);
            return tools.containClass(hierarchyChecker.parentMap.get(t2Decl), t1Decl);
        }  else if (t1 instanceof ArrayType && t2 instanceof ArrayType){
            return  (checkDownCast(((ArrayType) t1).getType(), ((ArrayType) t2).getType()));
        }
        return false;
    }

    private boolean checkFinalClass(ClassOrInterfaceType t){
        if (t.typeDecl instanceof ClassDecl){
            if (((ClassDecl)t.typeDecl).getModifiers().getModifiersSet().contains("final")) return true;
        }
        return true;
    }


    /** Literal */
    @Override
    public void visit(NumericLiteral node) {
        node.type = new NumericType(tools.empty(), "int");
        if (!node.value.equals("2147483648")) {
            node.integer_value = Integer.valueOf(node.value);
        }
    }

    @Override
    public void visit(StringLiteral node) {
        
        node.type = tools.getClassType("java.lang.String", (TypeDecl)env.lookup(tools.nameConstructor("java.lang.String")));
        //tools.println("assign " + node.value + " to " + node.type, DebugID.zhenyan);
        
    }

    @Override
    public void visit(CharLiteral node) {
        //node.type = new ClassOrInterfaceType(tools.list(tools.nameConstructor("java.lang.Character")), "");
        node.type = new NumericType(tools.empty(), "char");
    }

    @Override
    public void visit(BoolLiteral node) {
        //node.type = new ClassOrInterfaceType(tools.list(tools.nameConstructor("java.lang.Boolean")), "");
        //node.type = tools.getClassType("java.lang.String", (TypeDecl)env.lookup(tools.nameConstructor("java.lang.Boolean")));
        node.type = new PrimitiveType(tools.empty(), "boolean");
    }

    @Override
    public void visit(NullLiteral node) {
        node.type = new NullType(tools.empty(), "null");
    }

    public void visit(ThisLiteral node) {
        if (isStatic) throw new SemanticError("this literal cannot appear in static class member decl");
        ClassDecl classDecl = (ClassDecl) env.ASTNodeToScopes.get(node).typeDecl;
        node.type = tools.getClassType(classDecl.getName(), classDecl);
    }

    /** Operators */

    public void visit(UnaryExprNotPlusMinus node){
        if (node.children.size() == 2) {
            // Not statement
            Type type = node.getUnaryExpr().type;
            if (!(type instanceof PrimitiveType && type.getNameString().equals("boolean") )) {
                throw new SemanticError("expression \'"+node.getUnaryExpr()+":" +type+"\' after '!' is not boolean.");
            }
            node.type = type;
        } else {
            throw new SemanticError("should never go here ...");
        }
    }

    @Override
    public void visit(LHS node) {
        if (node.hasName()){
            disAmbiguousNameField(node.getName(), node, hierarchyChecker.containMap.get(currTypeDecl));
        }   else {
            node.type = node.getExpr().type;
        }
    }

    public void visit(DimExpr node){
        node.type = node.getOperatorLeft().type;
    }

    public void visit(ArrayAccess node) {
        Type e1Type = null;
        if (node.hasName()){
            Map<String, List<ASTNode>> map = null;
            if (classBodyDecl instanceof FieldDecl){
                map = hierarchyChecker.inheritMapRe.get(currTypeDecl);
            }   else {
                map = hierarchyChecker.containMap.get(currTypeDecl);
            }
            disAmbiguousNameField(node.getName(), node, map);
            e1Type = node.type;
        }   else {
            e1Type = node.getExpr().type;
        }
        if (! (e1Type instanceof ArrayType)){
            throw new SemanticError("in array access: e1 " + e1Type + " is not array type");
        }
        if (!(node.getDimExpr().type instanceof NumericType)){
            throw new SemanticError("in array access: dimexpr " + node.getDimExpr().type + " is not numeric type");
        }

        Type t1 = new NumericType(tools.empty(), "int");
        Type t2 = node.getDimExpr().type;
        if (!isAssignable(t1, t2, env)) {
            throw new SemanticError("Array index must have numeric type: "+ t2);
        }
        node.type = ((ArrayType)e1Type).getType();
    }


    public void visit(UnaryExpr node){
        if (node.children.size() == 2) {
            // Not statement
            Type expr = node.getUnaryExpr().type;
            node.type = expr;
            if (node.getUnaryExpr().integer_value != null) {
                node.integer_value = - node.getUnaryExpr().integer_value;
            }
            
        }
    }

    public void visit(PrimaryNoArray node){
        if (node.getExpr().type == null){
            throw new SemanticError(node.getExpr().type + " should not be null");
        }
        node.type = node.getExpr().type;
        if (node.getExpr() instanceof Expr) {
            node.integer_value = node.getExpr().integer_value;
            node.boolStruct = node.getExpr().boolStruct;
        }
    }

    public void visit(AdditiveExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new NumericType(tools.empty(), "int");
                if (node.getOperatorLeft().integer_value != null && node.getOperatorRight().integer_value != null) {
                    if (node.getOperator().equals("+")) {
                        node.integer_value = node.getOperatorLeft().integer_value + node.getOperatorRight().integer_value;
                    } else if (node.getOperator().equals("-")) {
                        node.integer_value = node.getOperatorLeft().integer_value - node.getOperatorRight().integer_value;
                    }
                }
                
            } else if ( (t1 instanceof ClassOrInterfaceType && tools.get_class_qualifed_name((ClassOrInterfaceType)t1, env).equals("java.lang.String") && t2 != null)
            || (t2 instanceof ClassOrInterfaceType && tools.get_class_qualifed_name((ClassOrInterfaceType)t2, env).equals("java.lang.String") && t1 != null)) {
                if (node.isPlusOperator()) {
                    node.type = tools.getClassType("java.lang.String", (TypeDecl)env.lookup(tools.nameConstructor("java.lang.String")));
                } else {
                    throw new SemanticError("Invalid operator for String between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
                }
            }   else {
                throw new SemanticError("Invalid AdditiveExpr between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }
    

    public void visit(MultiplicativeExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new NumericType(tools.empty(), "int");
                if (node.getOperatorLeft().integer_value != null && node.getOperatorRight().integer_value != null) {
                    if (node.getOperator().equals("*")) {
                        node.integer_value = node.getOperatorLeft().integer_value * node.getOperatorRight().integer_value;
                    } else if (node.getOperator().equals("/")) {
                        node.integer_value = node.getOperatorLeft().integer_value / node.getOperatorRight().integer_value;
                    } else if (node.getOperator().equals("%")) {
                        node.integer_value = node.getOperatorLeft().integer_value % node.getOperatorRight().integer_value;
                    }
                }
            } else {
                
                throw new SemanticError("Invalid MultiplicativeExpr between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }

    public void visit(RelationExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else if (node.isInstanceOf()) {
            Type t1 = node.getisInstanceOfLeft().type;
            Type t2 = node.getisInstanceOfRight();
            // String qualified_name2 = tools.get_class_qualifed_name(((ClassOrInterfaceType)t2).typeDecl, env);
            // if (t2 instanceof ClassOrInterfaceType && (qualified_name2.equals("java.lang.Object") || qualified_name2.equals("java.lang.Cloneable")
            // || qualified_name2.equals("java.io.Serializable") ) ) {
            //     throw new SemanticError("Cannot check instanceof on simple types");
            // }
            if (t1 instanceof PrimitiveType || t2 instanceof PrimitiveType) {
                throw new SemanticError("Invalid instanceof use between "+t1 + " and "+t2);
            }
            if (isAssignable(t1, t2, env) || isAssignable(t2, t1, env)) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else {
                throw new SemanticError("Invalid instanceof use between "+t1 + " and "+t2);
            }

            
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
                if (node.getOperatorLeft().integer_value != null && node.getOperatorRight().integer_value != null) {
                    if (node.getOperator().equals("<")) {
                        Boolean res = node.getOperatorLeft().integer_value < node.getOperatorRight().integer_value;
                        node.boolStruct = new Expr.BoolStruct(res);
                    } else if (node.getOperator().equals(">")) {
                        Boolean res = node.getOperatorLeft().integer_value > node.getOperatorRight().integer_value;
                        node.boolStruct = new Expr.BoolStruct(res);
                    } else if (node.getOperator().equals("<=")) {
                        Boolean res = node.getOperatorLeft().integer_value <= node.getOperatorRight().integer_value;
                        node.boolStruct = new Expr.BoolStruct(res);
                    } else if (node.getOperator().equals(">=")) {
                        Boolean res = node.getOperatorLeft().integer_value >= node.getOperatorRight().integer_value;
                        node.boolStruct = new Expr.BoolStruct(res);
                    } else if (node.getOperator().equals(">")) {
                        Boolean res = node.getOperatorLeft().integer_value > node.getOperatorRight().integer_value;
                        node.boolStruct = new Expr.BoolStruct(res);
                    }
                }
            } else if (t1.equals(t2)) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else if (isAssignable(t1, t2, env) || isAssignable(t2, t1, env)) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else {
                throw new SemanticError("Invalid RelationExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }

    public void visit(EqualityExpr node){
        if (node.children.size() == 1) {    
            node.type = (node.getSingleChild()).type;
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
                if (node.getOperatorLeft().integer_value != null && node.getOperatorRight().integer_value != null) {
                    if (node.getOperator().equals("==")) {
                        Boolean res = node.getOperatorLeft().integer_value == node.getOperatorRight().integer_value;
                        node.boolStruct = new Expr.BoolStruct(res);
                    } else if (node.getOperator().equals("!=")) {
                        Boolean res = node.getOperatorLeft().integer_value != node.getOperatorRight().integer_value;
                        node.boolStruct = new Expr.BoolStruct(res);
                    }
                }
            } else if (t1.equals(t2)) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else if (t2 instanceof NullType ) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else if (t1 instanceof NumericType && t2 instanceof ClassOrInterfaceType) {
                String qualified_name2 = tools.get_class_qualifed_name(((ClassOrInterfaceType)t2).typeDecl, env);
                if (qualified_name2.equals("java.lang.Object")) {
                    throw new SemanticError("Invalid EqualityExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
                }
            }  else if (t2 instanceof NumericType && t1 instanceof ClassOrInterfaceType) {
                String qualified_name1 = tools.get_class_qualifed_name(((ClassOrInterfaceType)t1).typeDecl, env);
                if (qualified_name1.equals("java.lang.Object")) {
                    throw new SemanticError("Invalid EqualityExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
                }
            } else if (isAssignable(t1, t2, env) || isAssignable(t2, t1, env)) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else {
                throw new SemanticError("Invalid EqualityExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }

    public void visit(ConditionalAndExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else if (t1.equals(t2) ) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else {
                throw new SemanticError("Invalid ConditionalAndExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }

    public void visit(ConditionalOrExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else if (t1.equals(t2)) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else {
                throw new SemanticError("Invalid ConditionalOrExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }


    /** Assignment */

    public void visit(IfThenStmt node){
        Expr expr = node.getExpr();
        Type type = expr.type;
        if (!(type instanceof PrimitiveType && type.getNameString().equals("boolean"))) {
            throw new SemanticError("if statement needs boolean: "+ expr+ ":"+type);
        }
    }

    public void visit(IfThenElseStmt node){
        Expr expr = node.getExpr();
        Type type = expr.type;
        if (!(type instanceof PrimitiveType && type.getNameString().equals("boolean"))) {
            throw new SemanticError("if statement needs boolean: "+ expr+ ":"+type);
        }
    }


    public void visit(WhileStmt node){
        Expr expr = node.getExpr();
        Type type = expr.type;
        if (!(type instanceof PrimitiveType && type.getNameString().equals("boolean"))) {
            throw new SemanticError("While statement needs boolean: "+ expr+ ":"+type);
        }
    }

    public void visit(Expr node){
        node.type = ((AssignmentExpr)node.getSingleChild()).type;
    }

    public void visit(AssignmentExpr node){
        node.type = (node.getSingleChild()).type;
    }

    public boolean isAssignable(Type t1, Type t2, RootEnvironment env) {
        if (t2 instanceof NullType && !(t1 instanceof PrimitiveType)) {
            return true;
        }
        if (t1.equals(t2) && !(t1 instanceof PrimitiveType)) {
            return true;
        }
        if (t1 instanceof PrimitiveType  &&
        t2 instanceof PrimitiveType) {
            if (t1.getNameString().equals(t2.getNameString())) {
                return true;
            }
            if (t1.getNameString().equals("int") && t2.getNameString().equals("byte")) {
                return true;
            }
            if (t1.getNameString().equals("int") && t2.getNameString().equals("short")) {
                return true;
            }
            if (t1.getNameString().equals("short") && t2.getNameString().equals("byte")) {
                return true;
            }
            if (t1.getNameString().equals("int") && t2.getNameString().equals("char")) {
                return true;
            }
        }
        String qualified_name1 = "";
        String qualified_name2 = "";
        if (t1 instanceof ClassOrInterfaceType) {
            qualified_name1 = tools.get_class_qualifed_name(((ClassOrInterfaceType)t1).typeDecl, env);
        }
        if (t2 instanceof ClassOrInterfaceType) {
            qualified_name1 = tools.get_class_qualifed_name(((ClassOrInterfaceType)t2).typeDecl, env);
        }
        if (t1 instanceof ArrayType && (qualified_name2.equals("java.lang.Cloneable")||
        qualified_name2.equals("java.lang.Object")||
        qualified_name2.equals("java.io.Serializable"))) {
            return true;
        }
        if (t2 instanceof ArrayType && (qualified_name1.equals("java.lang.Cloneable")||
        qualified_name1.equals("java.lang.Object")||
        qualified_name1.equals("java.io.Serializable"))) {
            return true;
        }
        if (t1 instanceof PrimitiveType && qualified_name2.equals("java.lang.Object")) {
            return false;
        }
        if (qualified_name1.equals("java.lang.String") && qualified_name2.equals("java.lang.Object")) {
            return false;
        }

        if (checkUpCast(t2,t1)){
            return true;
        }
        return false;
    }


    public void visit(Assignment node){
        Type t1;
        LHS lhs = node.getAssignmentLeft();
        if (lhs.isAssignable == false) {
            throw new SemanticError("A final field must not be assigned to. (Array.length is final)");
        }
        Type t2 = (node.getAssignmentRight()).type;
        //System.out.println(node.getAssignmentLeft());
        if (lhs.hasName()) {
            t1 = lhs.type;
            if (t1 == null) {
                throw new SemanticError("Invalid Assignment use between " + node.getAssignmentLeft()+":"+t1 + " and " +node.getAssignmentRight()+":"+ t2);
            }
            if (isAssignable(t1, t2, env)) {
                node.type = t1;
            } else {
                throw new SemanticError("Invalid Assignment use between " + node.getAssignmentLeft()+":"+t1 + " and " +node.getAssignmentRight()+":"+ t2);
            }
            //System.out.println(((Name)lhs.children.get(0)).getValue());
        } else{
            //field_access|array_access
            if (lhs.children.get(0) instanceof FieldAccess) {
                FieldAccess field_access = (FieldAccess)lhs.children.get(0);
                String field = field_access.getID().value;
                if (field_access.getPrimary().type instanceof ArrayType){
                    if (field.equals("length")){
                        throw new SemanticError("A final field must not be assigned to. (Array.length is final)");
                    }
                }
            }
            t1 = lhs.getExpr().type;
            if (isAssignable(t1, t2, env)) {
                node.type = t1;
            } else {
                throw new SemanticError("Invalid Assignment use between " + node.getAssignmentLeft()+":"+t1 + " and " +node.getAssignmentRight()+":"+ t2);
            }
        }
        
        //System.out.println(t2.getNameString());
        //System.out.println("====================");
    }
    

    
    @Override
    public void visit(ArrayCreationExpr node){
        Type t1 = new NumericType(tools.empty(), "int");
        Type t2 = node.getDimExpr().type;
        if (isAssignable(t1, t2, env)) {
            node.type = new ArrayType(tools.list(node.getType()), "");
        } else {
            throw new SemanticError("Array index must have numeric type: "+ t2);
        } 
    }

    /** Check that the name of a constructor is the same as the name of its enclosing class. */
    /** A constructor in a class other than java.lang.Object implicitly calls the zero-argument constructor of its superclass. 
     * Check that this zero-argument constructor exists. (insert superclass’s constructor into that constructor) */


    public void visit(ConstructorDecl node){
        classBodyDecl = node;
        returnType = null;
        isStatic = false;
        String constructor_name = node.getName();
        String class_name = "";
        node.whichClass = (ClassDecl)env.ASTNodeToScopes.get(node).typeDecl;
        if (env.ASTNodeToScopes.get(node).typeDecl instanceof ClassDecl) {
            class_name = ((ClassDecl)env.ASTNodeToScopes.get(node).typeDecl).getName();
        }
        if (!class_name.equals(constructor_name)){
            throw new SemanticError("constructor name \'"+ constructor_name +"\' differs in Class \'"+class_name +"\' .");
        }
        // System.out.println(class_name);
        // System.out.println(tools.get_class_qualifed_name(node, env));
        //node.type = tools.getClassType(tools.get_class_qualifed_name(node, env), env.ASTNodeToScopes.get(node).typeDecl);
        if (!tools.get_class_qualifed_name(node, env).equals("java.lang.Object")) {
            ConstructorDeclarator constructor_declarator = node.getConstructorDeclarator();
            if (constructor_declarator.getParameterList() == null || constructor_declarator.getParameterList().getParams().size() == 0) {
                
            }
        }

    }

    /** TypeDecls */
    @Override
    public void visit(ClassDecl node) {
        // field map
        int fieldOffset = 4;
        List <FieldDecl> fieldDecls = node.getAllFieldDecls();
        for (FieldDecl fieldDecl : fieldDecls) {
            node.fieldMap.put(fieldDecl, fieldOffset);
            fieldOffset += 4;
        }

        // method map
        List <MethodDecl> methodDecls = node.getMethodDecls();
        int methodOffset = 8;
        for (MethodDecl methodDecl : methodDecls) {
            node.methodMap.put(methodDecl, methodOffset);
            methodOffset += 4;
        }
        currTypeDecl = node;

        /** check its super constructor */
        List<Referenceable> parents =  hierarchyChecker.parentMap.get(node);
        for (Referenceable superclass: parents) {
            boolean hasSuperConstructor = true;
            if (superclass instanceof ClassDecl && 
            !((ClassDecl)superclass).getModifiers().getModifiersSet().contains("abstract")) {
                hasSuperConstructor = false;
                Map<String, List<ASTNode>> methods = hierarchyChecker.declareMapRe.get((ClassDecl)superclass);
                List<ASTNode> constructors = methods.get(((ClassDecl)superclass).getName());
                for (ASTNode i: constructors) {
                    ConstructorDecl construct = (ConstructorDecl)i;
                    if (construct.getConstructorDeclarator().numParams() == 0) {
                        hasSuperConstructor = true;
                        // must be direct parent supercall
                        if (node.parentClass == (ClassDecl)superclass) {
                            node.supercall = construct;
                        }
                    }
                }
            }
            if (!hasSuperConstructor) {
                throw new SemanticError("Zero-arguments super constructor not exisit: " + node+":"+node.getName());
            }
        }
    }

    @Override
    public void visit(InterfaceDecl node) {
        currTypeDecl = node;
    }

    @Override
    public void visit(FieldDecl node) {
        isStatic = tools.checkStatic(node.getModifiers());
        classBodyDecl = node;
        this.fieldType = node.getType();
    }

    @Override
    public void visit(VarDeclarator node){
        if (node.children.size() == 2 && fieldType != null && node.getExpr()!=null) {
            Type t2 = node.getExpr().type;
            if (!isAssignable(fieldType, t2, env)) {
                throw new SemanticError("Invalid FieldDecl between " +":"+fieldType + " and " +node.getExpr()+":"+ t2); 
            }
        }
        fieldType = null;
    }


    @Override
    public void visit(LocalVarDecl node) {
        String var = node.getVarDeclarators().getFirstName();
        context.put(var, node);
        fieldType = node.getType();

        // check local var initializer
        VarDeclarator vardecl = node.getVarDeclarators().getLastVarDeclarator();
        localVarDecl = node;
        if (vardecl.children.get(1) == null) {
            throw new SemanticError("Local Variable"+ node.getType() + ":" +node.getName() +" needs initializer after."); 
        }
    }

    @Override
    public void visit(Parameter node) {
        String var = node.getVarDeclaratorID().getName();
        context.put(var, node);
    }

    @Override
    public void visit(MethodDecl node) {
        //System.out.println(node);
        isStatic = tools.checkStatic(node.getMethodHeader().getModifiers());
        /** update return value*/
        this.returnType = node.getMethodHeader().getType();
        this.classBodyDecl = node;
    }

    @Override
    public void visit(ForInit node) {
        if (node.isVarDecl()){
            context.put(node.getVarDeclarator().getName(), node);
        }
        //TODO: stmt case
    }


    /** Exprs */
    @Override
    public void visit(FieldAccess node) {
        String field = node.getID().value;
        if (node.getPrimary().type instanceof ArrayType){
            if (field.equals("length")){
                node.field = new FieldDecl(tools.empty(), "length");
                node.type = tools.intType();
                return;
            }
        }   else if (node.getPrimary().type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType)node.getPrimary().type;
            Map<String, List<ASTNode>> containMap = hierarchyChecker.containMap.get(classType.typeDecl);
            FieldDecl fieldDecl = tools.fetchField(containMap.get(field));
            if (fieldDecl != null){
                node.field = fieldDecl;
                node.type = fieldDecl.getType();
                //tools.println("assign field access " + field + " to type: " + node.type, DebugID.zhenyan );
                if (node.getPrimary() instanceof ThisLiteral && !checkStaticUse(fieldDecl)) throw new SemanticError("Cannot use non-static this." + field + " in static class body decl: " + classBodyDecl);
                /** check protected access */
                checkProtected(fieldDecl, classType.typeDecl);

                return;
            }
        }
        throw new SemanticError("cannot evaluate " + field + " to any type");
    }


    @Override
    public void visit(ClassInstanceCreateExpr node) {
        TypeDecl typeDecl = node.getType().typeDecl;
        if (typeDecl instanceof ClassDecl){
            ClassDecl classDecl = (ClassDecl)typeDecl;
            List<Referenceable>  declares = hierarchyChecker.declareMap.get(classDecl);
            //tools.println(" declares are " + declares, DebugID.zhenyan);
            ConstructorDecl ctorDecl = tools.fetchConstructor(declares, node.getArgumentTypeList());
            if (ctorDecl != null){
                checkProtected(ctorDecl, null);
                node.type = node.getType();
                if ( (((ClassOrInterfaceType)node.type).typeDecl instanceof InterfaceDecl) || 
                ((ClassDecl)((ClassOrInterfaceType)node.type).typeDecl).getModifiers().getModifiersSet().contains("abstract") ) {
                    throw new SemanticError("The type in a class instance creation expression must be a non-abstract class.");
                }
                node.callable = ctorDecl;
                return;
            }
        }

        throw new SemanticError("Cannot init interface type " + node.getType());

    }

    @Override
    public void visit(CastExpr node) {
        Type t1 = node.getUnaryExpr().type;
        Type t2 = node.getType();

        if (t1 == null) return; //TODO: remove these when type completed
        /** case 1: 2 same types*/
        if (t1.equals(t2)){
            node.type = t2;
            return;
        }

        /** case 2: both type are numeric */
        if (t1 instanceof NumericType && t2 instanceof NumericType){
            node.type = t2;
            return;
        }


        /** case 3: upcast */
        if (checkUpCast(t1, t2)){
            //tools.println("cast " + t1 + " to " + t2 + " is upcast", DebugID.zhenyan);
            node.type = t2;
            return;
        }

        /** case 4: down cast*/
        if (checkDownCast(t1, t2)){
            //tools.println("cast " + t1 + " to " + t2 + " is down cast", DebugID.zhenyan);
            node.type = t2;
            return;
        }
        if (t1 instanceof ClassOrInterfaceType && t2 instanceof ClassOrInterfaceType){
            ClassOrInterfaceType classt1 = (ClassOrInterfaceType)t1;
            ClassOrInterfaceType classt2 = (ClassOrInterfaceType)t2;
            /** case 5: interface cast */
            if (classt1.typeDecl instanceof InterfaceDecl){
                if (classt2.typeDecl instanceof InterfaceDecl && !checkFinalClass(classt2)){
                    node.type = t2;
                    return;
                }
            }
            if (classt2.typeDecl instanceof InterfaceDecl){
                if (classt1.typeDecl instanceof InterfaceDecl && !checkFinalClass(classt1)){
                    node.type = t2;
                    return;
                }
            }
        } // if classOrInterfaceType
        throw new SemanticError("Cannot cast " + t1 + " to " + t2);
    }

    @Override
    public void visit(MethodInvocation node) {
        Referenceable resMethod = null;
        boolean isObject = false;
        Map<String, List<ASTNode>> containMap = null;
        /** decide which map to use*/
        if (classBodyDecl instanceof FieldDecl){
            containMap = hierarchyChecker.inheritMapRe.get(currTypeDecl);
        }   else {
            containMap = hierarchyChecker.containMap.get(currTypeDecl);
        }

        if (node.hasName()){
            Map<String, List<ASTNode>> declareMap = hierarchyChecker.declareMapRe.get(currTypeDecl);
            Name methodName = node.getName();
            //ambiguousNameBaseCase(methodName.getFullName(), declareMap);
            resMethod = findStaticMethodfix(env.ASTNodeToScopes.get(node), node.getName(), node.getArgumentTypeList());
            if (resMethod!= null){
                node.type = resMethod.getType();
                node.whichMethod = (Callable)resMethod;
                return;
            }
            String methodNameStr = methodName.getValue();
            String[] receiver_str = methodNameStr.split("\\.");
            Expr receiver = null;
            if (receiver_str.length == 1) {
                receiver = new ThisLiteral(Arrays.asList(), "this");
                if (containMap.containsKey(methodNameStr)){
                    resMethod = tools.fetchMethod(containMap.get(methodNameStr), node.getArgumentTypeList());
                    if (!checkStaticUse(resMethod)) throw new SemanticError("cannot use non-static method " + resMethod + " in static class member decl");
                } // if
            }   else {
                String res = "";
                for (int idx = 0; idx < receiver_str.length-1; idx++){
                    res += receiver_str[idx];
                    if (idx != receiver_str.length-2){
                        res += '.';
                    }
                }
                receiver = PostFixExpr.get(res, null);
                env.ASTNodeToScopes.put(receiver, env.ASTNodeToScopes.get(node));
                disAmbiguousNameField(((PostFixExpr)receiver).getName(), receiver, containMap);
                String method = receiver_str[receiver_str.length-1];
                if (receiver.type instanceof ClassOrInterfaceType){
                    ClassOrInterfaceType classType = (ClassOrInterfaceType)receiver.type;
                    containMap = hierarchyChecker.containMap.get(classType.typeDecl);
                    if (containMap.containsKey(method)){
                        resMethod = tools.fetchMethod(containMap.get(method),node.getArgumentTypeList());
                        checkProtected(resMethod, classType.typeDecl);
                        if (resMethod == null){
                            resMethod = tools.fetchAbstractMethod(containMap.get(method), node.getArgumentTypeList());
                            checkProtected(resMethod, classType.typeDecl);
                            if (checkObjectStatic(resMethod)){
                                throw new SemanticError("cannot read static method on Object " + classType);
                            }
                        }   else{
                            checkProtected(resMethod, classType.typeDecl);
                            if (checkObjectStatic(resMethod)){
                                throw new SemanticError("cannot read static method on Object " + classType);
                            }
                        }
                    } // if
                }   else if (receiver.type instanceof ArrayType){
                    ClassDecl objectDecl = (ClassDecl) env.lookup(tools.nameConstructor("java.lang.Object"));
                    containMap = hierarchyChecker.containMap.get(objectDecl);
                    if (containMap.containsKey(method)){
                        resMethod = tools.fetchMethod(containMap.get(method),node.getArgumentTypeList());
                        checkProtected(resMethod, objectDecl);
                        if (resMethod == null){
                            resMethod = tools.fetchAbstractMethod(containMap.get(method), node.getArgumentTypeList());
                            checkProtected(resMethod, objectDecl);
                            if (checkObjectStatic(resMethod)){
                                throw new SemanticError("cannot read static method on Object " + objectDecl);
                            }
                        }   else{
                            checkProtected(resMethod, objectDecl);
                            if (checkObjectStatic(resMethod)){
                                throw new SemanticError("cannot read static method on Object " + objectDecl);
                            }
                        }
                    } // if
                }  else{
                    throw new SemanticError("cannot call function " + method + " on non-class type");
                }
            }
            node.receiver = receiver;
        } /* if has name */   else {
            Primary primary = node.getPrimary();
            if (primary.type instanceof ClassOrInterfaceType){
                TypeDecl typeDecl = ((ClassOrInterfaceType)primary.type).typeDecl;
                containMap = hierarchyChecker.containMap.get(typeDecl);
                if (containMap.containsKey(node.getID().value)){
                    node.receiver = primary;
                    resMethod = tools.fetchMethod(containMap.get(node.getID().value), node.getArgumentTypeList());
                    checkProtected(resMethod, typeDecl);
                } // if
            }
        } // general if
        if (resMethod!= null){
            /*if (resMethod instanceof MethodDecl){
                System.out.println("method " + ((MethodDecl)resMethod).getName() + " has receiver " + node.receiver);
            }*/
            node.type = resMethod.getType();
            node.whichMethod = (Callable)resMethod;
            return;
        }

        throw new SemanticError("Cannot evaluate method invocation " + node.getName().getValue() + " " + node.getArgumentTypeList() + " find " + resMethod);
    }


    public void visit2(MethodInvocation node) {
        Referenceable resMethod = null;
        boolean isObject = false;
        Map<String, List<ASTNode>> containMap = null;
        /** decide which map to use*/
        if (classBodyDecl instanceof FieldDecl){
            containMap = hierarchyChecker.inheritMapRe.get(currTypeDecl);
        }   else {
            containMap = hierarchyChecker.containMap.get(currTypeDecl);
        }

        if (node.hasName()){
            Map<String, List<ASTNode>> declareMap = hierarchyChecker.declareMapRe.get(currTypeDecl);
            Name methodName = node.getName();
            //ambiguousNameBaseCase(methodName.getFullName(), declareMap);
            resMethod = findStaticMethodfix(env.ASTNodeToScopes.get(node), node.getName(), node.getArgumentTypeList());
            if (resMethod!= null){
                node.type = resMethod.getType();
                node.whichMethod = (Callable)resMethod;
                return;
            }

            List<String> names = node.getName().getFullName();
            String nameStr = "";
            Type currType = null;
            Referenceable currRes = null;
            Expr currExpr = null;
            int idx = 0;
            for (;idx < names.size();idx++){
                String str = names.get(idx);
                nameStr += str;
                if (isLastIdx(idx, names.size())){  // check method case
                    if (containMap.containsKey(nameStr)){
                        node.receiver = new ThisLiteral(null, "this");
                        resMethod = tools.fetchMethod(containMap.get(nameStr), node.getArgumentTypeList());
                        if (!checkStaticUse(resMethod)) throw new SemanticError("cannot use non-static method " + resMethod + " in static class member decl");
                        if (resMethod != null) break;
                    } // if
                }   else {
                    currRes = context.get(nameStr);
                    if (currRes != null) {
                        if (localVarDecl != null && nameStr.equals(localVarDecl.getVarDeclarators().getFirstName())) throw new SemanticError("Local variable decl " + nameStr + " cannot use it self");
                        checkProtected(currRes, null);
                        currType = currRes.getType();
                        isObject = true;
                        node.receiver = PostFixExpr.get(nameStr, currRes);
                        break;
                    }
                    // second check inherit map
                    if (containMap.containsKey(nameStr)){
                        FieldDecl  field = tools.fetchField(containMap.get(nameStr));
                        if (field != null){
                            isObject = true;
                            checkProtected(field, null);
                            currType = field.getType();
                            break;
                        }
                    }

                    if (idx == 0){
                        Token simpleName = tools.simpleNameConstructor(str);
                        ScopeEnvironment scopeEnvironment = env.ASTNodeToScopes.get(node);
                        Referenceable refer = scopeEnvironment.lookupTypeDecl(simpleName);
                       // System.out.println(str + " has type " + refer);
                        if (refer != null && refer instanceof TypeDecl){
                            currType = tools.getClassType(str, (TypeDecl)refer);
                            break;
                        }
                    }
                    // third check static field, which is env lookup: A.B.C
                    Referenceable nameRefer = env.lookup(tools.nameConstructor(nameStr));
                    if (nameRefer != null && nameRefer instanceof FieldDecl){
                        isObject = true;
                        if (!tools.checkStatic(((FieldDecl) nameRefer).getModifiers())) throw new SemanticError(nameStr + " is non static");
                        FieldDecl field = (FieldDecl)nameRefer;
                        TypeDecl typeDecl = env.ASTNodeToScopes.get(field).typeDecl;
                        checkProtected(field, typeDecl);
                        currType = field.getType();
                        break;
                    }   else if (nameRefer != null && nameRefer instanceof TypeDecl){
                        currType = tools.getClassType(nameStr, (TypeDecl)nameRefer);
                        break;
                    }
                    nameStr += '.';
                }   // else
            } // for
            idx++;
            if (resMethod == null) resMethod = evalMethod(currType, node.getName(), idx, node.getArgumentTypeList(), isObject);

        } /* if has name */   else {
            Primary primary = node.getPrimary();
            if (primary.type instanceof ClassOrInterfaceType){
                TypeDecl typeDecl = ((ClassOrInterfaceType)primary.type).typeDecl;
                containMap = hierarchyChecker.containMap.get(typeDecl);
                if (containMap.containsKey(node.getID().value)){
                    resMethod = tools.fetchMethod(containMap.get(node.getID().value), node.getArgumentTypeList());
                    checkProtected(resMethod, typeDecl);
                } // if
            }
        } // general if
        if (resMethod!= null){
            node.type = resMethod.getType();
            node.whichMethod = (Callable)resMethod;
            return;
        }

        throw new SemanticError("Cannot evaluate method invocation " + node.getName().getValue() + " " + node.getArgumentTypeList() + " find " + resMethod);
    }

    private boolean isLastIdx(int idx, int size){
        return idx == (size-1);
    }

    @Override
    public void visit(PostFixExpr node) {
        Map<String, List<ASTNode>> map = null;
        if (classBodyDecl instanceof FieldDecl){
            map = hierarchyChecker.inheritMapRe.get(currTypeDecl);
        }   else {
            //System.out.println(currTypeDecl);
            map = hierarchyChecker.containMap.get(currTypeDecl);
        }
        disAmbiguousNameField(node.getName(), node, map);
    }

    /** stmts */

    @Override
    public void visit(ReturnStmt node) {
        if (returnType == null && node.getExpr() != null) {
            /** constructor needs no return type */
            throw new SemanticError("value return in constructor :"+ node.getExpr().type);
        }
        if (node.getExpr()!= null){
            if (!isAssignable(returnType, node.getExpr().type, env)) throw new SemanticError("return type " + returnType + " does not match " + node.getExpr().type);
        }
    }
}
