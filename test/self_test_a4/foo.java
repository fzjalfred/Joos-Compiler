public class foo{

    public foo(){}
    public int b = 10;
    public foo field;
    public static int test (){
	foo f = new foo();
	f.field = new foo();
	f.field.b = 2;
        return f.field.b;
    }
}
