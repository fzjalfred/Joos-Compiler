package backend.asm;

public class LabelOperand extends Operand{
    public String name;
    public LabelOperand(String n) {
        name = n;
    }

    @Override
    public String toString() {
        return name+":";
    }
}

