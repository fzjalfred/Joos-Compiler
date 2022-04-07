package backend.asm;

public class label extends Code{
    public String name;
    public label(String n) {
        name = n;
    }

    @Override
    public String toString() {
        return name+":";
    }
}
