package ast;

import java.util.List;

public class NumericType extends PrimitiveType {
    public NumericType(List<ASTNode> children, String value){
        super(children, value);
    }

    @Override
    public String getNameString() {
        return value;
    }
}
