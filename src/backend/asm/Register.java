package backend.asm;

public class Register extends Operand{
    public String name;

    public Register(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
