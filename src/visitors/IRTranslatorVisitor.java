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
        node.ir_node = new Move(null, null);
    }
}
