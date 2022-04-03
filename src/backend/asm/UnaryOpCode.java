package backend.asm;

public abstract class UnaryOpCode extends Code{
    Operand op;

    public UnaryOpCode(Operand op){
        this.op = op;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + op;
    }
}
