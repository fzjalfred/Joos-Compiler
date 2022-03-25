package visitors;
import java.util.*;

import ast.*;
import ast.Expr;
import tir.src.joosc.ir.ast.*;


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
                stmts.add(new Move(new Temp(p.getVarDeclaratorID().getName()), new Temp("arg_" + index)));
                index++;
            }
        }
        node.ir_node = stmts;
    }

    public void visit(MethodBody node) {
        node.ir_node = node.getBlock().ir_node;
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

    public void visit(AndExpr node) {
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

    public List<Statement> getConditionalIRNode(Expr expr, String l, String lt, String lf) {
        return new ArrayList<Statement>();
    }

    public void visit(IfThenStmt node) {
        List <Statement> stmts = new ArrayList<Statement>();
        Label label = new Label("label_" + node.hashCode());
        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());

        Stmt thenStmt = (Stmt) node.getThenStmt();
        List <Statement> conditional_stmts = getConditionalIRNode(node.getExpr(), label.name(), true_label.name(), false_label.name());
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
//        List <Statement> seq_stmts = new ArrayList<Statement>();
//        for (ASTNode stmt: thenStmt.children){
//            Stmt stmt1 = (Stmt)stmt;
//            seq_stmts.add(stmt1.ir_node);
//        }

        List <Statement> conditional_stmts = getConditionalIRNode(node.getExpr(), label.name(), true_label.name(), false_label.name());
        stmts.addAll(conditional_stmts);
        stmts.add(true_label);
        stmts.add(thenStmt.ir_node);
        stmts.add(false_label);

        node.ir_node = new Seq(stmts);
    }

    public void visit(WhileStmt node) {
        Label label = new Label("label_" + node.hashCode());
        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());
        List<Statement> conditional_stmts = getConditionalIRNode(node.getExpr(),label.name(), true_label.name(), false_label.name());

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
        stmts.add(new Jump(new Temp(label.name())));
        stmts.add(false_label);

        node.ir_node = new Seq(stmts);
    }

}
