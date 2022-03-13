package dataflowAnalysis;

import ast.AtomicStmt;
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

    public CFG(String filename){
        vertices = new HashSet<Vertex>();
        START = new Vertex(null);
        END = new Vertex(null);
        vertices.add(START);
        vertices.add(END);
        this.filename = filename;
    }

    public void setEdge(Vertex v1, Vertex v2){
        v1.successors.add(v2);
        v2.precessors.add(v1);
    }

    /** add a stmt to CFG, connect such vertex to previous vertex; return that vertex */
    public Vertex addVertex(AtomicStmt stmt, Vertex prev){
        if (!vertices.contains(prev)) throw new SemanticError("no such vertex " + prev + " in CFG in " + filename + " (probably while(true){})");
        Vertex v = new Vertex(stmt);
        setEdge(prev, v);
        vertices.add(v);
        return v;
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
