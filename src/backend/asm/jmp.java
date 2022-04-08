package backend.asm;

public class jmp extends UnaryOpCode{

    public jmp(LabelOperand labelOp){
        super(labelOp);
    }

    public jmp(Register labelOp){
        super(labelOp);
    }

    @Override
    public String toString() {
        return "jmp " + op;
    }
}
