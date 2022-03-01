package visitors;
import ast.*;
import exception.SemanticError;
import hierarchy.HierarchyChecking;
import type.*;
import utils.tools;

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

    /** Stmts */
    @Override
    public void visit(FieldDecl node) {
        String var = node.getVarDeclarators().getFirstName();
        context.put(var, node.getType());
    }

    @Override
    public void visit(LocalVarDecl node) {
        String var = node.getVarDeclarators().getFirstName();
        context.put(var, node.getType());
    }

    @Override
    public void visit(Parameter node) {
        //System.out.println("visited parameter");
        String var = node.getVarDeclaratorID().getName();
        context.put(var, node.getType());
    }

    @Override
    public void visit(MethodDecl node) {
        this.returnType = node.getMethodHeader().getType();
    }


    @Override
    public void visit(PostFixExpr node) {
        /** first check whether it's static */


        /** derive first expr's type  */
        Map<String, List<ASTNode>> contain = hierarchyChecker.containMap.get(currTypeDecl);
        List<String> names = node.getName().getFullName();
        String nameStr = "";
        Type currType = null;
        int idx = 0;
        for (;idx < names.size();idx++){
            String str = names.get(idx);
            nameStr += str;
            currType = context.get(nameStr);
            if (currType != null) break;

            // second check contain map
            if (contain.containsKey(nameStr) && contain.get(nameStr).get(0) instanceof FieldDecl){
                FieldDecl field = (FieldDecl)contain.get(nameStr).get(0);
                currType = field.getType();
                break;
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
            nameStr += '.';

        }

        /** process remaining name */
        idx++;
        for (; idx < names.size(); idx++){
            if (currType instanceof PrimitiveType) throw new SemanticError(nameStr.substring(0, nameStr.length()-1)+ " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
            String str = names.get(idx);
            nameStr += str;
            if (currType instanceof ArrayType && str.equals("length")){
                currType = new NumericType(tools.empty(), "int");
            }   else if (currType instanceof ClassOrInterfaceType){
                ClassDecl classDecl1 = (ClassDecl)currTypeDecl;
                ClassOrInterfaceType classType = (ClassOrInterfaceType)currType;
                assert classType.typeDecl != null;
                contain = hierarchyChecker.containMap.get(classType.typeDecl);
                if (contain.containsKey(str) && contain.get(str).get(0) instanceof FieldDecl){
                    FieldDecl field = (FieldDecl)contain.get(str).get(0);
                    currType = field.getType();
                    nameStr += '.';
                }   else {
                    throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
                }
            }   else {
                throw new SemanticError(nameStr + " has been inferred to type " + currType + "; so " + node.getName().getValue() + " cannot be resolved to type");
            }
        }

        if (currType!= null){
            node.type = currType;
        }   else {
            throw new SemanticError(nameStr + " cannot be resolved to type");
        }

    }

    @Override
    public void visit(ForInit node) {
        if (node.isVarDecl()){
            context.put(node.getVarDeclarator().getName(), node.getType());
        }
        //TODO: stmt case
    }


}
