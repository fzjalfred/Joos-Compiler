public class Casts{
    public Casts(){

    }

    public static int test(){
        int a = 0;
        Object o = new Object();
        int[] arrs = new int[10];
        Casts[] casts = new Casts[10];
        if (o instanceof int[]){
            a = a+1;
        }
        if (arrs instanceof Object){
            a = a+1;
        }
        if (arrs instanceof int[]){
            a = a+1;
        }
        if (casts instanceof Object){
            a = a+1;
        }
        if (casts instanceof Object[]){
            a = a+1;
        }
        if (casts instanceof Casts[]){
            a = a+1;
        }
        return a;
    }
}