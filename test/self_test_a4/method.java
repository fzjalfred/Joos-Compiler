public class method {
    public method() {
        f = 10;
    }
    public int f ;
    public static int test() {
        return method.bar();
    }
    public static int bar() {
        return 10;
    }
}
