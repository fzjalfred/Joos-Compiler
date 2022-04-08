package backend.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tile {

    public List<Code> codes;

    public Tile(){
        codes = new ArrayList<Code>();
    }
    public Tile(Code... codes){
        this(Arrays.asList(codes));
    }
    public Tile(List<Code> codes){
        this.codes = codes;
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
