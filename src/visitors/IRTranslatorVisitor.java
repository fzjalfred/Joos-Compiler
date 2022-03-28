package visitors;
import java.util.*;

import ast.*;
import ast.Expr;
import tir.src.joosc.ir.ast.*;
import tir.src.joosc.ir.ast.Name;
import tir.src.joosc.ir.interpret.Configuration;


public class IRTranslatorVisitor extends Visitor {
    public FuncDecl currFunc;
    public Map<MethodDecl, FuncDecl> mapping;

    public IRTranslatorVisitor(){
        this.currFunc = null;
        mapping = new HashMap<MethodDecl, FuncDecl>();
    }

    @Override
    public void visit(MethodDecl node) {
        List <Statement> stmts = new ArrayList<Statement>();
        // create label
        Label label = new Label(node.getName() + "_" + node.hashCode());
        stmts.add(label);
        // move params
        if (node.getMethodHeader().ir_node != null) {
            stmts.addAll(node.getMethodHeader().ir_node);
        }
        Seq seq_node = (Seq)node.getMethodBody().getBlock().ir_node;

        stmts.add(seq_node);
        Seq body = new Seq(stmts);

        node.funcDecl = new FuncDecl(node.getName(), node.getMethodHeader().getMethodDeclarator().numParams(), body);
        currFunc = node.funcDecl;
        mapping.put(node, node.funcDecl);
    }

    public void visit(MethodHeader node) {
        List <Statement> stmts = new ArrayList<Statement>();
        MethodDeclarator methodDeclarator = node.getMethodDeclarator();
        if (methodDeclarator.hasParameterList()) {
            ParameterList parameterList = methodDeclarator.getParameterList();
            int index = 0;
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
        if (child instanceof Assignment){
            Assignment assignmentChild = (Assignment)child;
            node.ir_node = new Move(assignmentChild.getAssignmentLeft().ir_node, assignmentChild.getAssignmentRight().ir_node);
        }   else {
            node.ir_node = new Exp(node.getExpr().ir_node);
        }
    }

    public void visit(Assignment node){
        node.ir_node = node.getAssignmentRight().ir_node;
    }

    public void visit(LHS node){
        if (node.hasName()){
            node.ir_node = new Temp(node.getName().getValue());
        }
    }

    public void visit(MethodInvocation node) {
        tir.src.joosc.ir.ast.Expr funcAddr = new Name(node.getName().getValue());
        List<tir.src.joosc.ir.ast.Expr> args = node.getArgumentList().ir_node;
        node.ir_node = new Call(funcAddr, args);
    }

    public void visit(Block node){
        List<Statement> stmts = new ArrayList<Statement>();
        for (ASTNode stmt: node.getBlockStmts().children){
            Stmt stmt1 = (Stmt)stmt;
            stmts.add(stmt1.ir_node);
        }
        node.ir_node = new Seq(stmts);
    }

    public void visit(LocalVarDecl node){
        node.ir_node = new Move(new Temp(node.getVarDeclarators().getFirstName()), node.getVarDeclarators().getLastVarDeclarator().getExpr().ir_node);
    }

    public void visit(PostFixExpr node){
        node.ir_node = new Temp(node.getName().getValue());
    }

    public void visit(NumericLiteral node){
        node.ir_node = new Const(node.integer_value.intValue());
    }

    public void visit(AdditiveExpr node){
        if (node.isPlusOperator()){
            node.ir_node = new BinOp(BinOp.OpType.ADD, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        }   else {
            node.ir_node = new BinOp(BinOp.OpType.SUB, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
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
        if (node.getOperator().equals("*")){
            node.ir_node = new BinOp(BinOp.OpType.MUL,node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        }   else if (node.getOperator().equals("/")){
            node.ir_node = new BinOp(BinOp.OpType.DIV,node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        }   else {
            node.ir_node = new BinOp(BinOp.OpType.MOD,node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        }
    }

    public void visit(ReturnStmt node) {
        if (node.getExpr() != null) {
            node.ir_node = new Return(node.getExpr().ir_node);
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

        stmts.add(new Move(temp, new Const(0)));
        stmts.add(new CJump(node.getOperatorLeft().ir_node, true_label.name(), false_label.name()));
        stmts.add(false_label);
        stmts.add(new Move(temp, node.getOperatorRight().ir_node));
        stmts.add(true_label);

        node.ir_node = new ESeq(new Seq(stmts), temp);
    }

    public void visit(EqualityExpr node) {
        if (node.getOperator().equals("==")) {
            node.ir_node = new BinOp(BinOp.OpType.EQ, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        } else if (node.getOperator().equals("!=")) {
            node.ir_node = new BinOp(BinOp.OpType.NEQ, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        }
    }

    public void visit(RelationExpr node) {
        if (node.getOperator().equals(">")) {
            node.ir_node = new BinOp(BinOp.OpType.GT, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        } else if (node.getOperator().equals("<")) {
            node.ir_node = new BinOp(BinOp.OpType.LT, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        } else if (node.getOperator().equals(">=")) {
            node.ir_node = new BinOp(BinOp.OpType.GEQ, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        } else if (node.getOperator().equals("<=")) {
            node.ir_node = new BinOp(BinOp.OpType.LEQ, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
        }
    }

    public void visit(PrimaryNoArray node){
        node.ir_node = node.getExpr().ir_node;
    }

    public List<Statement> getConditionalIRNode(Expr expr, String lt, String lf) {
        List<Statement> stmts = new ArrayList<Statement>();
        if (expr.boolStruct != null && expr.boolStruct.bool == true){
            // C[true, lt, lf]
            stmts.add(new Jump(new Temp(lt)));  
        } else if (expr.boolStruct != null && expr.boolStruct.bool == false) {
            // C[false, lt, lf]
            stmts.add(new Jump(new Temp(lf)));  
        } else if (expr instanceof PrimaryNoArray && expr.getSingleChild() instanceof Expr) {
            return getConditionalIRNode(expr.getSingleChild(), lt, lf);
        } else if (expr instanceof UnaryExprNotPlusMinus && ((UnaryExprNotPlusMinus)expr).isNot()) {
            stmts.addAll(getConditionalIRNode(((UnaryExprNotPlusMinus)expr).getUnaryExpr(), lf, lt));
            
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals("<")) {
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.LT, expr.getOperatorLeft().ir_node, expr.getOperatorRight().ir_node),
                lt, lf));
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals(">")) {
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.GT, expr.getOperatorLeft().ir_node, expr.getOperatorRight().ir_node),
                lt, lf));
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals("<=")) {
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.LEQ, expr.getOperatorLeft().ir_node, expr.getOperatorRight().ir_node),
                lt, lf));
        } else if (expr instanceof RelationExpr && ((RelationExpr)expr).getOperator().equals(">=")) {
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.GEQ, expr.getOperatorLeft().ir_node, expr.getOperatorRight().ir_node),
                lt, lf));
        } else if (expr instanceof EqualityExpr && ((EqualityExpr)expr).getOperator().equals("==")) {
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.EQ, expr.getOperatorLeft().ir_node, expr.getOperatorRight().ir_node),
                lt, lf));
        } else if (expr instanceof EqualityExpr && ((EqualityExpr)expr).getOperator().equals("!=")) {
            stmts.add(new CJump(
                new BinOp(BinOp.OpType.NEQ, expr.getOperatorLeft().ir_node, expr.getOperatorRight().ir_node),
                lt, lf));
        } else if (expr instanceof ConditionalAndExpr){
            Label l = new Label("condAndlabel_" + expr.hashCode());
            stmts.addAll(getConditionalIRNode(expr.getOperatorLeft(), l.name(), lf));
            stmts.add(l);
            stmts.addAll(getConditionalIRNode(expr.getOperatorRight(), lt, lf));
        } else if (expr instanceof ConditionalOrExpr) {
            Label l = new Label("condOrlabel_" + expr.hashCode());
            stmts.addAll(getConditionalIRNode(expr.getOperatorLeft(), lt, l.name()));
            stmts.add(l);
            stmts.addAll(getConditionalIRNode(expr.getOperatorRight(), lt, lf));
        } else {
            //C[E[expr], lt, lf]
            stmts.add(new CJump(expr.ir_node, lt, lf));
        }
        
        return stmts;
    }

    public void visit(UnaryExpr node){
        node.ir_node = new BinOp(BinOp.OpType.SUB, new Const(0), node.getUnaryExpr().ir_node);
    }

    public void visit(UnaryExprNotPlusMinus node) {
        node.ir_node = new BinOp(BinOp.OpType.XOR, new Const(1), node.getUnaryExpr().ir_node);
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
        System.out.println(thenStmt.ir_node);
        stmts.add(thenStmt.ir_node);
        stmts.add(false_label);
        stmts.add(elseStmt.ir_node);

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

}
