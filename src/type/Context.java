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
    private Stack<Pair<ContextFrame, ContextFrame>> frames;

    /** put empty context frame on top of frames */
    public void entry(String name){
        Pair<ContextFrame, ContextFrame> frame = new Pair<ContextFrame,ContextFrame>(new ContextFrame(name), new ContextFrame(name));
        frames.push(frame);
    }

    /** pop top of the frame */
    public void pop(){
        //System.out.println("popping " + frames.peek());
        frames.pop();
    }

    /** put <name, type> on top of frames */
    public Pair<ContextFrame, ContextFrame> peek(){
        return frames.peek();
    }

    public void put(String name, Referenceable type){

        frames.peek().first.put(name, type);
    }

    public void put(String name, MethodDecl methodDecl){
        Referenceable methods = getMethods(name);
        if (methods == null){
            MethodList methodList = new MethodList(name);
            methodList.add(methodDecl);
            frames.peek().second.put(name, methodList);
        }   else if (methods instanceof MethodList) {
            MethodList methodList = (MethodList)methods;
            methodList.add(methodDecl);
        }
    }

    public void put(String name, AbstractMethodDecl methodDecl){
        Referenceable methods = get(name);
        if (methods == null){
            AbstractMethodList methodList = new AbstractMethodList(name);
            methodList.add(methodDecl);
            frames.peek().second.put(name, methodList);
        }   else if (methods instanceof AbstractMethodList) {
            AbstractMethodList methodList = (AbstractMethodList)methods;
            methodList.add(methodDecl);
        }
    }

    /** get the type of the name of closet context frame  */
    public Referenceable get(String name){
        for (int i = frames.size()-1; i >= 0; i--){
            Referenceable res = frames.get(i).first.get(name);
            if (res != null) return res;
        }
        return null;
    }

    public Referenceable getMethods(String name){
        Iterator<Pair<ContextFrame, ContextFrame>> it = frames.iterator();
        Referenceable res = null;
        while (it.hasNext()){
            res = it.next().second.get(name);
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
        frames = new Stack<Pair<ContextFrame, ContextFrame>>();
    }

    @Override
    public String toString() {
        return "Context{" +
                "frames=" + frames +
                '}';
    }
}
