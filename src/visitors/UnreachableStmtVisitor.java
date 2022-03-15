package visitors;
import ast.*;
import dataflowAnalysis.CFG;
import exception.SemanticError;

import java.util.HashMap;
import java.util.Map;


public class UnreachableStmtVisitor extends Visitor{
    public Map<Referenceable, CFG> mapping;
    public CFG currCFG;
    public CFG.Vertex currVertex;
    public String fileName; // used for debug


    public UnreachableStmtVisitor(){
        mapping = new HashMap<Referenceable, CFG>();
        currCFG = null;
        currVertex = null;
        fileName = "";
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
        currVertex = currCFG.addVertex(node, currVertex);
    }

    public void visit(ForInit node){
        currVertex = currCFG.addVertex(node, currVertex);
    }

    public void visit(ForUpdate node){
        currVertex = currCFG.addVertex(node, currVertex);
    }

    public void visit(EmptyStmt node){
        currVertex = currCFG.addVertex(node, currVertex);
    }

    public void visit(ExprStmt node){
        currVertex = currCFG.addVertex(node, currVertex);
    }

    public void visit(LocalVarDeclStmt node){
        currVertex = currCFG.addVertex(node, currVertex);
    }

    public void visit(WhileStmt node){
        node.getExpr().accept(this);
        if (node.getExpr().boolStruct != null && !node.getExpr().boolStruct.bool) throw new SemanticError("unreachable stmts " + node.getStmt() + " in while(false)");
        ConditionalStmt conditionalStmt = new ConditionalStmt(node.getExpr());
        CFG.Vertex conditionVertex = currCFG.addVertex(conditionalStmt, currVertex);
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


}
