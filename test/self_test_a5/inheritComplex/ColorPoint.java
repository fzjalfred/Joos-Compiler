public class ColorPoint extends Point{

    public ColorPoint(){}

    public int colored(){
        return 10;
    }

    public int setX(int x){
        this.x = x;
        return this.x + colored();
    }

    public static int test(){
        Point p = new ColorPoint();
        return p.moveX(10);
    }

}