
public class Point{
    public int x;
    public Point(){}

    public int setX(int x){
        this.x = x;
        return this.x;
    }

    public int moveX(int x){
        int res = this.setX(x);
        return res;
    }
}
