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

    public static Register eax = new Register("eax");
    public static Register ebx = new Register("ebx");
    public static Register ecx = new Register("ecx");
    public static Register edx = new Register("edx");
    public static Register esi = new Register("esi");
    public static Register edi = new Register("edi");
    public static Register ebp = new Register("ebp");
    public static Register esp = new Register("esp");
}
