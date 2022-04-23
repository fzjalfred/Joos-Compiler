public class StringTest{
    public StringTest(){

    }

    public static int test(){
        int res = 0;
        for (int i = 0; i < "haha".length(); i = i+1) res = "haha".charAt(i) + res;
        return res;
    }
}