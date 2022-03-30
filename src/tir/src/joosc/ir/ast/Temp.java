package tir.src.joosc.ir.ast;

/**
 * An intermediate representation for a temporary register
 * TEMP(name)
 */
public class Temp extends Expr_c {
    private String name;

    /**
     * @param name name of this temporary register
     */
    public Temp(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String label() {
        return "TEMP(" + name + ")";
    }

    @Override
    public String toString() {
        return "Temp{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public void canonicalize() {
        canonicalized_node = new Seq(new Exp(this));
    }
}
