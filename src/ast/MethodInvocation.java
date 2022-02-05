package ast;

import java.util.List;

public class MethodInvocation extends PrimaryNoArray {
    public MethodInvocation(List<ASTNode> children, String value){
        super(children, value);
    }
}