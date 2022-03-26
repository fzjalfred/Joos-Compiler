public class foo{
    public foo(){

    }
    public int bar(int a) {
        return a;
    }

    public int main(){
        int a = 10+10*5;
        int b = 20;
        int r = bar(a);
        boolean bool = false;
        boolean bool2 = true;
        {
            int c = 10 -5;
            int d = 10 / 5;
        }
        if (a == 3) {
            a = 4;
            b = 5;
        }
        return a;
    }
}