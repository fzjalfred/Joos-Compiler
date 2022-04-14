public class foo{

    public foo(){}
    public int b = 10;
    public boolean field;
    public static int test (){
        foo f = new foo();
        f.field = (true || false);
        return 0;
    }
}