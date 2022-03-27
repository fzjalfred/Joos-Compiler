public class foo{
    public foo(){

    }
    public int bar(int a) {
        return a;
    }

    public int main(){
        int a = 10+10*5;
        if (a >1 && a > 3 && a >2 ) {
            a = 4;
        }
        return -bar(a);
    }
}