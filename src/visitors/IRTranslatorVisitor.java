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
    private ClassDecl ObjectDecl;
    public FuncDecl currFunc;

    /** return the memory address of the last field: u can decide to read or write */
    public Expr_c translateFieldAccess(Referenceable first_receiver, List<FieldDecl> fields){
        Temp res = null;
        Seq fieldsReadCodes = new Seq();
        boolean isStatic = false;
        if (first_receiver instanceof ThisLiteral){
            res = currFunc.receiver; //fixme
        }   else if (first_receiver instanceof FieldDecl && ((FieldDecl)first_receiver).isStatic()){
            isStatic = true;
            FieldDecl _field = (FieldDecl)first_receiver;
            res = new Temp("staticAccessRes_" + first_receiver.hashCode());
            compUnit.externStrs.add(_field.getFirstVarName() + "_" + _field.hashCode());
            fieldsReadCodes.stmts().add(new Move(res, new Name((_field.getFirstVarName() + "_" + _field.hashCode()))));
        }   else {
            res = new Temp(first_receiver.toString());
        }
        if (fields.isEmpty()){
            return res;
        }   else {
            Temp temp = res;
            res = new Temp("fieldAccessRes");
            if (isStatic){
                fieldsReadCodes.stmts().add(new Move(res, new Mem(temp)));
            }   else {
                fieldsReadCodes.stmts().add(new Move(res, temp));
            }
        }
        for (int i = 0; i < fields.size(); i++){
            FieldDecl f = fields.get(i);
            ClassDecl classDecl = null;
            ScopeEnvironment scopeEnvironment =  env.ASTNodeToScopes.get(f);
            if (scopeEnvironment != null){
                classDecl = (ClassDecl)scopeEnvironment.typeDecl;
            }
            nullcheck(fieldsReadCodes.stmts(), res);
            if (i == fields.size()-1){
                if (f.value.equals("length")){
                    fieldsReadCodes.stmts().add(new Move(res, new BinOp(BinOp.OpType.SUB,res,  new Const(12))));
                }   else fieldsReadCodes.stmts().add(new Move(res, new BinOp(BinOp.OpType.ADD,res,  new Const(classDecl.fieldMap.get(f)))));
            }   else {
                fieldsReadCodes.stmts().add(new Move(res, new Mem(new BinOp(BinOp.OpType.ADD,res,  new Const(classDecl.fieldMap.get(f))))));
            }


        }
        //System.out.println("first reciever is " + first_receiver + " fields are " + fields + "res is " + new ESeq(fieldsReadCodes, new Mem(res)));
        return new ESeq(fieldsReadCodes, new Mem(res));
    }
    // TODO: null is not instanceof Others
    public tir.src.joosc.ir.ast.Expr instanceOfTestGeneral(Expr expr, ReferenceType type, Expr_c ir){
        //System.out.println("type is " + type);
        if (type instanceof ClassOrInterfaceType) {
            if (((ClassOrInterfaceType) type).typeDecl instanceof InterfaceDecl) return new Const(1);
            ClassDecl classDecl = (ClassDecl) ((ClassOrInterfaceType) type).typeDecl;
            if (classDecl.parentClass == null) return new BinOp(BinOp.OpType.NEQ, ir, new Const(0)); // type is Object
            else if (expr.type instanceof PrimitiveType) return new Const(0);
            else if (expr.type instanceof ArrayType) {
                return new Const(0);
            }
            else return instanceOfTest(ir, classDecl);
        }   else {
            ArrayType arrayType = (ArrayType)type;
            if (arrayType.equals(expr.type)) return new BinOp(BinOp.OpType.NEQ, ir, new Const(0)); // type is Object
            if (expr.type instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType)expr.type;
                if (classOrInterfaceType.typeDecl == ObjectDecl) {
                    if (arrayType.getType() instanceof ClassOrInterfaceType){
                        Seq codes = new Seq();
                        Temp right = new Temp("right_" + expr.hashCode());
                        codes.stmts().add(new Move(right, (new BinOp(BinOp.OpType.ADD, ir, new Const(4)))));
                        return  new ESeq(codes, instanceOfTest(right, (ClassDecl) ((ClassOrInterfaceType)arrayType.getType()).typeDecl));
                    }   else{
                        Seq codes = new Seq();
                        Temp right = new Temp("right_" + expr.hashCode());
                        codes.stmts().add(new Move(right, (new BinOp(BinOp.OpType.ADD, ir, new Const(4)))));
                        return new ESeq(codes, new BinOp(BinOp.OpType.EQ, right, new Const(0)));
                    }
                }
            }   else if (expr.type instanceof ArrayType && ((ArrayType)expr.type).getType() instanceof ClassOrInterfaceType && arrayType.getType() instanceof ClassOrInterfaceType){
                Seq codes = new Seq();
                Temp right = new Temp("right_" + expr.hashCode());
                codes.stmts().add(new Move(right, (new BinOp(BinOp.OpType.SUB, ir, new Const(4)))));
                return  new ESeq(codes, instanceOfTest(right, (ClassDecl) ((ClassOrInterfaceType)arrayType.getType()).typeDecl));
            }
            return new Const(0);
        }
    }

    public void visit(AndExpr node){
        node.ir_node = new BinOp(BinOp.OpType.ADD, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
    }

    public void visit(OrExpr node){
        node.ir_node = new BinOp(BinOp.OpType.OR, node.getOperatorLeft().ir_node, node.getOperatorRight().ir_node);
    }


    public tir.src.joosc.ir.ast.Expr instanceOfTest(Expr_c testee, ClassDecl type){
        compUnit.externStrs.add(tools.getVtable(type, env));
        List<Statement> stmts = new ArrayList<>();
        Temp head = new Temp("head_"+testee.hashCode());
        Temp right = new Temp("right_"+testee.hashCode());
        Const zeroConst = new Const(0);
        Label nullLabel = new Label("nullLabel_" + tools.getLabelOffset());
        stmts.add(new Move(right, testee));
        stmts.add(new CJump(new BinOp(BinOp.OpType.EQ, right, zeroConst), nullLabel.name()));
        stmts.add(new Move(head , new Mem(right)));
        Temp res = new Temp("res_"+testee.hashCode());
        stmts.add(new Move(res , zeroConst));
        Label targetClassVtable = new Label(tools.getVtable(type, env));
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
        stmts.add(nullLabel);

        return new ESeq(new Seq(stmts), res);
    }

    public IRTranslatorVisitor(RootEnvironment env){
        this.env = env;
        ObjectDecl = (ClassDecl)env.lookup(tools.nameConstructor("java.lang.Object"));
    }

    public void visit(CompilationUnit node) {
        compUnit = new CompUnit(node.fileName);
        compUnit.oriType = node.selfDecl;
        compUnit.env = env;
    }

    public void visit(NullLiteral node){
        node.ir_node = new Const(0);
    }


    public void visit(ForInit node){
        if (node.getVarDeclarator()!=null) {
            Expr_c expr = node.getVarDeclarator().getExpr().ir_node;
            Temp var = new Temp(node.getVarDeclarator().getVarDeclaratorID().getName());
            node.ir_node = new Move(var, expr);
        } else {
            node.ir_node = ((StmtExpr)node.children.get(0)).ir_node;
        }
        
    }

    public void visit(ForUpdate node){
        node.ir_node = node.getStmt().ir_node;
    }

    char getChar(String s){
        if (s.equals("\\n")) return '\n';
        if (s.equals("\\r")) return '\r';
        if (s.equals("\\t")) return '\t';
        throw new BackendError("no such char " +s );
    }

    public void visit(CharLiteral node) {
        String realChar = node.value.substring(1, node.value.length()-1);
        char c = 0;
        if (realChar.length() != 1){
            c = getChar(realChar);
        }   else{
            c = node.value.charAt(1);
        }
        node.ir_node = new Const((int)c, Expr_c.DataType.Word);
    }

    public void visit(ForStmt node){
        Seq res = new Seq();
        Label beginLabel = new Label("ForLoopBegin_" + node.hashCode());
        Label nextLabel = new Label("ForLoopNext_" + node.hashCode());
        Label endLabel = new Label("ForLoopEnd_" + node.hashCode());
        if (node.getForInit() != null && node.getForInit().ir_node != null){
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

    String parseStr(String s){
        //System.out.println("original str is " + s);
        String res = "";
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if (c == '\\' && i < s.length()-1){
                char next = s.charAt(i+1);
                if (next == 'n') res += '\n';
                if (next == 't') res += '\t';
                if (next == 'r') res += '\r';
                i++;
            }   else {
                res += c;
            }
        }
        //System.out.println("parsed str is " + res);
        return res;
    }

    public void visit(StringLiteral node){
        String parsedStr = parseStr(node.value);
        String labelName = "StringLiteral"+ "_" + node.hashCode();
        if (!compUnit.stringLiteralToLabel.containsKey(parsedStr)){
            //System.out.println(node.value + " is not in " + compUnit.stringLiteralToLabel);
            compUnit.stringLiteralToLabel.put(parsedStr, labelName);
        }   else {
            labelName = compUnit.stringLiteralToLabel.get(parsedStr);
        }
        Temp strTemp = new Temp("static_tmp_" + node.hashCode());
        node.ir_node = new ESeq(new Move(strTemp, new Name(labelName)), strTemp);
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
        Label nullLabel = new Label("nullLabel_"+node.hashCode());
        if (node.getOperatorRight().type instanceof  NullType) {
            node.ir_node = node.getOperatorRight().ir_node;
            return;
        }
        if (node.type instanceof ClassOrInterfaceType && ((ClassOrInterfaceType)(node.type)).typeDecl == ObjectDecl && node.getOperatorRight().type instanceof ArrayType){
            node.ir_node = new BinOp(BinOp.OpType.SUB, node.getOperatorRight().ir_node, new Const(8));
        }   else if (node.type instanceof ArrayType) {
            if (node.getOperatorRight().type instanceof ClassOrInterfaceType && ((ClassOrInterfaceType)node.getOperatorRight().type).typeDecl == ObjectDecl){
                if (((ArrayType)node.type).getType() instanceof ClassOrInterfaceType){
                    ClassOrInterfaceType classType = ((ClassOrInterfaceType)((ArrayType)node.type).getType());
                    Temp right = new Temp("right_"+node.hashCode());
                    Temp arrHead = new Temp("arrHead_"+node.hashCode());
                    Temp resHead = new Temp("res"+node.hashCode());
                    Seq codes = new Seq(new Move(right, node.getOperatorRight().ir_node),new Move(resHead,new BinOp(BinOp.OpType.ADD, right, new Const(8))), new Move(arrHead, new BinOp(BinOp.OpType.ADD, right, new Const(4))));
                    Label trueLabel = new Label("TrueLabel_" +node.hashCode());
                    codes.stmts().add(new CJump(instanceOfTest(arrHead, (ClassDecl)classType.typeDecl), trueLabel.name()));
                    codes.stmts().add(new Exp(new Call(new Name("__exception"))));
                    codes.stmts().add(trueLabel);
                    node.ir_node = new ESeq(codes, resHead);
                }   else {
                    Temp right = new Temp("right_"+node.hashCode());
                    Temp arrHead = new Temp("arrHead_"+node.hashCode());
                    Temp resHead = new Temp("res"+node.hashCode());
                    Seq codes = new Seq(new Move(right, node.getOperatorRight().ir_node),new Move(resHead,new BinOp(BinOp.OpType.ADD, right, new Const(8))), new Move(arrHead, new BinOp(BinOp.OpType.ADD, right, new Const(4))));
                    Label trueLabel = new Label("TrueLabel_" +node.hashCode());
                    codes.stmts().add(new CJump(new BinOp(BinOp.OpType.EQ, new Mem(arrHead), new Const(0)), trueLabel.name()));
                    codes.stmts().add(new Exp(new Call(new Name("__exception"))));
                    codes.stmts().add(trueLabel);
                    node.ir_node = new ESeq(codes, resHead);
                }
            }   else if (node.getOperatorRight().type instanceof ArrayType){
                if (((ArrayType)node.type).getType() instanceof ClassOrInterfaceType){
                    ClassOrInterfaceType classType = ((ClassOrInterfaceType)((ArrayType)node.type).getType());
                    Expr_c right = new Temp("right_"+node.hashCode());
                    Temp arrHead = new Temp("arrHead_"+node.hashCode());
                    Seq codes = new Seq(new Move(right, node.getOperatorRight().ir_node),new Move(arrHead, new BinOp(BinOp.OpType.SUB, right, new Const(4))));
                    Label trueLabel = new Label("TrueLabel_" +node.hashCode());
                    codes.stmts().add(new CJump(instanceOfTest(arrHead, (ClassDecl)classType.typeDecl), trueLabel.name()));
                    codes.stmts().add(new Exp(new Call(new Name("__exception"))));
                    codes.stmts().add(trueLabel);
                    node.ir_node = new ESeq(codes, right);
                }   else {
                    node.ir_node = node.getOperatorRight().ir_node;
                }
            }   else {
                throw new BackendError("no such cast " + node);
            }
        }   else if (node.type instanceof ClassOrInterfaceType){
            ReferenceType referenceType = (ReferenceType)node.type;
            Expr_c right = new Temp("right_"+node.hashCode());
            List<Statement> codes = new ArrayList<>();
            codes.add(new Move(right, node.getOperatorRight().ir_node));
            Label trueLabel = new Label("TrueLabel_" +node.hashCode());
            codes.add(new CJump(instanceOfTestGeneral(node.getOperatorRight(), referenceType, right), trueLabel.name()));
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

        Seq seq_node;
        if (node == ((MethodList)(env.lookup(tools.nameConstructor("java.io.OutputStream.nativeWrite")))).methods.get(0)) {
            List <Statement> seqStmts = new ArrayList<Statement>();
            List<tir.src.joosc.ir.ast.Expr> printArgs = new ArrayList<tir.src.joosc.ir.ast.Expr>();
            printArgs.add(new Temp(node.getFirstParamName()));
            seqStmts.add(new Exp(new Call(new Name("NATIVEjava.io.OutputStream.nativeWrite"), printArgs))); //debug
            seq_node = new Seq(seqStmts);
        } else {
            seq_node = (Seq)node.getMethodBody().ir_node;
        }

        int paramNum = node.getMethodHeader().getMethodDeclarator().numParams();
        if (!node.isStatic()) {
            paramNum += 1;
        }
        stmts.add(seq_node);
        Seq body = new Seq(stmts);
        node.funcDecl.body = body;
        node.funcDecl.numParams = paramNum;
        node.funcDecl.name = name;
        // if (true){
        //     System.out.println();
        //     System.out.println(node.funcDecl);
        //     System.out.println();
        // }
        //System.out.println("function decl in " + node + " is " + node.funcDecl);
        currFunc = node.funcDecl;
        compUnit.appendFunc(node.funcDecl);
    }

    public void visit(MethodHeader node) {
        List <Statement> stmts = new ArrayList<Statement>();
        MethodDeclarator methodDeclarator = node.getMethodDeclarator();
        int index = 0;
        if (!node.isStatic()) {
            stmts.add(new Move(currFunc.receiver, new Temp(Configuration.ABSTRACT_ARG_PREFIX + index)));
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
        if (node.getBlock() == null) node.ir_node = new Seq();
        else node.ir_node = node.getBlock().ir_node;
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
            node.ir_node = new Move(acs.ir_node, ((Assignment)child).getAssignmentRight().ir_node );
        } else if (child instanceof Assignment){
            Assignment assignmentChild = (Assignment)child;
            if (assignmentChild.getAssignmentLeft().type instanceof ClassOrInterfaceType && assignmentChild.getAssignmentRight().type instanceof ArrayType){
                TypeDecl decl = ((ClassOrInterfaceType)assignmentChild.getAssignmentLeft().type).typeDecl;
                if (decl != ObjectDecl) throw new BackendError("cannot assign arr type to non-Object type");
                Expr_c right_res = assignmentChild.getAssignmentRight().ir_node;
                Expr_c left_res = assignmentChild.getAssignmentLeft().ir_node;
                Temp tmp = new Temp("assArr_"+node.hashCode());
                node.ir_node = new Seq(new Move(tmp, right_res), new Move(left_res, new BinOp(BinOp.OpType.SUB, tmp, new Const(8))));
            }   else {
                Expr_c right_res = assignmentChild.getAssignmentRight().ir_node;
                Expr_c left_res = assignmentChild.getAssignmentLeft().ir_node;
                node.ir_node = new Move(left_res, right_res);
            }
        } else {
            node.ir_node = new Exp(node.getExpr().ir_node);
        }
    }


    public void visit(Assignment node){
        Expr_c left = node.getAssignmentLeft().ir_node;
        Expr_c right = node.getAssignmentRight().ir_node;
        Statement stmts = null;
        if (node.getAssignmentLeft().type instanceof ClassOrInterfaceType && node.getAssignmentRight().type instanceof ArrayType){
            TypeDecl decl = ((ClassOrInterfaceType)node.getAssignmentLeft().type).typeDecl;
            if (decl != ObjectDecl) throw new BackendError("cannot assign arr type to non-Object type");
            Temp tmp = new Temp("assArr_"+node.hashCode());
            stmts = new Seq(new Move(tmp, right), new Move(left, new BinOp(BinOp.OpType.SUB, tmp, new Const(8))));
        }   else {
            stmts = new Move(left, right);
        }
        if (left instanceof ESeq){
            node.ir_node = new ESeq(stmts, ((ESeq)left).expr());
        }   else if (left instanceof Mem && ((Mem)left).expr() instanceof ESeq){
            tir.src.joosc.ir.ast.Expr res = ((ESeq)((Mem)left).expr()).expr();
            node.ir_node = new ESeq(stmts, res);
        }   else {
            Temp res = new Temp("assignRes_"+node.hashCode());
            Seq codes = new Seq(new Move(res, node.getAssignmentRight().ir_node), new Move(node.getAssignmentLeft().ir_node, res));
            node.ir_node = new ESeq(codes,res);
        }
    }

    public void visit(FieldAccess node){
        Primary receiver = node.getPrimary();
    	if (receiver.getType() instanceof ArrayType){
		            node.ir_node = new Mem(new BinOp(BinOp.OpType.SUB, receiver.ir_node, new Const(12)));
			            }   else {
					                ClassDecl classDecl = (ClassDecl)((ClassOrInterfaceType)receiver.getType()).typeDecl;
							            node.ir_node = new Mem(new BinOp(BinOp.OpType.ADD, receiver.ir_node, new Const(classDecl.fieldMap.get(node.field))));
								            }
    }

    public void visit(LHS node){
        if (node.hasName()){
            if (node.refer instanceof FieldDecl){
                if (node.refer instanceof FieldDecl && ((FieldDecl)(node.refer)).isStatic()){
                    Temp resTemp = new Temp("staticFieldAccess_" + node.hashCode() );
                    FieldDecl _field = (FieldDecl)(node.refer);
                    compUnit.externStrs.add(_field.getFirstVarName() + "_" + _field.hashCode());
                    node.ir_node = new ESeq(new Move(resTemp, new Name((_field.getFirstVarName() + "_" + _field.hashCode()))), new Mem(resTemp));
                }   else {
                    node.ir_node = translateFieldAccess(node.first_receiver, node.subfields);
                }
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
        Seq codes = new Seq();
        Expr receiver = null;
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
                if(node.getArgumentList() != null) {
		  args.addAll(node.getArgumentList().ir_node);
		}
		compUnit.externStrs.add(callingMethod);
                funcAddr = new Name(callingMethod);
                node.ir_node = new Call(funcAddr, args);
                ((Call)node.ir_node).funcLabel = callingMethod;
                return;
            } else if (node.hasName()) {
                receiver = node.receiver;
               // System.out.println(node);
                if (node.receiver instanceof ThisLiteral){
                    Temp _this = currFunc.receiver;
                    args.add(_this);
                    vtable = new Mem(_this);
                }   else {
                    PostFixExpr _receiver = (PostFixExpr)node.receiver;
                    Expr_c _receiver_code = null;
                    if (_receiver.refer instanceof FieldDecl && ((FieldDecl)(_receiver.refer)).isStatic()){
                        Temp resTemp = new Temp("staticFieldAccess_" + node.hashCode() );
                        FieldDecl _field = (FieldDecl)(_receiver.refer);
                        compUnit.externStrs.add(_field.getFirstVarName() + "_" + _field.hashCode());
                        _receiver_code = new ESeq(new Seq(new Move(resTemp, new Mem(new Name((_field.getFirstVarName() + "_" + _field.hashCode())))), new Move(resTemp, new Mem(resTemp))), resTemp);
                    }   else {
                        Temp resTemp = new Temp("nonStaticFieldAccess_" + node.hashCode() );
                        _receiver_code = new ESeq(new Move(resTemp, translateFieldAccess(_receiver.first_receiver, _receiver.subfields)), resTemp);
                    }
                    Temp receiver_tmp = new Temp("receiver_"+node.hashCode());
                    codes.stmts().add(new Move(receiver_tmp, _receiver_code));
                    args.add(receiver_tmp);
                    nullcheck(codes.stmts(), receiver_tmp);
                    if (((PostFixExpr) node.receiver).getType() instanceof ArrayType ){
                        vtable = new Mem(new BinOp(BinOp.OpType.SUB,receiver_tmp, new Const(8)));
                    }   else {
                        vtable = new Mem(receiver_tmp);
                    }
                }
            } else {
                receiver = node.getPrimary();
                Temp arg = new Temp("argtmp_"+node.hashCode());
                codes.stmts().add(new Move(arg, node.getPrimary().ir_node));
                args.add(arg);
                nullcheck(codes.stmts(), arg);
                if (node.getPrimary().getType() instanceof ArrayType){
                    vtable = new Mem(new BinOp(BinOp.OpType.SUB,arg, new Const(8)));
                }   else  {
                    vtable = new Mem(arg);
                }
            }
            if(node.getArgumentList() != null) {
                args.addAll(node.getArgumentList().ir_node);
            }
            ClassDecl classDecl = null;
            if (receiver.type instanceof ClassOrInterfaceType){
                classDecl = (ClassDecl)((ClassOrInterfaceType)(receiver.type)).typeDecl;
            }   else {
                classDecl = ObjectDecl;
            }
            
            int offset = classDecl.methodMap.get(method_decl);
            
            funcAddr = new Mem(new BinOp(BinOp.OpType.ADD, vtable, new Const(offset)));
            if (codes.stmts().isEmpty()){
                node.ir_node = new Call(funcAddr, args);
                ((Call)node.ir_node).funcLabel = callingMethod;
            }   else {
                Call call = new Call(funcAddr, args);
                call.funcLabel =callingMethod;
                node.ir_node = new ESeq(codes,call );
            }

        } else if (node.whichMethod instanceof AbstractMethodDecl) {
            
            AbstractMethodDecl interface_method = (AbstractMethodDecl)node.whichMethod;
            Expr_c vtable = null;
            List <Statement> stmts = new ArrayList<Statement>();
            // search itable and find method decl
            // case: this.x
            if (node.hasName()) {
                if (node.receiver instanceof ThisLiteral){
                    Temp _this = currFunc.receiver;
                    args.add(_this);
                    vtable = new Mem(_this);
                }   else {
                    PostFixExpr _receiver = (PostFixExpr)node.receiver;
                    Expr_c _receiver_code = null;
                    if (_receiver.refer instanceof FieldDecl && ((FieldDecl)(_receiver.refer)).isStatic()){
                        Temp resTemp = new Temp("staticFieldAccess_" + node.hashCode() );
                        FieldDecl _field = (FieldDecl)(_receiver.refer);
                        compUnit.externStrs.add(_field.getFirstVarName() + "_" + _field.hashCode());
                        _receiver_code = new ESeq(new Seq(new Move(resTemp, new Mem(new Name((_field.getFirstVarName() + "_" + _field.hashCode())))), new Move(resTemp, new Mem(resTemp))), resTemp);
                    }   else {
                        _receiver_code = translateFieldAccess(_receiver.first_receiver, _receiver.subfields);
                    }
                    Temp receiver_tmp = new Temp("receiver_"+node.hashCode());
                    stmts.add(new Move(receiver_tmp, _receiver_code));
                    args.add(receiver_tmp);
                    nullcheck(stmts, receiver_tmp);
                    vtable = new Mem(receiver_tmp);
                }
            } else {
                Temp arg = new Temp("argtmp_"+node.hashCode());
                stmts.add(new Move(arg, node.getPrimary().ir_node));
                args.add(arg);
                nullcheck(stmts, arg);
                vtable = new Mem(arg);
            }
            if(node.getArgumentList() != null) {
                args.addAll(node.getArgumentList().ir_node);
            }
            Temp vtable_addr = new Temp("vtable_addr_"+node.hashCode());
            stmts.add(new Move(vtable_addr, vtable));
            Temp itable_addr = new Temp("itable_addr_"+node.hashCode());
            stmts.add(new Move(itable_addr, new Mem(vtable_addr)));
            //InterfaceDecl classDecl = (InterfaceDecl)env.ASTNodeToScopes.get(interface_method).typeDecl;
            Temp bitmask = new Temp("bitmask_"+node.hashCode());
            stmts.add(new Move(bitmask, new Mem(itable_addr)));
            Temp tset = new Temp("itable_offset_"+node.hashCode());
            stmts.add(new Move(tset, new BinOp(BinOp.OpType.AND, new Const(interface_method.getName().hashCode()), bitmask)));
            stmts.add(new Move(tset, new BinOp(BinOp.OpType.MUL, tset, new Const(4))));
            stmts.add(new Move(tset, new BinOp(BinOp.OpType.ADD, tset, new Const(4))));
            Temp index_vtable = new Temp("vtable_index_"+node.hashCode());
            stmts.add(new Move(index_vtable, new Mem(new BinOp(BinOp.OpType.ADD, itable_addr, tset))));
            node.ir_node = new ESeq(new Seq(stmts), new Call(new Mem(new BinOp(BinOp.OpType.ADD, vtable_addr, index_vtable) ), args));
            
            // System.out.println("======invocation=======");
            // System.out.println(interface_method.getName()+interface_method.hashCode());
            // System.out.println(interface_method.getName().hashCode());
            // System.out.println("=======================");
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
            if (node.getType() instanceof ClassOrInterfaceType && node.getVarDeclarators().getLastVarDeclarator().getExpr().type instanceof ArrayType){
                TypeDecl decl = ((ClassOrInterfaceType)node.getType()).typeDecl;
                if (decl != ObjectDecl) throw new BackendError("cannot assign arr type to non-Object type");
                Expr_c right_res = node.getVarDeclarators().getLastVarDeclarator().getExpr().ir_node;
                Expr_c left_res = new Temp(node.getVarDeclarators().getFirstName());
                Temp tmp = new Temp("assArr_"+node.hashCode());
                node.ir_node = new Seq(new Move(tmp, right_res), new Move(left_res, new BinOp(BinOp.OpType.SUB, tmp, new Const(8))));
            }   else {
                node.ir_node = new Move(new Temp(node.getVarDeclarators().getFirstName()), varDecl.ir_node);
            }
        }
    }

    public void visit(PostFixExpr node){
        //System.out.println(node.refer);

        if (node.refer instanceof FieldDecl){
            if (node.refer instanceof FieldDecl && ((FieldDecl)(node.refer)).isStatic()){
                Temp resTemp = new Temp("staticFieldAccess_" + node.hashCode() );
                FieldDecl _field = (FieldDecl)(node.refer);
                compUnit.externStrs.add(_field.getFirstVarName() + "_" + _field.hashCode());
                node.ir_node = new ESeq(new Move(resTemp, new Name((_field.getFirstVarName() + "_" + _field.hashCode()))), new Mem(resTemp));
            }   else {
                node.ir_node = translateFieldAccess(node.first_receiver, node.subfields);
            }
        }   else {
            node.ir_node = new Temp(node.getName().getValue());
        }
    }

    public void visit(NumericLiteral node){
        node.ir_node = new Const((int)Long.parseLong(node.value));
    }

    public void visit(AdditiveExpr node){
       // System.out.println("node has type " + node.type);
        Expr_c expr_c1 = node.getOperatorLeft().ir_node;
        Expr_c expr_c2 = node.getOperatorRight().ir_node;;
        if (node.isPlusOperator()){
            if (node.type instanceof ClassOrInterfaceType){ //String case
                node.string_concat.accept(this);
                node.ir_node = node.string_concat.ir_node;
            }    else {
                node.ir_node = new BinOp(BinOp.OpType.ADD, expr_c1, expr_c2);
            }
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
            Temp right = new Temp("right_"+node.hashCode());
            node.ir_node = new ESeq(new Seq(new Move(right, expr_c2), divisionByZeroCheck(right)), new BinOp(BinOp.OpType.DIV,expr_c1, right));
        }   else {
            node.ir_node = new BinOp(BinOp.OpType.MOD,expr_c1, expr_c2);
        }
    }

    public void visit(ReturnStmt node) {

        if (node.getExpr() != null) {
            Expr_c expr_c = node.getExpr().ir_node;
            node.ir_node = new Return(expr_c);
        } else {
            node.ir_node = new Return(new Const(0)); // dummy value
        }
    }

    public void visit(ConditionalAndExpr node) {
        Temp temp = new Temp("t_"+node.hashCode());
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
        Temp temp = new Temp("t_"+node.hashCode());
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
            if (type instanceof ReferenceType){
                node.ir_node = (Expr_c) instanceOfTestGeneral(node.getOperatorLeft(), (ReferenceType)node.getisInstanceOfRight(), node.getOperatorLeft().ir_node);
            }
        }
    }

    public void visit(PrimaryNoArray node){
        node.ir_node = node.getExpr().ir_node;
    }

    public List<Statement> getConditionalIRNode(Expr expr, String lt, String lf) {
        List<Statement> stmts = new ArrayList<Statement>();
        Expr_c expr_c = expr.ir_node;
        if (expr instanceof FieldAccess || expr instanceof MethodInvocation || expr instanceof ArrayAccess){
            stmts.add(new CJump(expr_c, lt, lf));
        }   else if (expr instanceof PrimaryNoArray && expr.value.equals("()")) {
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
        }   else if (expr instanceof EqualityExpr && ((EqualityExpr)expr).getOperator().equals("==")) {
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
        } else if ((expr instanceof ConditionalAndExpr) && expr.children.size() == 2){
            Label l = new Label("condAndlabel_" + expr.hashCode());
            stmts.addAll(getConditionalIRNode(expr.getOperatorLeft(), l.name(), lf));
            stmts.add(l);
            stmts.addAll(getConditionalIRNode(expr.getOperatorRight(), lt, lf));
        } else if ((expr instanceof ConditionalOrExpr) && expr.children.size() == 2) {
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

    public void nullcheck(List<Statement> stmts, Expr_c expr_c){
        Label ok_label = new Label("ok_label"+tools.getLabelOffset());
        stmts.add(new CJump(new BinOp(BinOp.OpType.NEQ, expr_c, new Const(0)), ok_label.name()));
        stmts.add(new Exp(new Call(new Name("__exception"))));
        stmts.add(ok_label);
    }

    public void boundcheck(List<Statement> stmts, Expr node, Expr_c ta, Expr_c ti){
        Label negative_index_exception = new Label("negative_index_exception"+tools.getLabelOffset());
        Label lower_bound_ok_label = new Label("lower_bound_ok_label"+tools.getLabelOffset());

        Label index_exception_label = new Label("index_exception_label"+tools.getLabelOffset());
        Label bound_ok_label = new Label("bound_ok_label"+tools.getLabelOffset());

        // //check 0<=ti
        stmts.add(new CJump(new BinOp(BinOp.OpType.GEQ,ti, new Const(0)), lower_bound_ok_label.name(), negative_index_exception.name()));
        stmts.add(negative_index_exception);
        stmts.add(new Exp(new Call(new Name("__exception"))));
        stmts.add(lower_bound_ok_label);

        // //check ti<size
        stmts.add(new CJump(new BinOp(BinOp.OpType.LT, ti, new Mem(new BinOp(BinOp.OpType.SUB, ta, new Const(4*3)))), bound_ok_label.name(), index_exception_label.name()));
        stmts.add(index_exception_label);
        stmts.add(new Exp(new Call(new Name("__exception"))));
        stmts.add(bound_ok_label);
    }

    public void visit(ArrayAccess node){
        List <Statement> stmts = new ArrayList<Statement>();
        Temp ta = new Temp("ta_"+node.hashCode());
        tir.src.joosc.ir.ast.Expr e1 = null;
        if (node.hasName()) {
            if (node.refer instanceof FieldDecl && ((FieldDecl)(node.refer)).isStatic()){
                Temp resTemp = new Temp("staticFieldAccess_" + node.hashCode() );
                FieldDecl _field = (FieldDecl)(node.refer);
                compUnit.externStrs.add(_field.getFirstVarName() + "_" + _field.hashCode());
                e1 = new ESeq(new Move(resTemp, new Name((_field.getFirstVarName() + "_" + _field.hashCode()))), new Mem(resTemp));
            }   else {
                e1 = translateFieldAccess(node.first_receiver, node.subfields);
            }
        } else {
            node.recursive_dectecter += "recursive_layer_";
            e1 = node.getExpr().ir_node;
        }
        stmts.add(new Move(ta, e1));
        // // null check
        nullcheck(stmts, ta);

        Temp ti = new Temp("ti_"+node.hashCode());
        stmts.add(new Move(ti, node.getDimExpr().ir_node));
        
        // // bounds check
        boundcheck(stmts, node, ta, ti);

        Temp res = new Temp("res_"+node.hashCode());
        stmts.add(new Move(res, new BinOp(BinOp.OpType.ADD, ta, new BinOp(BinOp.OpType.MUL, ti, new Const(4)))));
        node.ir_node = new ESeq(new Seq(stmts), new Mem(res));

    }

    public void visit(ArrayCreationExpr node) {
        ArrayType type = (ArrayType) node.type;
        ClassDecl decl = null;
        if (type.getType() instanceof ClassOrInterfaceType){
            decl = (ClassDecl) ((ClassOrInterfaceType)type.getType()).typeDecl;
        }
        List <Statement> stmts = new ArrayList<Statement>();
        Temp tn = new Temp("tn_"+node.hashCode());
        
        stmts.add(new Move(tn, node.getDimExpr().ir_node));
        
        
         //check 0<=ti
         Label negative_check = new Label("negative_check"+node.hashCode());
         Label lower_bound_ok_label = new Label("lower_bound_ok_label"+node.hashCode());
         stmts.add(new CJump(new BinOp(BinOp.OpType.GEQ, tn,new Const(0)), lower_bound_ok_label.name(), negative_check.name()));
         stmts.add(negative_check);
         stmts.add(new Exp(new Call(new Name("__exception"))));
         stmts.add(lower_bound_ok_label);

        Temp tm = new Temp("tm_"+node.hashCode());
        stmts.add(new Move(tm, new Call(new Name("__malloc"), new BinOp(BinOp.OpType.ADD, new BinOp(BinOp.OpType.MUL, tn, new Const(4)), new Const(12)))));
        stmts.add(new Move(new Mem(tm), tn));

        //TODO dispatch vector
        stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, new Const(4), tm)), new Name(tools.getVtable(ObjectDecl, env))));
        compUnit.externStrs.add(tools.getVtable(ObjectDecl, env));

        //Used for Casting check
        if (decl != null) {
            stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, new Const(8), tm)), new Name(tools.getVtable(decl, env))));
            compUnit.externStrs.add(tools.getVtable(decl, env));
        }   else {
            stmts.add(new Move(new Mem(new BinOp(BinOp.OpType.ADD, new Const(8), tm)), new Const(0)));
        }

        // loop to clear memory locations.
        Temp cleaner = new Temp("cleaner");
        stmts.add(new Move(cleaner, new BinOp(BinOp.OpType.ADD, tm, new Const(4*3))));

        Label clean = new Label("clean"+node.hashCode());
        Label cleanup_done = new Label("cleanup_done"+node.hashCode());
        stmts.add(clean);
        stmts.add(new Move(new Mem(cleaner), new Const(0)));
        stmts.add(new Move(cleaner, new BinOp(BinOp.OpType.ADD, cleaner, new Const(4))));
        stmts.add(new CJump(new BinOp(BinOp.OpType.LT, cleaner, new BinOp(BinOp.OpType.ADD, tm, new BinOp(BinOp.OpType.ADD, new Const(4*3), new BinOp(BinOp.OpType.MUL, tn, new Const(4))))), clean.name(), cleanup_done.name()));
        stmts.add(cleanup_done);


        Temp res = new Temp("res_"+ node.hashCode());
        stmts.add(new Move(res, new BinOp(BinOp.OpType.ADD, tm, new Const(4*3))));
        //stmts.add(new Exp(new Call(new Name("__exception"))));
        node.ir_node = new ESeq(new Seq(stmts), res);
    }

    public void visit(ClassInstanceCreateExpr node) {
        ConstructorDecl callingConstructor = (ConstructorDecl) node.callable;
        ClassDecl initClass = callingConstructor.whichClass;

        // calc heap size
        compUnit.externStrs.add(tools.getVtable(initClass, env));
        compUnit.externStrs.add(tools.getItable(initClass, env));
        int size = 4; // vtb addr
        List <FieldDecl> fieldDecls = initClass.getAllNonStaticFieldDecls();
        size += fieldDecls.size() * 4; // add 4 * field num
        List<Statement> stmts = new ArrayList<Statement>();
        String consName = callingConstructor.getName() + "_" + callingConstructor.hashCode();
        Temp heapStart = new Temp("heapStart_"+node.hashCode());
        stmts.add(new Move(heapStart, new Call(new Name("__malloc"), new Const(size))));
	    Temp thisTemp = new Temp("THIS_"+callingConstructor.getName());
        FuncDecl reserved = currFunc;
	    if (callingConstructor.funcDecl == null){
            String name = callingConstructor.getName() + "_" + hashCode();
            callingConstructor.funcDecl = new FuncDecl(name, 0, null, "THIS_"+callingConstructor.getName());
            currFunc = callingConstructor.funcDecl;
        }   else {
	        currFunc = callingConstructor.funcDecl;
        }
	    stmts.add(new Move(thisTemp, heapStart));

        for (FieldDecl fieldDecl : initClass.getAllNonStaticFieldDecls()) {
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
        compUnit.externStrs.add(tools.getVtable(initClass, env));
        stmts.add(new Move(new Mem(heapStart), new Name(tools.getVtable(initClass, env))));


        Temp vtable_addr = new Temp("vtable_addr_create_"+node.hashCode());
        stmts.add(new Move(vtable_addr, new Mem(heapStart)));
        compUnit.externStrs.add(tools.getItable(initClass, env));
        stmts.add(new Move(new Mem(vtable_addr), new Name(tools.getItable(initClass, env))));
        // calling constructor like method invocation

        compUnit.externStrs.add(consName);
        tir.src.joosc.ir.ast.Expr consAddr = new Name(consName);
        List <tir.src.joosc.ir.ast.Expr> exprList = new ArrayList<tir.src.joosc.ir.ast.Expr>();
        exprList.add(heapStart);
        if (node.getArgumentList() != null) {
            exprList.addAll(node.getArgumentList().ir_node);
            //System.out.println("in class " + consName + " args are " + node.getArgumentList().ir_node);
            stmts.add(new Exp(new Call(consAddr, exprList)));
        } else {

            stmts.add(new Exp(new Call(consAddr, exprList)));
        }
        node.ir_node = new ESeq(new Seq(stmts), heapStart);
        currFunc = reserved;
    }

    public void visit(ConstructorDeclarator node) {
        List <Statement> stmts = new ArrayList<Statement>();
        int index = 0;
        stmts.add(new Move(currFunc.receiver, new Temp(Configuration.ABSTRACT_ARG_PREFIX + index)));
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
            exprList.add(currFunc.receiver);
            stmts.add(new Exp(new Call(new Name(superConsName), exprList)));
        }

        // move params
        if (node.getConstructorDeclarator().ir_node != null) {
            stmts.addAll(node.getConstructorDeclarator().ir_node);
        }
        Seq seq_node = node.getConstructorBody().ir_node;

        stmts.add(seq_node);
        Seq body = new Seq(stmts);
        node.funcDecl.numParams = node.getConstructorDeclarator().numParams()+1;
        node.funcDecl.body = body;
        compUnit.appendFunc(node.funcDecl);
    }

    public void visit(ThisLiteral node){
        node.ir_node = currFunc.receiver;
    }

    public void visit(ClassDecl node) {
        currFunc = new FuncDecl(node.getName(), 0, null, "THIS_"+node.getName());
        List<FieldDecl> fields = node.getStaticFieldDecls();
        for (FieldDecl f : fields){
            compUnit.definedLabels.add(f.getFirstVarName() + "_" + f.hashCode());
        }
        compUnit.staticFields.addAll(fields);
    }



}
