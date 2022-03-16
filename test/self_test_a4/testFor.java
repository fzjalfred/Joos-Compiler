public class testFor {
    public testFor() {

    }

    public static int foo() {
        for (int i = 100; i<100; i=i+1) {
            i = i + 1;
        }
        return 0;
    }

    public int bar(int x) {
        while (x>0) { 
            int y = x;
            y=y-1;  
            x = y;
        };
        return x;
    }
}