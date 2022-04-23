public class foo extends bar{
    public foo(){}
    
    
    public int sad(){
        return happy();
    }

    public static int test(){
        foo a = new foo();
        return a.sad();
    }
}