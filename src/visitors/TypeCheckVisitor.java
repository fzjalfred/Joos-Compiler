package visitors;
import ast.*;
import exception.SemanticError;
import hierarchy.HierarchyChecking;
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


    public TypeCheckVisitor(RootEnvironment env, HierarchyChecking hierarchyChecker){
        context = new Context();
        this.env = env;
        this.hierarchyChecker = hierarchyChecker;
        this.returnType = null;
        this.currTypeDecl = null;
    }

    @Override
    public void visit(NumericLiteral node) {
        node.type = new PrimitiveType(tools.empty(), "int");
    }

    @Override
    public void visit(StringLiteral node) {
        node.type = new ClassOrInterfaceType(tools.list(tools.nameConstructor("java.lang.String")), "");
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
    }

    @Override
    public void visit(ForInit node) {
        if (node.isVarDecl()){
            context.put(node.getVarDeclarator().getName(), node);
        }
        //TODO: stmt case
    }


    /** Exprs */
    private boolean checkUpCast(ClassOrInterfaceType t1, ClassOrInterfaceType t2){
        TypeDecl t1Decl = t1.typeDecl;
        TypeDecl t2Decl = t2.typeDecl;

        //tools.println(hierarchyChecker.parentMap.get(t1Decl) + " p for t1 " + t1.typeDecl.getType());
        return tools.containClass(hierarchyChecker.parentMap.get(t1Decl), t2Decl);
    }

    private boolean checkDownCast(ClassOrInterfaceType t1, ClassOrInterfaceType t2){
        TypeDecl t1Decl = t1.typeDecl;
        TypeDecl t2Decl = t2.typeDecl;
        //tools.println(hierarchyChecker.parentMap.get(t2Decl) + " p for t2  " + t2.typeDecl.getType());
        return tools.containClass(hierarchyChecker.parentMap.get(t2Decl), t1Decl);
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

        if (t1 instanceof ClassOrInterfaceType && t2 instanceof ClassOrInterfaceType){
            ClassOrInterfaceType classt1 = (ClassOrInterfaceType)t1;
            ClassOrInterfaceType classt2 = (ClassOrInterfaceType)t2;
            /** case 3: upcast */
            if (checkUpCast(classt1, classt2)){
                tools.println("cast " + classt1 + " to " + classt2 + " is upcast", DebugID.zhenyan);
                node.type = t2;
                return;
            }

            /** case 4: down cast*/
            if (checkDownCast(classt1, classt2)){
                tools.println("cast " + classt1 + " to " + classt2 + " is upcast", DebugID.zhenyan);
                node.type = t2;
                return;
            }
            // TODO: interface cases
        }
        // TODO: throw error
    }

    @Override
    public void visit(MethodInvocation node) {
        if (node.hasName()){
            Name methodName = node.getName();

            if (methodName.type != null) {
                node.type = methodName.type;
                return;
            }

            Map<String, List<ASTNode>> containMap = null;
            Map<String, List<ASTNode>> inheritMap = hierarchyChecker.inheritMapRe.get(currTypeDecl);
            List<String> names = node.getName().getFullName();
            String nameStr = "";
            Type currType = null;
            Referenceable resMethodList = null;
            Referenceable resMethod = null;
            int idx = 0;
            for (;idx < names.size();idx++){
                String str = names.get(idx);
                nameStr += str;
                if (isLastIdx(idx, names.size())){  // check method case
                    resMethodList = context.getMethods(nameStr);    // first check context methods
                    if (resMethodList != null) break;
                    if (inheritMap.containsKey(nameStr)){
                        resMethod = tools.fetchMethod(inheritMap.get(nameStr), node.getArgumentTypeList());
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

            if (resMethod!= null){
                node.type = resMethod.getType();
                return;
            }   else if (resMethodList != null){
                assert resMethodList instanceof MethodList;
                resMethod = ((MethodList)resMethodList).match(node.getArgumentTypeList());
                if (resMethod != null){
                    node.type = resMethod.getType();
                    return;
                }
            }
            //throw new SemanticError(nameStr + " cannot be resolved to type"); TODO: uncomment this when finish all expr type check

        }   else {
            //TODO: deal with primary
        }
    }

    private boolean isLastIdx(int idx, int size){
        return idx == (size-1);
    }

    @Override
    public void visit(PostFixExpr node) {
        /** first check whether it's static */
        if (node.getName().type != null){
            node.type = node.getName().type;
            return;
        }

        /** derive first expr's type  */
        Map<String, List<ASTNode>> containMap = hierarchyChecker.containMap.get(currTypeDecl);
        Map<String, List<ASTNode>> inheritMap = hierarchyChecker.inheritMapRe.get(currTypeDecl);
        List<String> names = node.getName().getFullName();
        String nameStr = "";
        Type currType = null;

        int idx = 0;
        for (;idx < names.size();idx++){
            String str = names.get(idx);
            nameStr += str;
            currType = context.getType(nameStr);
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
            if (currType instanceof PrimitiveType) throw new SemanticError(nameStr.substring(0, nameStr.length()-1)+ " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
            String str = names.get(idx);
            if (currType instanceof ArrayType && str.equals("length")){
                nameStr += str;
                currType = new NumericType(tools.empty(), "int");
                nameStr += '.';
            }   else if (currType instanceof ClassOrInterfaceType){
                ClassDecl classDecl1 = (ClassDecl)currTypeDecl;
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
                throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
            }   else {
                throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
            } // if
        } // for

        if (currType!= null){
            node.type = currType;
            tools.println("assign " + currType + " to " + node.getName().getValue(), DebugID.zhenyan);
        }   else {
            throw new SemanticError(nameStr + " cannot be resolved to type");
        }

    }




}
