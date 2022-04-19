package visitors;
import java.util.*;

import ast.*;
import ast.Expr;
import exception.BackendError;
import exception.SemanticError;
import tir.src.joosc.ir.ast.*;
import tir.src.joosc.ir.ast.Name;
import tir.src.joosc.ir.interpret.Configuration;
import type.RootEnvironment;
import type.ScopeEnvironment;
import utils.*;


public class IRTranslatorVisitor extends Visitor {
    public CompUnit compUnit;
    private RootEnvironment env;

    /** return the memory address of the last field: u can decide to read or write */
    public Expr_c translateFieldAccess(Referenceable first_receiver, List<FieldDecl> fields){
        Temp res = null;
        Seq fieldsReadCodes = new Seq();

        if (first_receiver instanceof ThisLiteral){
            res = new Temp("_THIS"); //fixme
        }   else {
            res = new Temp(first_receiver.toString());
        }
        if (fields.isEmpty()){
            return res;
        }   else {
            Temp temp = res;
            res = new Temp("fieldAccessRes");
            fieldsReadCodes.stmts().add(new Move(res, temp));
        }
        for (int i = 0; i < fields.size(); i++){
            FieldDecl f = fields.get(i);
            ClassDecl classDecl = null;
            ScopeEnvironment scopeEnvironment =  env.ASTNodeToScopes.get(f);
            if (scopeEnvironment != null){
                classDecl = (ClassDecl)scopeEnvironment.typeDecl;
            }
            if (i == fields.size()-1){
                if (f.value.equals("length")){
                    fieldsReadCodes.stmts().add(new Move(res, new BinOp(BinOp.OpType.SUB,res,  new Const(8))));
                }   else fieldsReadCodes.stmts().add(new Move(res, new BinOp(BinOp.OpType.ADD,res,  new Const(classDecl.fieldMap.get(f)))));
            }   else {
                fieldsReadCodes.stmts().add(new Move(res, new Mem(new BinOp(BinOp.OpType.ADD,res,  new Const(classDecl.fieldMap.get(f))))));
            }


        }
        return new ESeq(fieldsReadCodes, new Mem(res));
    }
    public tir.src.joosc.ir.ast.Expr instanceOfTestGeneral(Expr expr, ReferenceType type){
        if (type instanceof ClassOrInterfaceType) {
            ClassDecl classDecl = (ClassDecl) ((ClassOrInterfaceType) type).typeDecl;
            if (classDecl.parentClass == null) return new Const(1); // type is Object
            else if (expr.type instanceof PrimitiveType) return new Const(0);
            else if (expr.type instanceof ArrayType) return new Const(0);
            else return instanceOfTest(expr.ir_node, classDecl);
        }   else {
            ArrayType arrayType = (ArrayType)type;
            if (arrayType.equals(expr.type)) return new Const(1);
            return new Const(0);
        }
    }

    public tir.src.joosc.ir.ast.Expr instanceOfTest(Expr_c testee, ClassDecl type){
        compUnit.externStrs.add(tools.getVtable(type));
        List<Statement> stmts = new ArrayList<>();
        Temp head = new Temp("head");
        Const zeroConst = new Const(0);
        stmts.add(new Move(head , new Mem(testee)));
        Temp res = new Temp("res");
        stmts.add(new Move(res , zeroConst));
        Label targetClassVtable = new Label(tools.getVtable(type));
        Label loopLabel = new Label("loopLabel_" + tools.getLabelOffset());
        Label trueLabel = new Label("trueLabel_" + tools.getLabelOffset());
        Label successLabel = new Label("successLabel_" + tools.getLabelOffset());
        Label failLabel = new Label("failLabel_" + tools.getLabelOffset());
        Label endLabel = new Label("endLabel_" + tools.getLabelOffset());
        stmts.add(loopLabel);
        stmts.add(new CJump(new BinOp(BinOp.OpType.NEQ, head, zeroConst), trueLabel.name(), endLabel.name()));
        stmts.add(trueLabel);
        stmts.add(new CJump(new BinOp(BinOp.OpType.EQ, head, new Name(targetClassVtable.name())), successLabel.name(), failLabel.name()));
        stmts.add(successLabel);
        stmts.add(new Move(res, new Const(1)));
        stmts.add(new Jump(new Name(endLabel.name())));
        stmts.add(failLabel);
        stmts.add(new Move(head, new Mem(new BinOp(BinOp.OpType.ADD, head, new Const(4)))));
        stmts.add(new Jump(new Name(loopLabel.name())));
        stmts.add(endLabel);

        return new ESeq(new Seq(stmts), res);
    }

    public IRTranslatorVisitor(RootEnvironment env){
        this.env = env;
    }

    public void visit(CompilationUnit node) {
        compUnit = new CompUnit(node.fileName);
        compUnit.oriType = node.selfDecl;
    }

    public void visit(NullLiteral node){
        node.ir_node = new Const(0);
    }


    public void visit(ForInit node){
        Expr_c expr = node.getVarDeclarator().getExpr().ir_node;
        Temp var = new Temp(node.getVarDeclarator().getVarDeclaratorID().getName());
        node.ir_node = new Move(var, expr);
    }

    public void visit(ForUpdate node){
        node.ir_node = node.getStmt().ir_node;
    }

    public void visit(CharLiteral node) {
        char c = node.value.charAt(1);
        node.ir_node = new Const(Character.getNumericValue(c), Expr_c.DataType.Word);
    }

    public void visit(ForStmt node){
        Seq res = new Seq();
        Label beginLabel = new Label("ForLoopBegin_" + node.hashCode());
        Label nextLabel = new Label("ForLoopNext_" + node.hashCode());
        Label endLabel = new Label("ForLoopEnd_" + node.hashCode());
        if (node.getForInit() != null){
            res.stmts().add(node.getForInit().ir_node);
        }
        res.stmts().add(beginLabel);
        Expr_c forCond = new Const(1);
        List<Statement> conditional_stmts = new ArrayList<>();
        if (node.getForExpr() != null){
            conditional_stmts.addAll(getConditionalIRNode(node.getForExpr(), nextLabel.name(), endLabel.name()));
        }
        res.stmts().addAll(conditional_stmts);
        res.stmts().add(nextLabel);
        res.stmts().add(((Stmt) node.getBlockStmt()).ir_node);
        if (node.getForUpdate() != null){
            res.stmts().add(node.getForUpdate().ir_node);
        }
        res.stmts().add(new Jump(new Name(beginLabel.name())));
        res.stmts().add(endLabel);
        node.ir_node = res;
    }

    public void visit(StringLiteral node){
        String labelName = "StringLiteral"+ "_" + node.hashCode();
        if (!compUnit.stringLiteralToLabel.containsKey(node.value)){
            //System.out.println(node.value + " is not in " + compUnit.stringLiteralToLabel);
            compUnit.stringLiteralToLabel.put(node.value, labelName);
        }   else {
            labelName = compUnit.stringLiteralToLabel.get(node.value);
        }
        node.ir_node = new Name(labelName);
    }

    Expr_c.DataType getIRDataType(PrimitiveType t){
        if (t.value.equals("bool") || t.value.equals("byte")){
            return Expr_c.DataType.Byte;
        }   else if (t.value.equals("short") || t.value.equals("char")) {
            return Expr_c.DataType.Word;
        }   else {
            return Expr_c.DataType.Dword;
        }
    }

    public void visit(CastExpr node){
        if (node.type instanceof ReferenceType){
            ReferenceType referenceType = (ReferenceType)node.type;
            Expr_c right = node.getOperatorRight().ir_node;
            List<Statement> codes = new ArrayList<>();
            Label trueLabel = new Label("TrueLabel_" +node.hashCode());
            codes.add(new CJump(instanceOfTestGeneral(node.getOperatorRight(), referenceType), trueLabel.name()));
            codes.add(new Exp(new Call(new Name("__exception"))));
            codes.add(trueLabel);
            node.ir_node = new ESeq(new Seq(codes), right);
        }   else {
            Temp newTemp = new Temp("castTemp", getIRDataType((PrimitiveType)node.type));
            node.ir_node = new ESeq(new Seq(new Move(newTemp, new Const(0)),new Move(newTemp, node.getOperatorRight().ir_node)), newTemp);
        }
    }


    @Override
    public void visit(MethodDecl node) {
        List <Statement> stmts = new ArrayList<Statement>();
        String name = "";
        if (node.isTest()) {
            name = node.getName();
        } else {
            name = node.getName() + "_" + node.hashCode();
        }
        // create label
        Label label = new Label(name);
        compUnit.definedLabels.add(label.name());
        stmts.add(label);
        // move params
        if (node.getMethodHeader().ir_node != null) {
            stmts.addAll(node.getMethodHeader().ir_node);
        }
        Seq seq_node = (Seq)node.getMethodBody().getBlock().ir_node;

        int paramNum = node.getMethodHeader().getMethodDeclarator().numParams();
        if (!node.isStatic()) {
            paramNum += 1;
        }
        stmts.add(seq_node);
        Seq body = new Seq(stmts);
        node.funcDecl = new FuncDecl(name, paramNum, body, new FuncDecl.Chunk());
        compUnit.appendFunc(node.funcDecl);
    }

    public void visit(MethodHeader node) {
        List <Statement> stmts = new ArrayList<Statement>();
        MethodDeclarator methodDeclarator = node.getMethodDeclarator();
        int index = 0;
        if (!node.isStatic()) {
            stmts.add(new Move(new Temp("_THIS"), new Temp(Configuration.ABSTRACT_ARG_PREFIX + index)));
            index++;
        }
        if (methodDeclarator.hasParameterList()) {
            ParameterList parameterList = methodDeclarator.getParameterList();
            for (Parameter p : parameterList.getParams()) {
                // FIXME:: not sure about temp(arg)
                stmts.add(new Move(new Temp(p.getVarDeclaratorID().getName()), new Temp(Configuration.ABSTRACT_ARG_PREFIX + index)));
                index++;
            }
        }
        node.ir_node = stmts;
    }

    public void visit(MethodBody node) {
        node.ir_node = node.getBlock().ir_node;
    }

    public void visit(ArgumentList node) {
        List <tir.src.joosc.ir.ast.Expr> exprList = new ArrayList<tir.src.joosc.ir.ast.Expr>();
        for (Expr expr : node.getArgs()){
            exprList.add((tir.src.joosc.ir.ast.Expr) expr.ir_node);
        }
        node.ir_node = exprList;
    }



    public void visit(StmtExpr node){
        Expr child = node.getExpr();
        if (child instanceof Assignment && ((LHS) (((Assignment)child).getAssignmentLeft())).getExpr() instanceof ArrayAccess) {
            ArrayAccess acs = (ArrayAccess) ((LHS) (((Assignment)child).getAssignmentLeft())).getExpr();
            node.ir_node = new Move(acs.ir_node, ((Assignment)child).getAssignmentRight().ir_node);

        } else if (child instanceof Assignment){

            Assignment assignmentChild = (Assignment)child;
            Expr_c right_res = assignmentChild.getAssignmentRight().ir_node;
            Expr_c left_res = assignmentChild.getAssignmentLeft().ir_node;
            node.ir_node = new Move(left_res, right_res);
        } else {
            node.ir_node = new Exp(node.getExpr().ir_node);
        }
    }


    public void visit(Assignment node){
        node.ir_node = node.getAssignmentRight().ir_node;
    }

    public void visit(FieldAccess node){
        Primary receiver = node.getPrimary();
        ClassDecl classDecl = (ClassDecl)((ClassOrInterfaceType)receiver.getType()).typeDecl;
        node.ir_node = new Mem(new BinOp(BinOp.OpType.ADD, receiver.ir_node, new Const(classDecl.fieldMap.get(node.field))));
    }

    public void visit(LHS node){
        if (node.hasName()){
            if (node.refer instanceof FieldDecl){
                node.ir_node = translateFieldAccess(node.first_receiver, node.subfields);
            }   else {
                node.ir_node = new Temp(node.getName().getValue());
            }
        }   else {
            node.ir_node = node.getExpr().ir_node;
        }
    }

    public void visit(MethodInvocation node) {
        String callingMethod ="";
        // TODO interface abstract method
        // System.out.println(node.whichMethod);
        tir.src.joosc.ir.ast.Expr funcAddr = null;
        // arguments
        List<tir.src.joosc.ir.ast.Expr> args = new ArrayList<tir.src.joosc.ir.ast.Expr>();

        MethodDecl method_decl = null;
        if (node.whichMethod instanceof MethodDecl) {
            //function_addr
            Expr_c vtable = null;
            method_decl = (MethodDecl)node.whichMethod;
            if ((method_decl).isTest()){
                callingMethod = (method_decl).getName();
            } else {
                callingMethod = (method_decl).getName() + "_"+ node.whichMethod.hashCode();
            }
            if (method_decl != null && (method_decl).getModifiers().getModifiersSet().contains("static")) {
                compUnit.externStrs.add(callingMethod);
                funcAddr = new Name(callingMethod);
                node.ir_node = new Call(funcAddr, args);
                ((Call)node.ir_node).funcLabel = callingMethod;
                return;
            } else if (node.hasName()) {
               // System.out.println(node);
                if (node.receiver instanceof ThisLiteral){
                    Temp _this = new Temp("_THIS");
                    args.add(_this);
                    vtable = new Mem(_this);
                }   else {
                    PostFixExpr _receiver = (PostFixExpr)node.receiver;
                    Expr_c _receiver_code = translateFieldAccess(_receiver.first_receiver, _receiver.subfields);
                    args.add(_receiver_code);
                    vtable = new Mem(_receiver_code);
                }
            } else {
                args.add(node.getPrimary().ir_node);
                vtable = new Mem(node.getPrimary().ir_node);
            }
            if(node.getArgumentList() != null) {
                args.addAll(node.getArgumentList().ir_node);
            }
            ClassDecl classDecl = (ClassDecl)env.ASTNodeToScopes.get(method_decl).typeDecl;
            int offset = classDecl.methodMap.get(method_decl);
            funcAddr = new Mem(new BinOp(BinOp.OpType.ADD, vtable, new Const(offset)));
            node.ir_node = new Call(funcAddr, args);
            ((Call)node.ir_node).funcLabel = callingMethod;
        } else if (node.whichMethod instanceof AbstractMethodDecl) {
            
            AbstractMethodDecl interface_method = (AbstractMethodDecl)node.whichMethod;
            Expr_c vtable = null;
            List <Statement> stmts = new ArrayList<Statement>();
            // search itable and find method decl
            // case: this.x
            if (node.hasName()) {
                if (node.receiver instanceof ThisLiteral){
                    Temp _this = new Temp("_THIS");
                    args.add(_this);
                    vtable = new Mem(_this);
                }   else {
                    PostFixExpr _receiver = (PostFixExpr)node.receiver;
                    Expr_c _receiver_code = translateFieldAccess(_receiver.first_receiver, _receiver.subfields);
                    args.add(_receiver_code);
                    vtable = new Mem(_receiver_code);
                }
            } else {
                args.add(node.getPrimary().ir_node);
                vtable = new Mem(node.getPrimary().ir_node);
            }
            if(node.getArgumentList() != null) {
                args.addAll(node.getArgumentList().ir_node);
            }
            Temp vtable_addr = new Temp("vtable_addr_"+node.hashCode());
            stmts.add(new Move(vtable_addr, vtable));
            Expr_c itable = new Mem(vtable_addr);
            //InterfaceDecl classDecl = (InterfaceDecl)env.ASTNodeToScopes.get(interface_method).typeDecl;
            Temp bitmask = new Temp("bitmask_"+node.hashCode());
            stmts.add(new Move(bitmask, new BinOp(BinOp.OpType.SUB, itable, new Const(4))));
            Temp tset = new Temp("itable_offset_"+node.hashCode());
            stmts.add(new Move(tset, new BinOp(BinOp.OpType.AND, new Const(node.hashCode()), bitmask)));
            
            node.ir_node = new ESeq(new Seq(stmts), new Call(new Mem(new BinOp(BinOp.OpType.ADD, vtable, tset)), args));
            
            System.out.println("======invocation=======");
            System.out.println(interface_method.getName()+interface_method.hashCode());
            System.out.println("=======================");
            //int offset = classDecl.interfaceMethodMap.get(interface_method);
            
        }
    }

    public void visit(Block node){
        List<Statement> stmts = new ArrayList<Statement>();
        if (node.getBlockStmts() == null) {
            node.ir_node = new Seq(stmts);
            return;
        }
        for (ASTNode stmt: node.getBlockStmts().children){
            Stmt stmt1 = (Stmt)stmt;
            stmts.add(stmt1.ir_node);
        }
        node.ir_node = new Seq(stmts);
    }

    public void visit(LocalVarDecl node){
        Expr varDecl = node.getVarDeclarators().getLastVarDeclarator().getExpr();
        if (node.getType() instanceof PrimitiveType){
            Temp res = new Temp(node.getVarDeclarators().getFirstName(), getIRDataType((PrimitiveType)node.getType()));
            node.ir_node = new Move(res, new Const(0));
            node.ir_node = new Move(res, varDecl.ir_node);
        }   else {
            node.ir_node = new Move(new Temp(node.getVarDeclarators().getFirstName()), varDecl.ir_node);
        }
    }

    public void visit(PostFixExpr node){
        if (node.refer instanceof FieldDecl){
            node.ir_node = translateFieldAccess(node.first_receiver, node.subfields);
        }   else {
            node.ir_node = new Temp(node.getName().getValue());
        }
    }

    public void visit(NumericLiteral node){
        node.ir_node = new Const(node.integer_value.intValue());
    }

    public void visit(AdditiveExpr node){
        Expr_c expr_c1 = node.getOperatorLeft().ir_node;
        Expr_c expr_c2 = node.getOperatorRight().ir_node;;
        if (node.isPlusOperator()){
            node.ir_node = new BinOp(BinOp.OpType.ADD, expr_c1, expr_c2);
        }   else {
            node.ir_node = new BinOp(BinOp.OpType.SUB, expr_c1, expr_c2);
        }
    }

    public void visit(BoolLiteral node){
        if (node.value.equals("true")){
            node.ir_node = new Const(1);
        }   else {
            node.ir_node = new Const(0);
        }
    }

    public Seq divisionByZeroCheck(Expr_c expr){
        Label label1 = new Label("DivisionByZeroFalse_" + tools.getLabelOffset());
        return new Seq(new CJump(expr, label1.name()), new Exp(new Call(new Name("__exception" ))), label1);
    }

    public void visit(MultiplicativeExpr node){
        Expr_c expr_c1 = node.getOperatorLeft().ir_node;
        Expr_c expr_c2 = node.getOperatorRight().ir_node;;
        if (node.getOperator().equals("*")){
            node.ir_node = new BinOp(BinOp.OpType.MUL,expr_c1, expr_c2);
        }   else if (node.getOperator().equals("/")){
            node.ir_node = new ESeq(divisionByZeroCheck(expr_c2), new BinOp(BinOp.OpType.DIV,expr_c1, expr_c2));
        }   else {
            node.ir_node = new BinOp(BinOp.OpType.MOD,expr_c1, expr_c2);
        }
    }

    public void visit(ReturnStmt node) {
        Expr_c expr_c = node.getExpr().ir_node;
        if (node.getExpr() != null) {
            node.ir_node = new Return(expr_c);
        } else {
            node.ir_node = new Return(new Const(0)); // dummy value
        }
    }

    public void visit(ConditionalAndExpr node) {
        Temp temp = new Temp("t");
        List <Statement> stmts = new ArrayList<Statement>();

        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());

        stmts.add(new Move(temp, new Const(0)));
        stmts.add(new CJump(node.getOperatorLeft().ir_node, true_label.name(), false_label.name()));
        stmts.add(true_label);
        stmts.add(new Move(temp, node.getOperatorRight().ir_node));
        stmts.add(false_label);

        node.ir_node = new ESeq(new Seq(stmts), temp);
    }

    public void visit(ConditionalOrExpr node) {
        Temp temp = new Temp("t");
        List <Statement> stmts = new ArrayList<Statement>();

        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());

        stmts.add(new Move(temp, new Const(1)));
        stmts.add(new CJump(node.getOperatorLeft().ir_node, true_label.name(), false_label.name()));
        stmts.add(false_label);
        stmts.add(new Move(temp, node.getOperatorRight().ir_node));
        stmts.add(true_label);

        node.ir_node = new ESeq(new Seq(stmts), temp);
    }

    public void visit(EqualityExpr node) {
        Expr_c expr_c1 = node.getOperatorLeft().ir_node;
        Expr_c expr_c2 = node.getOperatorRight().ir_node;
        if (node.getOperator().equals("==")) {
            node.ir_node = new BinOp(BinOp.OpType.EQ, expr_c1, expr_c2);
        } else if (node.getOperator().equals("!=")) {
            node.ir_node = new BinOp(BinOp.OpType.NEQ, expr_c1, expr_c2);
        }
    }

    public void visit(RelationExpr node) {
        Expr_c expr_c1 = node.getOperatorLeft().ir_node;
        if (node.getOperator().equals(">")) {
            Expr_c expr_c2 = node.getOperatorRight().ir_node;;
            node.ir_node = new BinOp(BinOp.OpType.GT, expr_c1, expr_c2);
        } else if (node.getOperator().equals("<")) {
            Expr_c expr_c2 = node.getOperatorRight().ir_node;;
            node.ir_node = new BinOp(BinOp.OpType.LT, expr_c1, expr_c2);
        } else if (node.getOperator().equals(">=")) {
            Expr_c expr_c2 = node.getOperatorRight().ir_node;;
            node.ir_node = new BinOp(BinOp.OpType.GEQ, expr_c1, expr_c2);
        } else if (node.getOperator().equals("<=")) {
            Expr_c expr_c2 = node.getOperatorRight().ir_node;;
            node.ir_node = new BinOp(BinOp.OpType.LEQ, expr_c1, expr_c2);
        } else { // TODO: array type
            ReferenceType type = (ReferenceType) node.getisInstanceOfRight();
            if (type instanceof ClassOrInterfaceType){
                node.ir_node = (Expr_c) instanceOfTestGeneral(node.getOperatorLeft(), (ReferenceType)node.getisInstanceOfRight());
            }
        }
    }

    public void visit(PrimaryNoArray node){
        node.ir_node = node.getExpr().ir_node;
    }

    public List<Statement> getConditionalIRNode(Expr expr, String lt, String lf) {
        List<Statement> stmts = new ArrayList<Statement>();
        Expr_c expr_c = expr.ir_node;
        if (expr.boolStruct != null && expr.boolStruct.bool == true){
            // C[true, lt, lf]
            stmts.add(new Jump(new Name(lt)));
        } else if (expr.boolStruct != null && expr.boolStruct.bool == false) {
            // C[false, lt, lf]
            stmts.add(new Jump(new Name(lf)));
        } else if (expr instanceof PrimaryNoArray && expr.getSingleChild() instanceof Expr) {
            return getConditionalIRNode(expr.getSingleChild(), lt, lf);
        } else if (expr instanceof UnaryExprNotPlusMinus && ((UnaryExprNotPlusMinus)expr).isNot()) {
            stmts.addAll(getConditionalIRNode(((UnaryExprNotPlusMinus)expr).getUnaryExpr(), lf, lt));
            
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals("<")) {
            Expr_c expr_c1 = expr.getOperatorLeft().ir_node;
            Expr_c expr_c2 = expr.getOperatorRight().ir_node;;
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.LT, expr_c1,expr_c2),
                lt, lf));
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals(">")) {
            Expr_c expr_c1 = expr.getOperatorLeft().ir_node;
            Expr_c expr_c2 = expr.getOperatorRight().ir_node;;
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.GT, expr_c1,expr_c2),
                lt, lf));
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals("<=")) {
            Expr_c expr_c1 = expr.getOperatorLeft().ir_node;
            Expr_c expr_c2 = expr.getOperatorRight().ir_node;;
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.LEQ, expr_c1,expr_c2),
                lt, lf));
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals(">=")) {
            Expr_c expr_c1 = expr.getOperatorLeft().ir_node;
            Expr_c expr_c2 = expr.getOperatorRight().ir_node;;
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.GEQ, expr_c1,expr_c2),
                lt, lf));
        } else if (expr instanceof EqualityExpr && ((EqualityExpr)expr).getOperator().equals("==")) {
            Expr_c expr_c1 = expr.getOperatorLeft().ir_node;
            Expr_c expr_c2 = expr.getOperatorRight().ir_node;;
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.EQ, expr_c1,expr_c2),
                lt, lf));
        } else if (expr instanceof EqualityExpr && ((EqualityExpr)expr).getOperator().equals("!=")) {
            Expr_c expr_c1 = expr.getOperatorLeft().ir_node;
            Expr_c expr_c2 = expr.getOperatorRight().ir_node;;
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.NEQ, expr_c1,expr_c2),
                lt, lf));
        } else if (expr instanceof ConditionalAndExpr && expr.children.size() == 2){
            Label l = new Label("condAndlabel_" + expr.hashCode());
            stmts.addAll(getConditionalIRNode(expr.getOperatorLeft(), l.name(), lf));
            stmts.add(l);
            stmts.addAll(getConditionalIRNode(expr.getOperatorRight(), lt, lf));
        } else if (expr instanceof ConditionalOrExpr && expr.children.size() == 2) {
            Label l = new Label("condOrlabel_" + expr.hashCode());
            stmts.addAll(getConditionalIRNode(expr.getOperatorLeft(), lt, l.name()));
            stmts.add(l);
            stmts.addAll(getConditionalIRNode(expr.getOperatorRight(), lt, lf));
        } else {
            //C[E[expr], lt, lf]
            stmts.add(new CJump(expr_c, lt, lf));
        }
        
        return stmts;
    }

    public void visit(UnaryExpr node){
        Expr_c expr_c = node.getUnaryExpr().ir_node;
        node.ir_node = new BinOp(BinOp.OpType.SUB,new Const(0), expr_c);
    }

    public void visit(UnaryExprNotPlusMinus node) {
        Expr_c expr_c = node.getUnaryExpr().ir_node;
        node.ir_node = new BinOp(BinOp.OpType.XOR, expr_c, new Const(1));
    }

    public void visit(IfThenStmt node) {
        List <Statement> stmts = new ArrayList<Statement>();
        
        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());

        Stmt thenStmt = (Stmt) node.getThenStmt();
        List <Statement> conditional_stmts = getConditionalIRNode(node.getExpr(), true_label.name(), false_label.name());
        stmts.addAll(conditional_stmts);
        stmts.add(true_label);
        stmts.add(thenStmt.ir_node);
        stmts.add(false_label);

        node.ir_node = new Seq(stmts);
    }

    public void visit(IfThenElseStmt node) {
        List <Statement> stmts = new ArrayList<Statement>();
        Label label = new Label("label_" + node.hashCode());
        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());
        Label res_label = new Label("res_"+node.hashCode());

        Stmt thenStmt = (Stmt)node.getThenStmt();
        Stmt elseStmt = (Stmt)node.getElseStmt();
//        List <Statement> seq_stmts = new ArrayList<Statement>();
//        for (ASTNode stmt: thenStmt.children){
//            Stmt stmt1 = (Stmt)stmt;
//            seq_stmts.add(stmt1.ir_node);
//        }

        List <Statement> conditional_stmts = getConditionalIRNode(node.getExpr(), true_label.name(), false_label.name());
        stmts.addAll(conditional_stmts);
        stmts.add(true_label);
        stmts.add(thenStmt.ir_node);
        stmts.add(new Jump(new Name(res_label.name())));
        stmts.add(false_label);
        stmts.add(elseStmt.ir_node);
        stmts.add(res_label);

        node.ir_node = new Seq(stmts);
    }

    public void visit(WhileStmt node) {
        Label label = new Label("label_" + node.hashCode());
        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());
        List<Statement> conditional_stmts = getConditionalIRNode(node.getExpr(), true_label.name(), false_label.name());

        List<Statement> stmts = new ArrayList<Statement>();
        BlockStmt whileStmt = node.getStmt();
        stmts.add(label);
        stmts.addAll(conditional_stmts);
        stmts.add(true_label);
        stmts.add(((Stmt)(whileStmt)).ir_node);
        stmts.add(new Jump(new Name(label.name())));
        stmts.add(false_label);

        node.ir_node = new Seq(stmts);
    }

    public void visit(DimExpr node) {
        node.ir_node = node.getSingleChild().ir_node;
    }

    public void nullcheck(List<Statement> stmts, Expr node, Expr_c expr_c){
        Label ok_label = new Label("ok_label"+node.hashCode()+node.recursive_dectecter);
        stmts.add(new CJump(new BinOp(BinOp.OpType.NEQ, expr_c, new Const(0)), ok_label.name()));
        stmts.add(new Exp(new Call(new Name("__exception"))));
        stmts.add(ok_label);
    }

    public void boundcheck(List<Statement> stmts, Expr node, Expr_c ta, Expr_c ti){
        Label negative_index_exception = new Label("negative_index_exception"+node.hashCode()+node.recursive_dectecter);
        Label lower_bound_ok_label = new Label("lower_bound_ok_label"+node.hashCode()+node.recursive_dectecter);

        Label index_exception_label = new Label("index_exception_label"+node.hashCode()+node.recursive_dectecter);
        Label bound_ok_label = new Label("bound_ok_label"+node.hashCode()+node.recursive_dectecter);

        // //check 0<=ti
        stmts.add(new CJump(new BinOp(BinOp.OpType.GEQ,ti, new Const(0)), lower_bound_ok_label.name(), negative_index_exception.name()));
        stmts.add(negative_index_exception);
        stmts.add(new Exp(new Call(new Name("__exception"))));
        stmts.add(lower_bound_ok_label);

        // //check ti<size
        stmts.add(new CJump(new BinOp(BinOp.OpType.LT, ti, new Mem(new BinOp(BinOp.OpType.SUB, ta, new Const(4*2)))), bound_ok_label.name(), index_exception_label.name()));
        stmts.add(index_exception_label);
        stmts.add(new Exp(new Call(new Name("__exception"))));
        stmts.add(bound_ok_label);
    }

    public void visit(ArrayAccess node){
        List <Statement> stmts = new ArrayList<Statement>();
        Temp ta = new Temp("ta");
        tir.src.joosc.ir.ast.Expr e1 = null;
        if (node.hasName()) {
            e1 = translateFieldAccess(node.first_receiver, node.subfields);
        } else {
            node.recursive_dectecter += "recursive_layer_";
            e1 = node.getExpr().ir_node;
        }
        stmts.add(new Move(ta, e1));
        
        // // null check
        nullcheck(stmts, node, ta);

        Temp ti = new Temp("ti");
        stmts.add(new Move(ti, node.getDimExpr().ir_node));
        
        // // bounds check
        boundcheck(stmts, node, ta, ti);

        Temp res = new Temp("res");
        stmts.add(new Move(res, new BinOp(BinOp.OpType.ADD, ta, new BinOp(BinOp.OpType.MUL, ti, new Const(4)))));
        node.ir_node = new ESeq(new Seq(stmts), new Mem(res));

    }

    public void visit(ArrayCreationExpr node) {
        List <Statement> stmts = new ArrayList<Statement>();
        Temp tn = new Temp("tn");
        
        stmts.add(new Move(tn, node.getDimExpr().ir_node));
        
        
         //check 0<=ti
         Label negative_check = new Label("negative_check"+node.hashCode());
         Label lower_bound_ok_label = new Label("lower_bound_ok_label"+node.hashCode());
         stmts.add(new CJump(new BinOp(BinOp.OpType.GEQ, tn,new Const(0)), lower_bound_ok_label.name(), negative_check.name()));
         stmts.add(negative_check);
         stmts.add(new Exp(new Call(new Name("__exception"))));
         stmts.add(lower_bound_ok_label);

        Temp tm = new Temp("tm");
        stmts.add(new Move(tm, new Call(new Name("__malloc"), new BinOp(BinOp.OpType.ADD, new BinOp(BinOp.OpType.MUL, tn, new Const(4)), new Const(4*2)))));
        stmts.add(new Move(new Mem(tm), tn));
        //TODO dispatch vector
        stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, new Const(4), tm)), new Name("Object_VTABLE")));
        compUnit.externStrs.add("Object_VTABLE");

        // loop to clear memory locations.
        Temp cleaner = new Temp("cleaner");
        stmts.add(new Move(cleaner, new BinOp(BinOp.OpType.ADD, tm, new Const(4*2))));

        Label clean = new Label("clean"+node.hashCode());
        Label cleanup_done = new Label("cleanup_done"+node.hashCode());
        stmts.add(clean);
        stmts.add(new Move(new Mem(cleaner), new Const(0)));
        stmts.add(new Move(cleaner, new BinOp(BinOp.OpType.ADD, cleaner, new Const(4))));
        stmts.add(new CJump(new BinOp(BinOp.OpType.LT, cleaner, new BinOp(BinOp.OpType.ADD, tm, new BinOp(BinOp.OpType.ADD, new Const(4*2), new BinOp(BinOp.OpType.MUL, tn, new Const(4))))), clean.name(), cleanup_done.name()));
        stmts.add(cleanup_done);


        Temp res = new Temp("res");
        stmts.add(new Move(res, new BinOp(BinOp.OpType.ADD, tm, new Const(4*2))));
        //stmts.add(new Exp(new Call(new Name("__exception"))));
        node.ir_node = new ESeq(new Seq(stmts), res);
    }

    public void visit(ClassInstanceCreateExpr node) {
        ConstructorDecl callingConstructor = (ConstructorDecl) node.callable;
        ClassDecl initClass = callingConstructor.whichClass;

        // calc heap size
        int size = 4; // vtb addr
        List <FieldDecl> fieldDecls = initClass.getAllFieldDecls();
        size += fieldDecls.size() * 4; // add 4 * field num
        List<Statement> stmts = new ArrayList<Statement>();

        Temp heapStart = new Temp("heapStart_"+node.hashCode());
        stmts.add(new Move(heapStart, new Call(new Name("__malloc"), new Const(size))));

//        int fieldOffset = 4;
//        for (FieldDecl fieldDecl : fieldDecls) {
//            initClass.fieldMap.put(fieldDecl, fieldOffset);
//            fieldOffset += 4;
//        }
        for (FieldDecl fieldDecl : initClass.fieldMap.keySet()) {
            int fieldOffset = initClass.fieldMap.get(fieldDecl);
            if (fieldDecl.hasRight()) {
                Expr expr = fieldDecl.getExpr();
                if (expr.ir_node == null) {
                    throw new BackendError("field decl not visited");
                }
                if (fieldDecl.getType() instanceof PrimitiveType){
                    Expr_c.DataType type = getIRDataType((PrimitiveType)fieldDecl.getType());
                    if (type == Expr_c.DataType.Word || type == Expr_c.DataType.Byte){
                        Temp fieldInitTemp = new Temp("fieldInitTemp", type);
                        stmts.add(new Move(fieldInitTemp, new Const(0)));
                        stmts.add(new Move(fieldInitTemp, expr.ir_node));
                        stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, heapStart, new Const(fieldOffset))), fieldInitTemp));
                    }   else {
                        stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, heapStart, new Const(fieldOffset))), expr.ir_node));
                    }
                }   else {
                    stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, heapStart, new Const(fieldOffset))), expr.ir_node));
                }
            } else {
                stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, heapStart, new Const(fieldOffset))), new Const(0)));
            }
        }

        // class's CDV
        // calc vtable size
        //System.out.println("==DEBUG==CDV=========");
        compUnit.externStrs.add(tools.getVtable(initClass));
        stmts.add(new Move(new Mem(heapStart), new Name(tools.getVtable(initClass))));


        Temp vtable_addr = new Temp("vtable_addr_create_"+node.hashCode());
        stmts.add(new Move(vtable_addr, new Mem(heapStart)));
        compUnit.externStrs.add(tools.getItable(initClass));
        stmts.add(new Move(new Mem(vtable_addr), new Name(tools.getItable(initClass))));
        // calling constructor like method invocation
        String consName = callingConstructor.getName() + "_" + callingConstructor.hashCode();
        compUnit.externStrs.add(consName);
        tir.src.joosc.ir.ast.Expr consAddr = new Name(consName);
        List <tir.src.joosc.ir.ast.Expr> exprList = new ArrayList<tir.src.joosc.ir.ast.Expr>();
        exprList.add(heapStart);
        if (node.getArgumentList() != null) {
            exprList.addAll(node.getArgumentList().ir_node);
            stmts.add(new Exp(new Call(consAddr, exprList)));
        } else {

            stmts.add(new Exp(new Call(consAddr, exprList)));
        }
        node.ir_node = new ESeq(new Seq(stmts), heapStart);
    }

    public void visit(ConstructorDeclarator node) {
        List <Statement> stmts = new ArrayList<Statement>();
        int index = 0;
        stmts.add(new Move(new Temp("_THIS"), new Temp(Configuration.ABSTRACT_ARG_PREFIX + index)));
        index++;
        if (node.hasParameterList()) {
            ParameterList parameterList = node.getParameterList();
            for (Parameter p : parameterList.getParams()) {
                stmts.add(new Move(new Temp(p.getVarDeclaratorID().getName()), new Temp(Configuration.ABSTRACT_ARG_PREFIX + index)));
                index++;
            }
        }
        node.ir_node = stmts;
    }

    public void visit(ConstructorBody node) {
        List <Statement> stmts = new ArrayList<Statement>();
        if (node.getBlockStmts() == null) {
            node.ir_node = new Seq(stmts);
            return;
        }

        for (ASTNode stmt : node.getBlockStmts().children) {
            Stmt stmt1 = (Stmt) stmt;
            stmts.add(stmt1.ir_node);
        }
        node.ir_node = new Seq(stmts);
    }

    public void visit(ConstructorDecl node) {
        List <Statement> stmts = new ArrayList<Statement>();
        String name = node.getName() + "_" + node.hashCode();
        compUnit.definedLabels.add(name);
        Label label = new Label(name);
        stmts.add(label);

        ConstructorDecl superCons = node.whichClass.supercall;
        if (superCons != null) {
            String superConsName = superCons.getName() + "_" + superCons.hashCode();
            compUnit.externStrs.add(superConsName);
            List <tir.src.joosc.ir.ast.Expr> exprList = new ArrayList<tir.src.joosc.ir.ast.Expr>();
            exprList.add(new Temp("_THIS"));
            stmts.add(new Exp(new Call(new Name(superConsName), exprList)));
        }

        // move params
        if (node.getConstructorDeclarator().ir_node != null) {
            stmts.addAll(node.getConstructorDeclarator().ir_node);
        }
        Seq seq_node = node.getConstructorBody().ir_node;

        stmts.add(seq_node);
        Seq body = new Seq(stmts);
        node.funcDecl = new FuncDecl(name, node.getConstructorDeclarator().numParams()+1, body, new FuncDecl.Chunk());
        compUnit.appendFunc(node.funcDecl);
    }

    public void visit(ThisLiteral node){
        node.ir_node = new Temp("_THIS");
    }



}
