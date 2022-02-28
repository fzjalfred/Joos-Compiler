package visitors;
import ast.*;
import exception.SemanticError;
import type.*;
import utils.tools;

public class TypeCheckVisitor extends Visitor{
    public Context context;
    public RootEnvironment env;
    public Type returnType;

    public TypeCheckVisitor(RootEnvironment env){
        context = new Context();
        this.env = env;
        this.returnType = null;
    }

    @Override
    public void visit(NumericLiteral node) {
        node.type = new PrimitiveType(tools.empty(), "int");
    }

    @Override
    public void visit(StringLiteral node) {
        node.type = new ClassOrInterfaceType(tools.list(tools.nameConstructor("java.lang.String")), "");
    }


    /** Stmts */
    @Override
    public void visit(FieldDecl node) {
        String var = node.getVarDeclarators().getFirstName();
        context.put(var, node.getType());
    }

    @Override
    public void visit(LocalVarDecl node) {
        String var = node.getVarDeclarators().getFirstName();
        context.put(var, node.getType());
    }

    @Override
    public void visit(Parameter node) {
        String var = node.getVarDeclaratorID().getName();
        context.put(var, node.getType());
    }

    @Override
    public void visit(MethodDecl node) {
        this.returnType = node.getMethodHeader().getType();
    }

    @Override
    public void visit(PostFixExpr node) {
        String name = node.getName().getValue();
        Type type = context.get(name);
        if (type != null) {
            node.type = type;
        }   else {
            //TODO: case analysis: read fields
            //throw new SemanticError("Cannot find local variable or formal parameter: " + name);
        }

    }

    @Override
    public void visit(ForInit node) {
        if (node.isVarDecl()){
            context.put(node.getVarDeclarator().getName(), node.getType());
        }
        //TODO: stmt case
    }


}
