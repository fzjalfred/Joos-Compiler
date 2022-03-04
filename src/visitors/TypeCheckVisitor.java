package visitors;
import lexer.Token;
import lexer.sym;
import ast.*;
import exception.SemanticError;
import hierarchy.HierarchyChecking;
import lexer.Token;
import type.*;
import utils.*;

import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument.Content;

public class TypeCheckVisitor extends Visitor{
    public Context context;
    public RootEnvironment env;
    public HierarchyChecking hierarchyChecker;
    public Type returnType;
    public TypeDecl currTypeDecl;
    public List<Type> numsTypes;


    public TypeCheckVisitor(RootEnvironment env, HierarchyChecking hierarchyChecker){
        context = new Context();
        this.env = env;
        this.hierarchyChecker = hierarchyChecker;
        this.returnType = null;
        this.currTypeDecl = null;
    }
    /** helpers */
    private Referenceable findStaticMethod(ScopeEnvironment scopeEnvironment, Name name, List<Type> types){
        Referenceable res = env.lookup(name);
        if (res instanceof MethodList){
            MethodList methodList = (MethodList)res;
            MethodDecl methodDecl = methodList.match(types);
            if (methodDecl != null && tools.checkStatic(methodDecl.getMethodHeader().getModifiers()) ){
                return methodDecl;
            }
        }   else if (name.children.size() == 2){
            List<String> nameStrs = name.getFullName();
            res = scopeEnvironment.lookupTypeDecl(tools.simpleNameConstructor(nameStrs.get(0)));
            if (res != null){
                TypeDecl typeDecl = (TypeDecl)res;
                Map<String, List<ASTNode>> contain = hierarchyChecker.containMap.get(typeDecl);
                MethodDecl methodDecl = tools.fetchMethod(contain.get(nameStrs.get(1)), types);
                if (tools.checkStatic(methodDecl.getMethodHeader().getModifiers())) return methodDecl;
            }
        }
        return null;
    }
    private void disAmbiguousNameField(Name name, Expr node){
        /** first check whether it's static */
        if (name.type != null){
            node.type = name.type;
            return;
        }

        /** derive first expr's type  */
        Map<String, List<ASTNode>> containMap = hierarchyChecker.containMap.get(currTypeDecl);
        List<String> names = name.getFullName();
        String nameStr = "";
        Type currType = null;

        int idx = 0;
        for (;idx < names.size();idx++){
            String str = names.get(idx);
            nameStr += str;
            currType = context.getType(nameStr);
            //tools.println("look up field" + nameStr + " get " + currType, DebugID.zhenyan);
            if (currType != null) {
                break;
            }

            // second check inherit map
            if (containMap.containsKey(nameStr)){
                FieldDecl  field = tools.fetchField(containMap.get(nameStr));
                if (field != null){
                    currType = field.getType();
                    break;
                }
            }

            // third check static field, which is env lookup: A.B.C
            Referenceable nameRefer = env.lookup(tools.nameConstructor(nameStr));
            if (nameRefer instanceof FieldDecl){
                if (!tools.checkStatic(((FieldDecl) nameRefer).getModifiers())) throw new SemanticError(nameStr + " is non static");
                FieldDecl field = (FieldDecl)nameRefer;
                currType = field.getType();
                break;
            }   else if (nameRefer instanceof ClassDecl){
                ClassDecl classDecl = (ClassDecl)nameRefer;
                currType = tools.getClassType(nameStr, classDecl);
                break;
            }

        }

        /** process remaining name */
        if (!isLastIdx(idx, names.size())) nameStr += '.';
        idx++;
        for (; idx < names.size(); idx++){
            if (currType instanceof PrimitiveType) throw new SemanticError(nameStr.substring(0, nameStr.length()-1)+ " has been inferred to type " + currType + "; so " + name.getValue() + " cannot be resolved to type");
            String str = names.get(idx);
            if (currType instanceof ArrayType && str.equals("length")){
                nameStr += str;
                currType = new NumericType(tools.empty(), "int");
                nameStr += '.';
            }   else if (currType instanceof ClassOrInterfaceType){
                ClassOrInterfaceType classType = (ClassOrInterfaceType)currType;
                assert classType.typeDecl != null;
                containMap = hierarchyChecker.containMap.get(classType.typeDecl);
                if (containMap.containsKey(str)){
                    FieldDecl  field = tools.fetchField(containMap.get(str));
                    if (field != null){
                        nameStr += str;
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

        if (currType!= null){
            node.type = currType;
            //tools.println("assign " + currType + " to " + node.getName().getValue(), DebugID.zhenyan);
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
    }

    @Override
    public void visit(StringLiteral node) {
        
        node.type = tools.getClassType("java.lang.String", (TypeDecl)env.lookup(tools.nameConstructor("java.lang.String")));
        tools.println("assign " + node.value + " to " + node.type, DebugID.zhenyan);
        
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
            disAmbiguousNameField(node.getName(), node);
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
            disAmbiguousNameField(node.getName(), node);
            e1Type = node.type;
        }   else {
            e1Type = node.getExpr().type;
        }
        if (! (e1Type instanceof ArrayType)){
            for (ASTNode i: node.children) {
                System.out.println(i);
            }
            throw new SemanticError("in array access: e1 " + e1Type + " is not array type");
        }
        if (!(node.getDimExpr().type instanceof NumericType)){
            throw new SemanticError("in array access: dimexpr " + node.getDimExpr().type + " is not numeric type");
        }
        node.type = ((ArrayType)e1Type).getType();
    }


    public void visit(UnaryExpr node){
        if (node.children.size() == 2) {
            // Not statement
            Type expr = node.getUnaryExpr().type;
            node.type = expr;
        } else if (node.children.size() == 2){
            node.type = node.getSingleChild().type;
        }
    }

    public void visit(PrimaryNoArray node){
        if (node.getExpr().type == null){
            throw new SemanticError(node.getExpr().type + " should not be null");
        }
        node.type = node.getExpr().type;
    }

    public void visit(AdditiveExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new NumericType(tools.empty(), "int");
            } else if ((t1 instanceof ClassOrInterfaceType && tools.get_class_qualifed_name((ClassOrInterfaceType)t1, env).equals("java.lang.String") && t2 != null)
            || (t2 instanceof ClassOrInterfaceType && tools.get_class_qualifed_name((ClassOrInterfaceType)t2, env).equals("java.lang.String") && t1 != null)) {
                node.type = tools.getClassType("java.lang.String", (TypeDecl)env.lookup(tools.nameConstructor("java.lang.String")));
            }   else {
                // System.out.println(node.children.get(0));
                // System.out.print("isNumericType: ");
                // System.out.println(t1 instanceof NumericType);
                // System.out.println(t1 == null);
                // System.out.println(((MethodInvocation)node.children.get(0)).getName().getValue());
                // System.out.println(((PostFixExpr)node.children.get(0)).getName().getValue());
                // System.out.println(node.children.get(2));
                // System.out.print("isNumericType: ");
                // System.out.println(t2 instanceof NumericType);
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
            } else {
                // System.out.println(node.children.get(0));
                // System.out.print("isNumericType: ");
                // System.out.println(t1 instanceof NumericType);
                // System.out.println(t1 == null);
                // System.out.println(((MethodInvocation)node.children.get(0)).getName().getValue());
                // System.out.println(((PostFixExpr)node.children.get(0)).getName().getValue());
                // System.out.println(node.children.get(2));
                // System.out.print("isNumericType: ");
                // System.out.println(t2 instanceof NumericType);
                
                throw new SemanticError("Invalid MultiplicativeExpr between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }

    public void visit(RelationExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else if (((Token)node.children.get(1)).type == sym.INSTANCEOF) {
            node.type = new PrimitiveType(tools.empty(), "boolean");
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else if (t1 != null && t2 != null && t1.equals(t2)) {
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
            } else if (t1.equals(t2)) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else if (t2 instanceof NullType ) {
                node.type = new PrimitiveType(tools.empty(), "boolean");
            } else {
                throw new SemanticError("Invalid EqualityExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }

    public void visit(AndExpr node){
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
                throw new SemanticError("Invalid AndExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
            }
        }
    }

    public void visit(OrExpr node){
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
                throw new SemanticError("Invalid OrExpr use between "+ node.getOperatorLeft()+":"+t1 + " "+ node.getOperatorRight()+":"+t2);
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

    public void visit(Expr node){
        node.type = ((AssignmentExpr)node.getSingleChild()).type;
    }

    public void visit(AssignmentExpr node){
        node.type = (node.getSingleChild()).type;
    }

    public boolean isAssignable(Type t1, Type t2, RootEnvironment env) {
        if (t2 instanceof NullType) {
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

        if (checkUpCast(t2,t1)){
            return true;
        }
        return false;
    }


    public void visit(Assignment node){
        Type t1;
        LHS lhs = node.getAssignmentLeft();
        Type t2 = (node.getAssignmentRight()).type;
        //System.out.println(node.getAssignmentLeft());
        if (lhs.hasName()) {
            //Name methodName = lhs.getName();
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
            node.type = lhs.getExpr().type;
        }
        
        //System.out.println(t2.getNameString());
        //System.out.println("====================");
    }
    

    

    public void visit(ArrayCreationExpr node){
        Type t1 = node.getType();
        node.type = new ArrayType(tools.list(t1), "");
    }

    /** Check that the name of a constructor is the same as the name of its enclosing class. */
    /** A constructor in a class other than java.lang.Object implicitly calls the zero-argument constructor of its superclass. 
     * Check that this zero-argument constructor exists. (insert superclass’s constructor into that constructor) */


    public void visit(ConstructorDecl node){
        String constructor_name = node.getName();
        String class_name = "";
        if (env.ASTNodeToScopes.get(node).typeDecl instanceof ClassDecl) {
            class_name = ((ClassDecl)env.ASTNodeToScopes.get(node).typeDecl).getName();
        } else if (env.ASTNodeToScopes.get(node).typeDecl instanceof InterfaceDecl) {
            class_name = ((InterfaceDecl)env.ASTNodeToScopes.get(node).typeDecl).getName();
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
        context.put(class_name, (Referenceable)node);

    }

    /** TypeDecls */
    @Override
    public void visit(ClassDecl node) {
        //System.out.println("visited classdecl");
        currTypeDecl = node;
    }

    @Override
    public void visit(InterfaceDecl node) {
        currTypeDecl = node;
    }

    @Override
    public void visit(FieldDecl node) {
        String var = node.getVarDeclarators().getFirstName();
        context.put(var, node);

    }


    @Override
    public void visit(LocalVarDecl node) {
        String var = node.getVarDeclarators().getFirstName();
        context.put(var, node);

        // check t1 = t2
        VarDeclarator dec = (VarDeclarator)node.getVarDeclarators().children.get(0);
        Type t1 = node.getType();
        if (dec.children.size() == 2) {
            Type t2 = dec.getExpr().type;
            if (!isAssignable(t1, t2, env)) {
                throw new SemanticError("Invalid Assignment use between " + var +":"+t1 + " and " +dec.getExpr()+":"+ t2);
            }
        }
        
    }

    @Override
    public void visit(Parameter node) {
        //System.out.println("visited parameter");
        String var = node.getVarDeclaratorID().getName();
        context.put(var, node);
    }

    @Override
    public void visit(MethodDecl node) {
        /** update return value*/
        this.returnType = node.getMethodHeader().getType();
        /** add method to context */
        context.put(node.getName(), node);
        tools.println("put " + node.getName(),DebugID.zhenyan  );
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
                node.type = tools.intType();
                return;
            }
        }   else if (node.getPrimary().type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType)node.getPrimary().type;
            Map<String, List<ASTNode>> containMap = hierarchyChecker.containMap.get(classType.typeDecl);
            FieldDecl fieldDecl = tools.fetchField(containMap.get(field));
            if (fieldDecl != null){
                node.type = fieldDecl.getType();
                tools.println("assign field access " + field + " to type: " + node.type, DebugID.zhenyan );
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
            tools.println(" declares are " + declares, DebugID.zhenyan);
            ConstructorDecl ctorDecl = tools.fetchConstructor(declares, node.getArgumentTypeList());
            if (ctorDecl != null){
                node.type = node.getType();
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
            tools.println("cast " + t1 + " to " + t2 + " is upcast", DebugID.zhenyan);
            node.type = t2;
            return;
        }

        /** case 4: down cast*/
        if (checkDownCast(t1, t2)){
            tools.println("cast " + t1 + " to " + t2 + " is down cast", DebugID.zhenyan);
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
        Referenceable resMethodList = null;
        Referenceable resMethod = null;
        Map<String, List<ASTNode>> containMap = hierarchyChecker.containMap.get(currTypeDecl);;
        Map<String, List<ASTNode>> inheritMap = hierarchyChecker.inheritMapRe.get(currTypeDecl);
        if (node.hasName()){
            Name methodName = node.getName();

            resMethod = findStaticMethod(env.ASTNodeToScopes.get(node), node.getName(), node.getArgumentTypeList());
            if (resMethod!= null){
                node.type = resMethod.getType();
                node.whichMethod = (Callable)resMethod;
                return;
            }

            List<String> names = node.getName().getFullName();
            String nameStr = "";
            Type currType = null;
            int idx = 0;
            for (;idx < names.size();idx++){
                String str = names.get(idx);
                nameStr += str;
                if (isLastIdx(idx, names.size())){  // check method case
                    if (containMap.containsKey(nameStr)){
                        resMethod = tools.fetchMethod(containMap.get(nameStr), node.getArgumentTypeList());
                        if (resMethod != null) break;
                    } // if
                }   else {
                    currType = context.getType(nameStr);
                    if (currType != null) {
                        nameStr += '.';
                        break;
                    }

                    // second check inherit map
                    if (inheritMap.containsKey(nameStr)){
                        FieldDecl  field = tools.fetchField(inheritMap.get(nameStr));
                        if (field != null){
                            currType = field.getType();
                            nameStr += '.';
                            break;
                        }
                    }

                    // third check static field, which is env lookup: A.B.C
                    Referenceable nameRefer = env.lookup(tools.nameConstructor(nameStr));
                    if (nameRefer instanceof FieldDecl){
                        if (!tools.checkStatic(((FieldDecl) nameRefer).getModifiers())) throw new SemanticError(nameStr + " is non static");
                        FieldDecl field = (FieldDecl)nameRefer;
                        currType = field.getType();
                        nameStr += '.';
                        break;
                    }   else if (nameRefer instanceof ClassDecl){
                        ClassDecl classDecl = (ClassDecl)nameRefer;
                        currType = tools.getClassType(nameStr, classDecl);
                        nameStr += '.';
                        break;
                    }
                    nameStr += '.';
                }   // else
            } // for


            idx++;
            for (; idx < names.size(); idx++){
                if (currType instanceof PrimitiveType) throw new SemanticError(nameStr.substring(0, nameStr.length()-1)+ " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
                String str = names.get(idx);
                nameStr += str;
                if (isLastIdx(idx, names.size()) && currType instanceof ClassOrInterfaceType){

                    ClassOrInterfaceType classType = (ClassOrInterfaceType)currType;
                    assert classType.typeDecl != null;
                    containMap = hierarchyChecker.containMap.get(classType.typeDecl);
                    if (containMap.containsKey(str)){
                        resMethod = tools.fetchMethod(containMap.get(str), node.getArgumentTypeList());
                    } // if
                }   else if (currType instanceof ArrayType && str.equals("length")){
                    currType = new NumericType(tools.empty(), "int");
                }   else if (currType instanceof ClassOrInterfaceType){
                    ClassOrInterfaceType classType = (ClassOrInterfaceType)currType;
                    assert classType.typeDecl != null;
                    containMap = hierarchyChecker.containMap.get(classType.typeDecl);
                    if (containMap.containsKey(str)){
                        FieldDecl  field = tools.fetchField(containMap.get(str));
                        if (field != null){
                            currType = field.getType();
                            nameStr += '.';
                            continue;
                        } // if
                    } // if
                    throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
                }   else {
                    throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
                }
            } // for
        } /* if has name */   else {
            Primary primary = node.getPrimary();
            if (primary.type instanceof ClassOrInterfaceType){
                TypeDecl typeDecl = ((ClassOrInterfaceType)primary.type).typeDecl;
                containMap = hierarchyChecker.containMap.get(typeDecl);
                if (containMap.containsKey(node.getID().value)){
                    resMethod = tools.fetchMethod(containMap.get(node.getID().value), node.getArgumentTypeList());
                } // if
            }
        } // general if
        if (resMethod!= null){
            node.type = resMethod.getType();
            node.whichMethod = (Callable)resMethod;
            return;
        }   else if (resMethodList != null){
            assert resMethodList instanceof MethodList;
            tools.println("method list is  " + resMethodList, DebugID.zhenyan);
            resMethod = ((MethodList)resMethodList).match(node.getArgumentTypeList());
            if (resMethod != null){
                node.type = resMethod.getType();
                node.whichMethod = (Callable)resMethod;
                return;
            }
            throw new SemanticError("Cannot evaluate method invocation " + node + " " + node.getArgumentTypeList());
        } // if
        // for (ASTNode i: node.children) {
        //     System.out.println(i);
        // }
        // System.out.println(((Name)node.children.get(0)).getValue());
        // System.out.println(((ArgumentList)node.children.get(2)).getArgsType());
        throw new SemanticError("Cannot evaluate method invocation " + node + " " + node.getArgumentTypeList() + " find " + resMethodList + " and " + resMethod);
    }

    private boolean isLastIdx(int idx, int size){
        return idx == (size-1);
    }

    @Override
    public void visit(PostFixExpr node) {
        disAmbiguousNameField(node.getName(), node);
    }




}
