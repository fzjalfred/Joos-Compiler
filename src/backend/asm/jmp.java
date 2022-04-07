package backend.asm;

public class jmp extends UnaryOpCode{

    public jmp(LabelOperand labelOp){
        super(labelOp);
    }

    @Override
    public String toString() {
        return "jmp " + op;
    }
}
