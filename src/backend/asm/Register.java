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

    static Register eax = new Register("eax");
    static Register ebx = new Register("ebx");
    static Register ecx = new Register("ecx");
    static Register edx = new Register("edx");
    static Register esi = new Register("esi");
    static Register edi = new Register("edi");
    static Register ebp = new Register("ebp");
    static Register esp = new Register("esp");
}
