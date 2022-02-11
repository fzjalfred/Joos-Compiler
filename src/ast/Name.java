package ast;

import java.util.ArrayList;
import java.util.List;

public class Name extends ASTNode{
    public Name(List<ASTNode> children, String value){
        super(children, value);
    }
    public boolean isSimpleName(){
        assert (!children.isEmpty());
        return children.size() == 1;
    }
    public List<String> getFullName(){
        List<String> res = new ArrayList<String>();
        for (ASTNode n : children){
            res.add(n.value);
        }
        return res;
    }
    public String getValue(){
        List<String> names = getFullName();
        return String.join(".",names);
    }

}
