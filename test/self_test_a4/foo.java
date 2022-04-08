public class foo{

    public foo(){}
    public static int blah(int a) {
        return 3;
    }
    public static int test (){
        int c = foo.blah(3);
        int d = c + 4;
        return d;

    }
}