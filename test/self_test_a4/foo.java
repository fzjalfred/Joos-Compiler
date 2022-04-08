public class foo{
    public foo(){
        super();
    }
    public int bar(int c,int d){
        return 10;
    }
    public void test(int a, int b){
        int c = a+b;
        bar(1,2);
        int d = 0;
    }
}