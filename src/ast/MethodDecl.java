package ast;

import java.util.List;

public class MethodDecl extends ClassMemberDecl {
    public MethodDecl(List<ASTNode> children, String value){

        super(children, value);
    }

    public MethodHeader getMethodHeader(){
        assert children.get(0) instanceof MethodHeader;
        return (MethodHeader)children.get(0);
    }

    public MethodBody getMethodBody(){
        assert children.get(1) instanceof MethodBody;
        return (MethodBody)children.get(1);
    }

    public String getName(){
        MethodHeader mh = getMethodHeader();
        return mh.getName();
    }
}
