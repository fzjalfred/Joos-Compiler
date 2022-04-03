package backend.asm;

public class RegFactory {

    static int cnt = 0;
    public static Register getRegister(){
        Integer reg_id = cnt;
        cnt++;
        return new Register("t" + reg_id.hashCode() + '_' +reg_id);
    }
}
