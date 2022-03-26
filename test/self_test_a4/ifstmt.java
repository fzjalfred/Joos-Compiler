public class ifstmt {
    public ifstmt() {}

    public int nest(int x) {
        if (x == 1) {
            return 1;
        }
        if (x == 3 || x == 4 || x == 5) {
            return 43;
        }
        // if (!(x == 1)) {
        //     return 34;
        // }
        if (x != 1) {
            return 999;
        }

        return x;
    }
}




// C[e1&&e2&&e3, lt, lf]:

// CJump(E[e1], l1, lf)
// l1: CJump(E[e2], l2, lf)
// l2: CJump(E[e3], lt, lf)




// C[(e1&&e2)&&e3, lt, lf]:

// ...
// Cjump(E[e1&&e2], l, lf)
// l: Cjump(E[e3], lt, lf)