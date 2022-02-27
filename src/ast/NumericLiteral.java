package ast;

import visitors.Visitor;

import java.util.List;

public class NumericLiteral extends Literal{
    public NumericLiteral(List<ASTNode> children, String value){
        super(children, value);
    }
    private long  upperBound = 2147483648L;
    public void invalidRange() throws Exception{
        String numStr = value;
        long num = Long.parseLong(numStr);
        if (num > upperBound){
            throw new Exception("Integer" + numStr + " overflow");
        }
    }
    public void setBound(long num) throws Exception{
        upperBound = num;
        invalidRange();
    }


    @Override
    public void accept(Visitor v){
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
        v.visit(this);
    }
}
