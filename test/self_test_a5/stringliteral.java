public class stringliteral{
    public stringliteral(){}

    public static int test(){
        int a = 0;
        stringliteral s = new stringliteral();
        if ("this is a" == "this is a"){
            a = a+1;
        }
        if ("this is a" != "this is not a"){
            a = a+1;
        }
        return a;
    }
}