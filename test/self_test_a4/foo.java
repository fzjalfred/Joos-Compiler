public class foo{

    public foo(){}
    public int b = 10;
    public static int test (){
        foo f = new foo();
        f.b = 2;
        return f.b;

    }
}