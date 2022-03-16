package dataflowAnalysis;

import ast.AtomicStmt;
import ast.ReturnStmt;
import exception.SemanticError;

import java.util.*;

/** CFG denotes control flow graph */
public class CFG {

    /** vertex structure for CFG */
    public class Vertex{
        public AtomicStmt stmt;
        public List<Vertex> successors;
        public List<Vertex> precessors;
        public boolean in;
        public boolean out;



        public Vertex(AtomicStmt stmt){
            this.stmt = stmt;
            successors = new ArrayList<Vertex>();
            precessors = new ArrayList<Vertex>();
            in = false;
            out = false;
        }

        private String listVerticesToString(List<Vertex> list){
            String res = "(";
            for (Vertex v : list){
                res += v.stmt + " ";
            }
            res += ")";
            return res;
        }

        @Override
        public String toString() {
            return "Vertex{" +
                    "stmt=" + stmt +
                    ", in=" + in +
                    ", out=" + out +
                    ", successors= " + (listVerticesToString(successors)) +
                    ", precessors= " + (listVerticesToString(precessors)) +
                    "}\n";
        }
    }

    /** internal vertices (except START/END)*/
    Set<Vertex> vertices;
    public Vertex START;
    public Vertex END;
    public String filename;
    public Queue<Vertex> worklist;

    public CFG(String filename){
        vertices = new HashSet<Vertex>();
        START = new Vertex(null);
        END = new Vertex(null);
        worklist = new LinkedList<>();
        vertices.add(START);
        vertices.add(END);
        this.filename = filename;
    }

    public void setEdge(Vertex v1, Vertex v2){
        v1.successors.add(v2);
        v2.precessors.add(v1);
    }

    /** add a stmt to CFG, connect such vertex to previous vertex; return that vertex */
    public Vertex addVertex(AtomicStmt stmt, Vertex prev, List<CFG.Vertex> ifpaths){
        if (prev != null && !vertices.contains(prev)) throw new SemanticError("no such vertex " + prev + " in CFG in " + filename);
        Vertex v = new Vertex(stmt);
        if (prev != null) setEdge(prev, v);
        for (CFG.Vertex i: ifpaths) {
            if (i != null)
                setEdge(i, v);
        }
        vertices.add(v);
        ifpaths.clear();
        return v;
    }

    public void initWorkList() {
        for (Vertex v : vertices) {
            worklist.add(v);
        }
    }

    public void updateVertex(Vertex vertex) {
        boolean flag = false;
        for (Vertex precessor : vertex.precessors) {
            flag = flag || precessor.out;
        }
        vertex.in = flag;
        if (vertex.stmt instanceof ReturnStmt) {
            vertex.out = false;
        } else if (vertex == START) {
            vertex.out = true;
        } else {
            vertex.out = flag;
        }
    }

    public void checkUnreachable() throws SemanticError {
        for (Vertex v : vertices) {
            if (v != START && v != END) {
                if (!v.in) {
                    throw new SemanticError(v + " is unreachable");
                }
            }
        }
    }

    public void runWorkList(boolean isVoid ) throws SemanticError{
//        System.out.println("new ");
//        System.out.println("start: " + START);
//        System.out.println("end: " + END);
        while (!worklist.isEmpty()) {
            Vertex v = worklist.remove();
//            System.out.println(v);
            Boolean prevValue = v.out;
            updateVertex(v);
            if (v.out != prevValue) {
                for (Vertex sucessor : v.successors) {
                    if (!worklist.contains(sucessor)) {
                        worklist.add(sucessor);
                    }
                }
            }
//            System.out.println(v);
//            System.out.println("");
        }
        checkUnreachable();
        if (!isVoid && END.in != false) {
            throw new SemanticError("in[END] is true");
        }
    }

    @Override
    public String toString() {
        return "CFG{" +
                "filename: " + filename +
                ", vertices=" + vertices +
                ", START=" + START +
                ", END=" + END +
                '}';
    }
}
