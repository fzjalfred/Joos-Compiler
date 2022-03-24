package visitors;
import java.util.*;

import ast.*;
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
        currFunc = new FuncDecl(node.getName(), node.getMethodHeader().getMethodDeclarator().numParams(), null);
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

}
