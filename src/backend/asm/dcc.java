package backend.asm;

public class dcc extends Code{
    public enum ccType {
        w("w"), d("w"), b("b");
        private String s;
        private ccType(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }

    public LabelOperand labelOperand;
    public ccType type;
    public dcc(ccType type, LabelOperand labelOperand){
        this.type = type;
        this.labelOperand = labelOperand;
    }

    @Override
    public String toString() {
        return "d" + type + " " + labelOperand;
    }
}
