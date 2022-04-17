public class foo{

    public foo(){
    }
    public int b = 10;
    public boolean bool;
    public foo field;
    public int bar(){return b;}
    public static int test (){
        boolean a = (1 == 2);
        foo f = new foo();
        int x = f.bar();
        return x;
    }
}

