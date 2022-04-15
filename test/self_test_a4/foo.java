public class foo{

    public foo(){
    }
    public int b = 10;
    public boolean bool;
    public foo field;

    public static int test (){
        boolean a = (1 == 2);
        foo f = new foo();
        f.bool = a;
        return 0;
    }
}
