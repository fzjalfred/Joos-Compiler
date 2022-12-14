package visitors;
import ast.*;
import dataflowAnalysis.CFG;
import exception.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UnreachableStmtVisitor extends Visitor{
    public Map<Referenceable, CFG> mapping;
    public CFG currCFG;
    public CFG.Vertex currVertex;
    public List<CFG.Vertex> ifpaths; // handle if else division
    public String fileName; // used for debug


    public UnreachableStmtVisitor(){
        mapping = new HashMap<Referenceable, CFG>();
        currCFG = null;
        currVertex = null;
        fileName = "";
        ifpaths = new ArrayList<CFG.Vertex>();
    }


    public void visit(CompilationUnit node){
        fileName = node.fileName;
    }

    public void visit(MethodDecl node){
        CFG cfg = new CFG(fileName);
        currCFG = cfg;
        currVertex = cfg.START;
        mapping.put(node, cfg);
    }

    public void visit(BoolLiteral node){
        if (node.value.equals("true")){
            node.boolStruct = new Expr.BoolStruct(true);
        }   else {
            node.boolStruct = new Expr.BoolStruct(false);
        }
    }

    public void visit(ConditionalAndExpr node){
        Expr left = node.getOperatorLeft();
        Expr right = node.getOperatorRight();
        if (left.boolStruct != null && right.boolStruct != null){
            node.boolStruct = new Expr.BoolStruct(left.boolStruct.bool && right.boolStruct.bool);
        }
    }

    public void visit(ConditionalOrExpr node){
        Expr left = node.getOperatorLeft();
        Expr right = node.getOperatorRight();
        if (left.boolStruct != null && right.boolStruct != null){
            node.boolStruct = new Expr.BoolStruct(left.boolStruct.bool || right.boolStruct.bool);
        }
    }

    public void visit(ConstructorDecl node){
        CFG cfg = new CFG(fileName);
        currCFG = cfg;
        currVertex = cfg.START;
        mapping.put(node, cfg);
    }

    public void visit(ReturnStmt node){
        currVertex = currCFG.addVertex(node, currVertex, ifpaths);
        
    }

    public void visit(EmptyStmt node){
        currVertex = currCFG.addVertex(node, currVertex, ifpaths);
        
    }

    public void visit(ExprStmt node){
        currVertex = currCFG.addVertex(node, currVertex, ifpaths);
        
    }

    public void visit(LocalVarDecl node){
        currVertex = currCFG.addVertex(node, currVertex, ifpaths);
        
    }

    public void visit(Block node){
        if (node.getBlockStmts() == null){
            currVertex = currCFG.addVertex(node, currVertex, ifpaths);
            
        }
    }

    public void visit(WhileStmt node){
        node.getExpr().accept(this);
        if (node.getExpr().boolStruct != null && !node.getExpr().boolStruct.bool) throw new SemanticError("unreachable stmts " + node.getStmt() + " in while(false)");
        ConditionalStmt conditionalStmt = new ConditionalStmt(node.getExpr());
        CFG.Vertex conditionVertex = currCFG.addVertex(conditionalStmt, currVertex, ifpaths);
        
        currVertex = conditionVertex;

        /** build edges between stmts in while */
        node.getStmt().accept(this);

        /** build edge between last stmt and conditionstmt*/
        currCFG.setEdge(currVertex, conditionVertex);
        if (node.getExpr().boolStruct != null && node.getExpr().boolStruct.bool){
            currVertex = null;
        }   else {
            currVertex = conditionVertex;
        }
    }

    
    // dataflow
    //                               ____________________________
    //                               |                           |
    //                               V                           |
    // ForStmt = ForInit --> ConditionalStmt --> stmts --> ForUpdate
    public void visit(ForInit node){
        currVertex = currCFG.addVertex(node, currVertex, ifpaths);
        
    }

    public void visit(ForUpdate node){
        currVertex = currCFG.addVertex(node, currVertex, ifpaths);
        
    }

    public void visit(ForStmt node) {
        // add ForInit to CFG
        node.getForInit().accept(this);

        // add ConditionalStmt
        Expr expr = node.getForExpr();
        if (expr != null) {
            expr.accept(this);
        }
        
        if (expr.boolStruct != null && !expr.boolStruct.bool) throw new SemanticError("unreachable stmts " + node.getBlockStmt() + " in for(;false;)");
        ConditionalStmt conditionalStmt = new ConditionalStmt(expr);
        CFG.Vertex conditionVertex = currCFG.addVertex(conditionalStmt, currVertex, ifpaths);
        
        currVertex = conditionVertex; 

        /** build edges from forExpr to stmts */
        if (node.getBlockStmt() != null)
        node.getBlockStmt().accept(this);

        // add ForUpdate
        if (node.getForUpdate() != null)
        node.getForUpdate().accept(this);

        currCFG.setEdge(currVertex, conditionVertex);
        /** build edges from ForUpdate back to ConditionalStmt */
        if (expr.boolStruct != null && expr.boolStruct.bool) {
            currVertex = null;
        } else  {
            currVertex = conditionVertex;
        }

    }

    //dataflow
    // IfThenStmt = conditionalStmt -> blockstmt  -> null
    //                      |                          ^
    //                      |--------------------------|
    public void visit(IfThenStmt node) {
        Expr expr = node.getExpr();
        BlockStmt ifstmt = node.getThenStmt();
        if (expr.boolStruct!= null && !expr.boolStruct.bool) {
            // ignore this if statement
        } else if (expr.boolStruct!= null && expr.boolStruct.bool) {
            ifstmt.accept(this);
        } else {
            expr.accept(this);
            ConditionalStmt conditionalStmt = new ConditionalStmt(expr);
            CFG.Vertex conditionVertex = currCFG.addVertex(conditionalStmt, currVertex, ifpaths);
            
            currVertex = conditionVertex;
            ifstmt.accept(this);
            CFG.Vertex ifpath_tmp = currVertex;
            currVertex = conditionVertex;
            ifpaths.add(ifpath_tmp);
        }
    }

    //dataflow
    // IfThenStmt = conditionalStmt -> blockstmt  -> null
    //                      |                          ^
    //                      |--------> elsestmt -------|
    public void visit(IfThenElseStmt node) {
        Expr expr = node.getExpr();
        BlockStmt ifstmt = node.getThenStmt();
        BlockStmt elsestmt = node.getElseStmt();

        if (expr.boolStruct!= null && !expr.boolStruct.bool) {
            elsestmt.accept(this);
        } else if (expr.boolStruct!= null && expr.boolStruct.bool) {
            ifstmt.accept(this);
        } else {
            ConditionalStmt conditionalStmt = new ConditionalStmt(expr);
            CFG.Vertex conditionVertex = currCFG.addVertex(conditionalStmt, currVertex, ifpaths);
            
            currVertex = conditionVertex;
            ifstmt.accept(this);
            CFG.Vertex ifpath_tmp = currVertex;
            currVertex = conditionVertex;
            elsestmt.accept(this);
            ifpaths.add(ifpath_tmp);
        }
    }
    

}
