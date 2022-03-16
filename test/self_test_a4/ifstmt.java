public class ifstmt {
    public ifstmt() {}
    public void foo(int x) {
        if (x == 2) {
            x = 3;
        }

        if (x == 3) {
            x = 4;
        } else {
            x = 5;
        }
    }
}
