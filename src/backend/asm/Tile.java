package backend.asm;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    public List<Code> codes;
    public Register res_register;

    public Tile(){
        codes = new ArrayList<Code>();
        res_register = null;
    }
    public Tile(List<Code> codes){
        this.codes = codes;
        res_register = null;
    }

    public Tile(Register res_register){
        codes = new ArrayList<Code>();
        this.res_register = res_register;
    }

    @Override
    public String toString() {
        String res = "";
        for (Code c: codes){
            res += (c.toString() + '\n');
        }
        return res;
    }
}
