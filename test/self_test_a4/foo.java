public class foo{
    public foo(){

    }
    public int bar(){
        return 11;
    }

    public int main(){
        int a = bar();
        int b = 5;
        {
            int c = 4;
            int d = 5;
        }
        if (a > 3 && b < 10){
            return a+b;
        }
        return 0;
    }
}