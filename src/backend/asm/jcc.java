package backend.asm;

public class jcc extends UnaryOpCode{
    public enum ccType {
        z("z"), e("e"), l("l"), nge("nge"), ge("ge"), nl("nl"), nz("nz");
        private String s;
        private ccType(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }
    public ccType cc;

    public jcc(ccType cc, Operand operand){
        super(operand);
        this.cc = cc;
    }
    @Override
    public String toString() {
        return "j"+cc.toString()+" "+op;
    }
}
