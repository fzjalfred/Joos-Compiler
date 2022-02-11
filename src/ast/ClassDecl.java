package ast;

import java.util.List;

public class ClassDecl extends TypeDecl{
    // children( modifiers ID Super Interface ClassBody)
    public ClassDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(1).value;
    }
    public ClassBodyDecls getClassBodyDecls(){
        if (children.get(4) == null) return null;
        assert children.get(4).children.get(0) instanceof ClassBodyDecls;
        return (ClassBodyDecls)children.get(4).children.get(0);
    }

}
