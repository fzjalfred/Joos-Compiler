package visitors;
import java.util.*;

import ast.*;
import ast.Expr;
import exception.SemanticError;
import tir.src.joosc.ir.ast.*;
import tir.src.joosc.ir.ast.Name;
import tir.src.joosc.ir.interpret.Configuration;
import type.RootEnvironment;
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
            ClassDecl classDecl = (ClassDecl) env.ASTNodeToScopes.get(f).typeDecl;
            if (i == fields.size()-1){
                fieldsReadCodes.stmts().add(new Move(res, new BinOp(BinOp.OpType.ADD,res,  new Const(classDecl.fieldMap.get(f)))));
            }   else {
                fieldsReadCodes.stmts().add(new Move(res, new Mem(new BinOp(BinOp.OpType.ADD,res,  new Const(classDecl.fieldMap.get(f))))));
            }


        }
        return new ESeq(fieldsReadCodes, new Mem(res));
    }

    public IRTranslatorVisitor(RootEnvironment env){
        this.env = env;
    }

    public void visit(CompilationUnit node) {
        compUnit = new CompUnit(node.fileName);
        compUnit.oriType = node.selfDecl;
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
            
            // depth first
            for (ASTNode c: acs.children) {
                c.accept(this);
            }
            ((Assignment)child).getAssignmentRight().accept(this);

            List <Statement> stmts = new ArrayList<Statement>();
            Temp ta = new Temp("ta");
            tir.src.joosc.ir.ast.Expr e1 = null;
            if (acs.hasName()) {
                e1 = new Temp(acs.getName().getValue());
            } else {
                e1 = acs.getExpr().ir_node;
            }
            stmts.add(new Move(ta, e1));
            // // null check
            nullcheck(stmts, acs, ta);

            Temp ti = new Temp("ti");
            stmts.add(new Move(ti, acs.getDimExpr().ir_node));
            
            // // bounds check
            boundcheck(stmts, acs, ta, ti);

            Temp res = new Temp("res");
            stmts.add(new Move(res, new Mem(new BinOp(BinOp.OpType.ADD, ta, new BinOp(BinOp.OpType.MUL, ti, new Const(4))))));
            acs.ir_node = new ESeq(new Seq(stmts), res);
            
            // array assignment
            Temp te = new Temp("te");
            stmts.add(new Move(te, ((Assignment)child).getAssignmentRight().ir_node));
            stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, ta, new BinOp(BinOp.OpType.MUL, new Const(4), ti))), te));
            node.ir_node = new Seq(stmts);

        } else if (child instanceof Assignment){

            Assignment assignmentChild = (Assignment)child;
            Expr_c right_res = assignmentChild.getAssignmentRight().ir_node;
            Expr_c left_res = assignmentChild.getAssignmentLeft().ir_node;
            System.out.println("left has " + left_res + " right has " + right_res);
            node.ir_node = new Move(left_res, right_res);
        } else {
            node.ir_node = new Exp(node.getExpr().ir_node);
        }
    }


    public void visit(Assignment node){
        node.ir_node = node.getAssignmentRight().ir_node;
    }

    public void visit(FieldAccess node){
        System.out.println(node.getPrimary() + " is field access");
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
            ClassDecl classDecl = (ClassDecl)env.ASTNodeToScopes.get(method_decl).typeDecl;
            int offset = classDecl.methodMap.get(method_decl);
            funcAddr = new Mem(new BinOp(BinOp.OpType.ADD, vtable, new Const(offset)));
        } else if (node.whichMethod instanceof AbstractMethodDecl) {
            AbstractMethodDecl interface_method = (AbstractMethodDecl)node.whichMethod;
            Expr_c itable = null;
            callingMethod = ((AbstractMethodDecl)node.whichMethod).getName() + "_"+ node.whichMethod.hashCode();
            // search itable and find method decl
            // case: this.x
            if (node.hasName()) {
                if (node.receiver instanceof ThisLiteral){
                    Temp _this = new Temp("_THIS");
                    args.add(_this);
                    itable = new Mem(_this);
                }   else {
                    PostFixExpr _receiver = (PostFixExpr)node.receiver;
                    Expr_c _receiver_code = translateFieldAccess(_receiver.first_receiver, _receiver.subfields);
                    args.add(_receiver_code);
                    itable = new Mem(_receiver_code);
                }
            } else {
                args.add(node.getPrimary().ir_node);
                itable = new Mem(node.getPrimary().ir_node);
            }
            InterfaceDecl classDecl = (InterfaceDecl)env.ASTNodeToScopes.get(interface_method).typeDecl;
            System.out.println("=======================");
            System.out.println(interface_method.getName());
            System.out.println(classDecl.getName());
            int offset = classDecl.interfaceMethodMap.get(interface_method);
            funcAddr = new Mem(new BinOp(BinOp.OpType.ADD, itable, new Const(offset)));
        }
        
        if(node.getArgumentList() != null) {
            args.addAll(node.getArgumentList().ir_node);
        }
        
        node.ir_node = new Call(funcAddr, args);
        ((Call)node.ir_node).funcLabel = callingMethod;
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
        node.ir_node = new Move(new Temp(node.getVarDeclarators().getFirstName()), varDecl.ir_node);
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

    public void visit(MultiplicativeExpr node){
        Expr_c expr_c1 = node.getOperatorLeft().ir_node;
        Expr_c expr_c2 = node.getOperatorRight().ir_node;;
        if (node.getOperator().equals("*")){
            node.ir_node = new BinOp(BinOp.OpType.MUL,expr_c1, expr_c2);
        }   else if (node.getOperator().equals("/")){
            node.ir_node = new BinOp(BinOp.OpType.DIV,expr_c1, expr_c2);
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
        Expr_c expr_c2 = node.getOperatorRight().ir_node;;
        if (node.getOperator().equals(">")) {
            node.ir_node = new BinOp(BinOp.OpType.GT, expr_c1, expr_c2);
        } else if (node.getOperator().equals("<")) {
            node.ir_node = new BinOp(BinOp.OpType.LT, expr_c1, expr_c2);
        } else if (node.getOperator().equals(">=")) {
            node.ir_node = new BinOp(BinOp.OpType.GEQ, expr_c1, expr_c2);
        } else if (node.getOperator().equals("<=")) {
            node.ir_node = new BinOp(BinOp.OpType.LEQ, expr_c1, expr_c2);
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
        List <Statement> seq_stmts = new ArrayList<Statement>();
        for (ASTNode stmt: whileStmt.children){
            Stmt stmt1 = (Stmt)stmt;
            seq_stmts.add(stmt1.ir_node);
        }

        stmts.add(label);
        stmts.addAll(conditional_stmts);
        stmts.add(true_label);
        stmts.add(new Seq(seq_stmts));
        stmts.add(new Jump(new Name(label.name())));
        stmts.add(false_label);

        node.ir_node = new Seq(stmts);
    }

    public void visit(DimExpr node) {
        node.ir_node = node.getSingleChild().ir_node;
    }

    public void nullcheck(List<Statement> stmts, Expr node, Expr_c expr_c){
        Label null_exception_label = new Label("null_exception_label"+node.hashCode()+node.recursive_dectecter);
        Label ok_label = new Label("ok_label"+node.hashCode()+node.recursive_dectecter);
        stmts.add(new CJump(new BinOp(BinOp.OpType.EQ, expr_c, new Const(0)), null_exception_label.name(), ok_label.name()));
        stmts.add(null_exception_label);
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
            e1 = new Temp(node.getName().getValue());
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
        stmts.add(new Move(res, new Mem(new BinOp(BinOp.OpType.ADD, ta, new BinOp(BinOp.OpType.MUL, ti, new Const(4))))));
        node.ir_node = new ESeq(new Seq(stmts), res);

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
        //stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, new Const(4), tm)), ...));

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
                    System.out.println("bug");
                }
                stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, heapStart, new Const(fieldOffset))), expr.ir_node));
            } else {
                stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, heapStart, new Const(fieldOffset))), new Const(0)));
            }
        }

        // class's CDV
        // calc vtable size

        stmts.add(new Move(new Mem(heapStart), new Name(compUnit.oriType.getName()+ "_VTABLE")));
        List <MethodDecl> methodDecls = initClass.getMethodDecls();

        size = methodDecls.size() * 4 + 4;
        Temp reg = new Temp("blah_"+node.hashCode());
        stmts.add(new Move(reg, new Call(new Name("__malloc"), new Const(size))));
        stmts.add(new Move(new Mem(heapStart), reg));
        Temp VThead = new Temp("VThead_" + node.hashCode());
        stmts.add(new Move(VThead, new Mem(heapStart)));
        Temp t = new Temp("t_" + node.hashCode());
//        int methodIndex = 1;
        for (MethodDecl methodDecl : initClass.methodMap.keySet()) {
            String name = methodDecl.getName() + "_" + methodDecl.hashCode();
            if (!initClass.selfMethodMap.contains(methodDecl)) compUnit.externStrs.add(name);
            int methodOffset = initClass.methodMap.get(methodDecl);
            stmts.add(new Move(t, new Name(name)));
            stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, VThead, new Const(methodOffset))), t));
        }

        // class's IDV
        // find all interface methods
        Map<AbstractMethodDecl, Integer> methods_in_itable = initClass.interfaceMethodMap;
        ClassDecl parentInterfaceMethod_iterater = initClass.parentClass;
        while(parentInterfaceMethod_iterater != null) {
            methods_in_itable.putAll(parentInterfaceMethod_iterater.interfaceMethodMap);
            parentInterfaceMethod_iterater = parentInterfaceMethod_iterater.parentClass;
        }
        // calc itable size
        //int N = (int)Math.ceil(Math.log(methods_in_itable.size())/Math.log(2));
        size = 4*methods_in_itable.size();
        Temp itable_reg = new Temp("itableStart_"+node.hashCode());
        stmts.add(new Move(itable_reg, new Call(new Name("__malloc"), new Const(size))));
        // stmts.add(new Move(itable_reg, new BinOp(BinOp.OpType.ADD, itable_reg, new Const(4)))); // first block is for bitmask
        stmts.add(new Move(new Mem(VThead), itable_reg));
        
        // add methods to itable
        for (AbstractMethodDecl itable_method: methods_in_itable.keySet()) {
            String name = null;
            for (MethodDecl vtable_method : initClass.methodMap.keySet()) {
                if (tools.get_sig(itable_method, env).equals(tools.get_sig(vtable_method, env)) ) {
                    name = vtable_method.getName() + "_" + vtable_method.hashCode();
                    break;
                }
            } // done the linking.
            assert name != null;
            int methodOffset = methods_in_itable.get(itable_method);
            stmts.add(new Move(t, new Name(name)));
            stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, itable_reg, new Const(methodOffset))), t));
        }

        // calling constructor like method invocation
        String consName = callingConstructor.getName() + "_" + callingConstructor.hashCode();
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
