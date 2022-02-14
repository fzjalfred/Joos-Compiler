package ast;

import java.util.List;

public class ClassOrInterfaceType extends ReferenceType {
    public ClassOrInterfaceType(List<ASTNode> children, String value){
        super(children, value);
    }
    public Name getName(){
        assert children.get(0) instanceof Name;
        return (Name)children.get(0);
    }

    public boolean equals(ClassOrInterfaceType classOrInterfaceType) {
        Name myName = getName();
        Name hisName = classOrInterfaceType.getName();
        return myName.equals(hisName);
    }
}
