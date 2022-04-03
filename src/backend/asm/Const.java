package backend.asm;

public class Const extends Operand{

    public Const(int value){
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
