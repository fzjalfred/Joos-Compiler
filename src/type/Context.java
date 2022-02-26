package type;

import java.util.*;
import ast.*;
public class Context {
    /**context frame for each scope */
    class ContextFrame{
        private Map<Name, Type> T;
        public Type get(Name name){
            if (T.containsKey(name)) return T.get(name);
            return null;
        }

        public void put(Name name, Type type){
            T.put(name, type);
        }
        public ContextFrame(){
            T = new HashMap<Name,Type>();
        }
    }

    private Stack<ContextFrame> frames;

    /** put empty context frame on top of frames */
    public void entry(){
        frames.push(new ContextFrame());
    }

    /** pop top of the frame */
    public void pop(){
        frames.pop();
    }

    /** put <name, type> on top of frames */
    public void put(Name name, Type type){
        frames.peek().put(name, type);
    }

    /** get the type of the name of closet context frame  */
    public Type get(Name name){
        Iterator<ContextFrame> it = frames.iterator();
        Type res = null;
        while (it.hasNext()){
            res = it.next().get(name);
            if (res != null) return res;
        }
        return null;
    }
}
