package tir.src.joosc.ir.ast;

import tir.src.joosc.ir.visit.AggregateVisitor;
import tir.src.joosc.ir.visit.CheckCanonicalIRVisitor;
import tir.src.joosc.ir.visit.IRVisitor;

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
}