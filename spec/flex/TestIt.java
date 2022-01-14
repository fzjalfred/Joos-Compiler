class TestIt {
    public static void main(String[] argv) throws java.io.IOException {
        MyLexer lex = new MyLexer(System.in);
        while (true) {
            MyLexer.Token t = lex.nextToken();
            if (t == null) break;
            System.out.println("token: " + t);
        }
    }
}
