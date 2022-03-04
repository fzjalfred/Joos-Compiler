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
        Map<String, List<ASTNode>> inheritMap = hierarchyChecker.inheritMapRe.get(currTypeDecl);
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
            if (inheritMap.containsKey(nameStr)){
                FieldDecl  field = tools.fetchField(inheritMap.get(nameStr));
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
        node.type = new PrimitiveType(tools.empty(), "bool");
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
            Type expr = node.getUnaryExpr().type;
            if (!(expr instanceof PrimitiveType && expr.getNameString() == "bool")) {
                throw new SemanticError("expression after '!' is not boolean.");
            }
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
            } else if (t1 instanceof ClassOrInterfaceType && tools.get_class_qualifed_name((ClassOrInterfaceType)t1, env).equals("java.lang.String") && t2 != null) {
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
                
                throw new SemanticError("NumericType or String are needed for +, - .");
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
                
                throw new SemanticError("NumericType are needed for *, /, % .");
            }
        }
    }

    public void visit(RelationExpr node){
        if (node.children.size() == 1) {
            node.type = (node.getSingleChild()).type;
        } else if (((Token)node.children.get(1)).type == sym.INSTANCEOF) {
            node.type = new PrimitiveType(tools.empty(), "bool");
        } else {
            Type t1 = (node.getOperatorLeft()).type;
            Type t2 = (node.getOperatorRight()).type;
            if (t1 instanceof NumericType && t2 instanceof NumericType) {
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else if (t1 instanceof NumericType && t2 instanceof NumericType ) {
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else {
                throw new SemanticError("Invalid RelationExpr use between "+ node.getOperatorLeft().toString() + " "+ node.getOperatorRight().toString());
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
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else if (t2 instanceof NullType ) {
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else {
                //throw new SemanticError("Invalid EqualityExpr use between "+ node.getOperatorLeft().toString() + " : " + t1 + " "+ node.getOperatorRight().toString()  + " : " + t2);
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
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else if (t1 instanceof NumericType && t2 instanceof NumericType ) {
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else {
                throw new SemanticError("Invalid AndExpr use between "+ node.getOperatorLeft().toString() + " "+ node.getOperatorRight().toString());
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
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else if (t1 instanceof NumericType && t2 instanceof NumericType ) {
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else {
                throw new SemanticError("Invalid OrExpr use between "+ node.getOperatorLeft().toString() + " "+ node.getOperatorRight().toString());
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
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else if (t1 instanceof NumericType && t2 instanceof NumericType ) {
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else {
                throw new SemanticError("Invalid ConditionalAndExpr use between "+ node.getOperatorLeft().toString() + " "+ node.getOperatorRight().toString());
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
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else if (t1 instanceof NumericType && t2 instanceof NumericType ) {
                node.type = new PrimitiveType(tools.empty(), "bool");
            } else {
                throw new SemanticError("Invalid ConditionalOrExpr use between "+ node.getOperatorLeft().toString() + " "+ node.getOperatorRight().toString());
            }
        }
    }

    public void visit(Assignment node){
        Type t1;
        LHS lhs = node.getAssignmentLeft();
        Type t2 = (node.getAssignmentRight()).type;

        // if () {
        //     node.type = t1;
        // } else {
        //     throw new SemanticError("Invalid Assignment use between ");
        // }
    }
    

    public void visit(AssignmentExpr node){
        node.type = (node.getSingleChild()).type;
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
        if (node.type instanceof ArrayType){
            if (field.equals("length")){
                node.type = tools.intType();
                return;
            }
        }   else if (node.type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType)node.type;
            Map<String, List<ASTNode>> containMap = hierarchyChecker.containMap.get(classType.typeDecl);
            FieldDecl fieldDecl = tools.fetchField(containMap.get(field));
            if (fieldDecl != null){
                node.type = fieldDecl.getType();
                tools.println("assign field access " + field + " to type: " + node.type, DebugID.zhenyan );
                return;
            }
        }
        // TODO: throw new SemanticError("cannot evaluate " + field + " to any type");
    }


    @Override
    public void visit(ClassInstanceCreateExpr node) {
        TypeDecl typeDecl = node.getType().typeDecl;
        if (typeDecl instanceof ClassDecl){
            ClassDecl classDecl = (ClassDecl)typeDecl;
            List<Referenceable>  declares = hierarchyChecker.declareMap.get(classDecl);
            ConstructorDecl ctorDecl = tools.fetchConstructor(declares, node.getArgumentTypeList());
            if (ctorDecl != null){
                node.type = node.getType();
                node.callable = ctorDecl;
                return;
            }
        }
        //throw new SemanticError("Cannot init interface type " + node.getType());

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
