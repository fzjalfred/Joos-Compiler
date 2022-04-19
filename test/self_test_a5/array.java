public class array{
    public array(){
        arr = new int[10];
    }
    public int[] arr;
    public array self;

    public static int test(){
        char c = 'a';
        array a1 = new array();
        for (int i = 0; i < 10; i = i+1){
            a1.arr[i] = i;
        }
        return a1.arr.hashCode();
    }

}