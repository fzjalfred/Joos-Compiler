package type;

import java.util.*;
import ast.*;
import utils.Pair;
public class Context {
    /**context frame for each scope */
    class ContextFrame{
        private Map<String, Referenceable> T;
        private String name;
        public Referenceable get(String name){
            if (T.containsKey(name)) return T.get(name);
            return null;
        }

        public void put(String name, Referenceable type){
            T.put(name, type);
        }
        public ContextFrame(String name){
            this.name = name;
            T = new HashMap<String,Referenceable>();
        }

        @Override
        public String toString() {
            return "ContextFrame{" +
                    "name: " + name +
                    "T=" + T +
                    '}';
        }
    }
    /** first denotes field frame; second denotes method frame */
    private Stack<ContextFrame> frames;

    /** put empty context frame on top of frames */
    public void entry(String name){
        frames.push(new ContextFrame(name));
    }

    /** pop top of the frame */
    public void pop(){
        //System.out.println("popping " + frames.peek());
        frames.pop();
    }

    /** put <name, type> on top of frames */
    public ContextFrame peek(){
        return frames.peek();
    }

    public void put(String name, Referenceable type){

        frames.peek().put(name, type);
    }


    /** get the type of the name of closet context frame  */
    public Referenceable get(String name){
        for (int i = frames.size()-1; i >= 0; i--){
            Referenceable res = frames.get(i).get(name);
            if (res != null) return res;
        }
        return null;
    }

    public Type getType(String name){
        Referenceable refer = get(name);
        if (refer != null) return refer.getType();
        return null;
    }

    public boolean isEmpty(){
        return frames.isEmpty();
    }

    public Context(){
        frames = new Stack<ContextFrame>();
    }

    @Override
    public String toString() {
        return "Context{" +
                "frames=" + frames +
                '}';
    }
}
