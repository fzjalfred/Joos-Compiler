package visitors;
import ast.*;
import type.*;
import utils.tools;

public class TypeCheckVisitor extends Visitor{
    public Context context;
    public RootEnvironment env;


    public TypeCheckVisitor(RootEnvironment env){
        context = new Context();
        this.env = env;
    }

    @Override
    public void visit(NumericLiteral node) {
        node.type = new PrimitiveType(tools.empty(), "int");
    }

    @Override
    public void visit(FieldDecl node) {
        String var = node.getFirstVarName();
        context.put(var, node.getType());
    }
}
