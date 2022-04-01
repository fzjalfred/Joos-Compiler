public class foo{
    public foo(){

    }
    public int bar(){
        return;
    }

    public int main(){
        bar();
        int a = 10;
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