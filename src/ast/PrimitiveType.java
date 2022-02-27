package ast;

import java.util.List;

public class PrimitiveType extends Type {
    public PrimitiveType(List<ASTNode> children, String value){
        super(children, value);
    }


    public boolean equals(PrimitiveType type) {
        return value.equals(type.value);
    }

    @Override
    public String getNameString() {
        return value;
    }
}
