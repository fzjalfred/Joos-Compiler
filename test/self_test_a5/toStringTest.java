public class toStringTest {
    public toStringTest() {}
    // public int foo() {
    //     char a = 90;
    //     String b = "abc";
    //     String c = a + b;
    //     return 22;
    // }
    public static int test() {
        int a = 90;
        Integer b = new Integer(90);
        String op2 = "88";
        String c1 = new Integer(a).toString() + op2;
        String c2 = a + op2;
        System.out.println(c1);
        System.out.println(c2);
        return 22;
    }
}
