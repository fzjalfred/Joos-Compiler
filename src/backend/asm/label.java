package backend.asm;

public class label extends Code{
    public String name;
    public label(String n) {
        name = n;
        isFunctionDecl = false;
    }
    public label(String n, boolean isFunctionDecl) {
        name = n;
        this.isFunctionDecl = isFunctionDecl;
    }
    public boolean isFunctionDecl;

    @Override
    public String toString() {
        return name+":";
    }
}
