package tir.src.joosc.ir.ast;

import backend.asm.Tile;
import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.IRVisitor;
import tir.src.joosc.ir.visit.TilingVisitor;
import utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An intermediate representation for a sequence of statements
 * SEQ(s1,...,sn)
 */
public class Seq extends Statement {
    private List<Statement> statements;
    private boolean replaceParent;

    /**
     * @param statements the statements
     */
    public Seq(Statement... statements) {
        this(Arrays.asList(statements));
    }

    @Override
    public String toString() {
        return "Seq{" +
                "statements=" + statements +
                ", replaceParent=" + replaceParent +
                '}';
    }

    private <T> List<T> filterNulls(List<T> list) {
        return list
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    /**
     * Create a SEQ from a list of statements.
     * The list should not be modified subsequently.
     * @param statements the sequence of statements
     */
    public Seq(List<Statement> statements) {
        // filter out nulls
        this.statements = filterNulls(statements);
        this.replaceParent = false;
    }

    public Seq(List<Statement> statements, boolean replaceParent) {
        this.statements = filterNulls(statements);
        this.replaceParent = replaceParent;
    }

    public Label removeFirst(){
        assert statements.get(0) instanceof Label;
        Label l = (Label) statements.get(0);
        statements.remove(0);
        return l;
    }

    public List<Statement> stmts() {
        return statements;
    }

    @Override
    public String label() {
        return "SEQ";
    }

    @Override
    public Node visitChildren(IRVisitor v) {
        boolean modified = false;

        List<Statement> results = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            Statement newStatement = (Statement) v.visit(this, statement);
            if (newStatement != statement) modified = true;
            results.add(newStatement);
        }

        if (modified) return v.nodeFactory().IRSeq(results);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        for (Statement statement : statements)
            result = v.bind(result, v.visit(statement));
        return result;
    }

    @Override
    public CheckCanonicalIRVisitor checkCanonicalEnter(
            CheckCanonicalIRVisitor v) {
        return v.enterSeq();
    }

    @Override
    public boolean isCanonical(CheckCanonicalIRVisitor v) {
        return !v.inSeq();
    }

    public Expr getLastExpr(){
        Statement last = statements.get(statements.size()-1);
        if (last instanceof Exp){
            return ((Exp)last).expr();
        }
        return null;
    }

    void addSeq(Seq that){
        this.statements.addAll(that.statements);
    }
    void addStatement(Statement stmt) {this.statements.add(stmt);}

    public void setLastStatement(Statement stmt){
        statements.set(statements.size()-1, stmt);
    }

    @Override
    public void canonicalize() {
        List<Statement> stmts = new ArrayList<Statement>();
        for (Statement statement: statements){
            stmts.addAll(statement.canonicalized_node.statements);
        }
        canonicalized_node = new Seq(stmts);
    }

    @Override
    public Pair<List<Node>, Tile> tiling(TilingVisitor v) {
        List<Node> res = new ArrayList<Node>();
        int index = 0;
        List<Statement> newStmts = new ArrayList<Statement>();
        boolean skipNext = false;
        for (Statement statement : statements) {
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (statement instanceof Exp) {
                if (((Exp)statement).expr() instanceof Call) {
                    Call call = (Call)((Exp)statement).expr();
                    if (index + 1 < statements.size() && statements.get(index+1) instanceof Move) {
                        Move move = (Move) statements.get(index+1);
                        if (move.source() instanceof Temp && ((Temp)move.source()).name().equals("_RET")) {
                            skipNext = true;
                            if (call.returnTarget == null) {
                                call.returnTarget =(Temp)move.target();
                            }
                        }
                    }
                }
            }
            newStmts.add(statement);
            index++;
        }
        res.addAll(newStmts);
        return new Pair<List<Node>, Tile>(res, v.unit());
    }
}
