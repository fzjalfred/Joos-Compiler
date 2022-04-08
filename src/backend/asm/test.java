package backend.asm;

public class test extends BinaryOpCode{
    public test(Operand op1, Operand op2){
        super(op1, op2);
    }
    @Override
    public String toString() {
        return "test "+op1+ " " +op2;
    }
}
