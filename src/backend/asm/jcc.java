package backend.asm;

public class jcc extends UnaryOpCode{
    public enum ccType {
        z, e, l, nge, ge, nl
    }
    public ccType cc;

    public jcc(ccType cc, Operand operand){
        super(operand);
        this.cc = cc;
    }
}
