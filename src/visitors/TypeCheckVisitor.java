package visitors;
import ast.*;
import type.*;
import utils.tools;

public class TypeCheckVisitor extends Visitor{
    Context context;
    RootEnvironment env;


    public TypeCheckVisitor(RootEnvironment env){
        context = new Context();
        this.env = env;
    }

    @Override
    public void visit(NumericLiteral node) {
        node.type = new PrimitiveType(tools.empty(), "int");
        //System.out.println("populated type " + node.type + " in literal " + node);
    }
}
