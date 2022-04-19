public class Amain {
    public Amain() {}
    public static int test() {
        // interface1 testin1 = null; // not passed
        interface1 testin1 = new instanceA();
        //testin1.myA()e;
        testin1.setX();

        instanceA testA2 = new instanceA();
        testA2.setX();
        return 52;
    }
}
