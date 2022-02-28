package type;

import java.util.*;
import ast.*;
public class Context {
    /**context frame for each scope */
    class ContextFrame{
        private Map<String, Type> T;
        private String name;
        public Type get(String name){
            if (T.containsKey(name)) return T.get(name);
            return null;
        }

        public void put(String name, Type type){
            T.put(name, type);
        }
        public ContextFrame(String name){
            this.name = name;
            T = new HashMap<String,Type>();
        }

        @Override
        public String toString() {
            return "ContextFrame{" +
                    "name: " + name +
                    "T=" + T +
                    '}';
        }
    }

    private Stack<ContextFrame> frames;

    /** put empty context frame on top of frames */
    public void entry(String name){
        //System.out.println("pushing " + frames.size()+1);
        frames.push(new ContextFrame(name));
    }

    /** pop top of the frame */
    public void pop(){
        //System.out.println("popping " + frames.peek());
        frames.pop();
    }

    /** put <name, type> on top of frames */
    public void put(String name, Type type){
        frames.peek().put(name, type);
    }

    /** get the type of the name of closet context frame  */
    public Type get(String name){
        Iterator<ContextFrame> it = frames.iterator();
        Type res = null;
        while (it.hasNext()){
            res = it.next().get(name);
            if (res != null) return res;
        }
        return null;
    }

    public boolean isEmpty(){
        return frames.isEmpty();
    }

    public Context(){
        frames = new Stack<ContextFrame>();
    }
}
