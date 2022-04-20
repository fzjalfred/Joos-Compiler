package backend.asm;

public class dcc extends Code{
    public enum ccType {
        w("w"), d("d"), b("b");
        private String s;
        private ccType(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }

    public Operand labelOperand;
    public ccType type;
    public dcc(ccType type, Operand labelOperand){
        this.type = type;
        this.labelOperand = labelOperand;
    }

    @Override
    public String toString() {
        return "d" + type + " " + labelOperand;
    }
}
