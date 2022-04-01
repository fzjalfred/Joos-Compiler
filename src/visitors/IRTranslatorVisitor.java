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
             Label null_exception_label = new Label("null_exception_label"+acs.hashCode());
             Label ok_label = new Label("stmt_ok_label"+acs.hashCode());
             stmts.add(new CJump(new BinOp(BinOp.OpType.EQ, e1, new Const(0)), null_exception_label.name(), ok_label.name()));
             stmts.add(null_exception_label);
             stmts.add(new Exp(new Call(new Name("__exception"))));
             stmts.add(ok_label);

            Temp ti = new Temp("ti");
            stmts.add(new Move(ti, acs.getDimExpr().ir_node));
            
            // // bounds check
             Label negative_index_exception = new Label("negative_index_exception"+acs.hashCode());
             Label lower_bound_ok_label = new Label("lower_bound_ok_label"+acs.hashCode());

             Label index_exception_label = new Label("index_exception_label"+acs.hashCode());
             Label bound_ok_label = new Label("bound_ok_label"+acs.hashCode());

            // //check 0<=ti
             stmts.add(new CJump(new BinOp(BinOp.OpType.LEQ, new Const(0), ti), lower_bound_ok_label.name(), negative_index_exception.name()));
             stmts.add(negative_index_exception);
             stmts.add(new Exp(new Call(new Name("__exception"))));
             stmts.add(lower_bound_ok_label);

            // //check ti<size
             stmts.add(new CJump(new BinOp(BinOp.OpType.LT, ti, new Mem(new BinOp(BinOp.OpType.SUB, ta, new Const(4*2)))), bound_ok_label.name(), index_exception_label.name()));
             stmts.add(index_exception_label);
             stmts.add(new Exp(new Call(new Name("__exception"))));
             stmts.add(bound_ok_label);

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
            node.ir_node = new Move(assignmentChild.getAssignmentLeft().ir_node, assignmentChild.getAssignmentRight().ir_node);
        } else {
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
        if(node.getArgumentList() != null) {
            List<tir.src.joosc.ir.ast.Expr> args = node.getArgumentList().ir_node;
            node.ir_node = new Call(funcAddr, args);
        }else{
            List <tir.src.joosc.ir.ast.Expr> exprList = new ArrayList<tir.src.joosc.ir.ast.Expr>();
            node.ir_node = new Call(funcAddr, exprList);
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

    public void visit(DimExpr node) {
        node.ir_node = node.getSingleChild().ir_node;
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
         Label null_exception_label = new Label("null_exception_label"+node.hashCode()+node.recursive_dectecter);
         Label ok_label = new Label("ok_label"+node.hashCode()+node.recursive_dectecter);
         stmts.add(new CJump(new BinOp(BinOp.OpType.EQ, ta, new Const(0)), null_exception_label.name(), ok_label.name()));
         stmts.add(null_exception_label);
         stmts.add(new Exp(new Call(new Name("__exception"))));
         stmts.add(ok_label);

        Temp ti = new Temp("ti");
        stmts.add(new Move(ti, node.getDimExpr().ir_node));
        
        // // bounds check
         Label negative_index_exception = new Label("negative_index_exception"+node.hashCode()+node.recursive_dectecter);
         Label lower_bound_ok_label = new Label("lower_bound_ok_label"+node.hashCode()+node.recursive_dectecter);

         Label index_exception_label = new Label("index_exception_label"+node.hashCode()+node.recursive_dectecter);
         Label bound_ok_label = new Label("bound_ok_label"+node.hashCode()+node.recursive_dectecter);

        // //check 0<=ti
         stmts.add(new CJump(new BinOp(BinOp.OpType.LEQ, new Const(0), ti), lower_bound_ok_label.name(), negative_index_exception.name()));
         stmts.add(negative_index_exception);
         stmts.add(new Exp(new Call(new Name("__exception"))));
         stmts.add(lower_bound_ok_label);

        // //check ti<size
         stmts.add(new CJump(new BinOp(BinOp.OpType.LT, ti, new Mem(new BinOp(BinOp.OpType.SUB, ta, new Const(4*2)))), bound_ok_label.name(), index_exception_label.name()));
         stmts.add(index_exception_label);
         stmts.add(new Exp(new Call(new Name("__exception"))));
         stmts.add(bound_ok_label);

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
         stmts.add(new CJump(new BinOp(BinOp.OpType.LEQ, new Const(0), tn), lower_bound_ok_label.name(), negative_check.name()));
         stmts.add(negative_check);
         stmts.add(new Exp(new Call(new Name("__exception"))));
         stmts.add(lower_bound_ok_label);

        Temp tm = new Temp("tm");
        stmts.add(new Move(tm, new Call(new Name("__malloc"), new BinOp(BinOp.OpType.ADD, new Const(4*2), new BinOp(BinOp.OpType.MUL, new Const(4), tn)))));
        stmts.add(new Move(new Mem(tm), tn));
        //TODO dispatch vector
        //stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, new Const(4), tm)), ...));

        // loop to clear memory locations.
        Temp cleaner = new Temp("cleaner");
        stmts.add(new Move(cleaner, new BinOp(BinOp.OpType.ADD, new Const(4*2), tm)));

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

}
