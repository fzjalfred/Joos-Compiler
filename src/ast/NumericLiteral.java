package ast;

import java.util.List;

public class NumericLiteral extends Literal{
    public NumericLiteral(List<ASTNode> children, String value){
        super(children, value);
    }
    private long  upperBound = 2147483648L;
    public void invalidRange() throws Exception{
        String numStr = children.get(0).value;
        long num = Long.parseLong(numStr);
        if (num > upperBound){
            throw new Exception("Integer" + numStr + " overflow");
        }
    }
    public void setBound(long num) throws Exception{
        upperBound = num;
        invalidRange();
    }

}
