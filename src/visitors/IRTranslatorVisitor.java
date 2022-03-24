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
        Label label = new Label(node.getName() + " " + node.hashCode());
        stmts.add(label);
        // move params
        MethodDeclarator methodDeclarator = node.getMethodHeader().getMethodDeclarator();
        if (methodDeclarator.hasParameterList()) {
            ParameterList parameterList = methodDeclarator.getParameterList();
            int index = 0;
            for (Parameter p : parameterList.getParams()) {
                // FIXME:: not sure about temp(arg)
                stmts.add(new Move(new Temp("x"+index), new Temp(p.value)));
                index++;
            }
        }
        Seq seq_node = (Seq)node.getMethodBody().getBlock().ir_node;

        stmts.addAll(seq_node.stmts());
        currFunc = new FuncDecl(node.getName(), node.getMethodHeader().getMethodDeclarator().numParams(), new Seq(stmts));
        node.funcDecl = currFunc;
        mapping.put(node, currFunc);
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
        node.ir_node = new Return(node.getExpr().ir_node);
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
        return null;
    }

    public void visit(IfThenStmt node) {
        List <Statement> stmts = new ArrayList<Statement>();
        Label label = new Label("label_" + node.hashCode());
        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());

        BlockStmt thenStmt = node.getThenStmt();
        List <Statement> seq_stmts = new ArrayList<Statement>();
        for (ASTNode stmt: thenStmt.children){
            Stmt stmt1 = (Stmt)stmt;
            seq_stmts.add(stmt1.ir_node);
        }

        List <Statement> conditional_stmts = getConditionalIRNode(node.getExpr(), label.name(), true_label.name(), false_label.name());
        stmts.addAll(conditional_stmts);
        stmts.add(true_label);
        stmts.add(new Seq(seq_stmts));
        stmts.add(false_label);

        node.ir_node = new Seq(stmts);

//        if (node.getExpr() instanceof AndExpr) {
//            AndExpr andExpr = (AndExpr) node.getExpr();
//            stmts.add(new CJump(andExpr.getOperatorLeft().ir_node, label.name(), false_label.name()));
//            stmts.add(label);
//            stmts.add(new CJump(andExpr.getOperatorRight().ir_node, true_label.name(), false_label.name()));
//            stmts.add(true_label);
//            stmts.add(new Seq(seq_stmts));
//            stmts.add(false_label);
//        } else {
//            List <Statement> stmts =
//        }
    }

    public void visit(IfThenElseStmt node) {
        List <Statement> stmts = new ArrayList<Statement>();
        Label label = new Label("label_" + node.hashCode());
        Label true_label = new Label("true_"+node.hashCode());
        Label false_label = new Label("false_"+node.hashCode());

        BlockStmt thenStmt = node.getThenStmt();
        List <Statement> seq_stmts = new ArrayList<Statement>();
        for (ASTNode stmt: thenStmt.children){
            Stmt stmt1 = (Stmt)stmt;
            seq_stmts.add(stmt1.ir_node);
        }

        List <Statement> conditional_stmts = getConditionalIRNode(node.getExpr(), label.name(), true_label.name(), false_label.name());
        stmts.addAll(conditional_stmts);
        stmts.add(true_label);
        stmts.add(new Seq(seq_stmts));
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
//           stmts.add(new Jump(label));
        stmts.add(false_label);

        node.ir_node = new Seq(stmts);
    }

}
