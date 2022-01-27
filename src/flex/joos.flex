import java_cup.runtime.*;




%%

%class Lexer

%line
%column

%cup

%{  
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn, new Token(type, ""));
    }
    
    private Symbol symbol(int type, Object value) {
        String lexeme = value.toString();
        return new Symbol(type, yyline, yycolumn, new Token(type, lexeme));
    }
%}
   

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

NUM = ([1-9][0-9]+)|[0-9]
IDENT = [A-Za-z_][A-Za-z_0-9]*
STRING = \"([^\\\"]|\\.)*\"
SINGLECHAR = \'.\'
COMMENTS = (\/\/.*)|(\/\*[^]*\*\/)

%%
   
<YYINITIAL> {
    /** class structure. */
    "class"           { return symbol(sym.CLASS, "class"); }
    "extends"         { return symbol(sym.EXTENDS, "extends"); }
    "implements"      { return symbol(sym.IMPLEMENTS, "implements"); }
    "static"          { return symbol(sym.STATIC, "static"); }
    "import"          { return symbol(sym.IMPORT, "import"); }
    "package"         { return symbol(sym.PACKAGE, "package"); }

    /** Modifiers. */
    "public"          { return symbol(sym.PUBLIC, "public"); }
    "protected"       { return symbol(sym.PROTECTED, "protected"); }
    "abstract"        { return symbol(sym.ABSTRACT, "abstract"); }
    "native"          { return symbol(sym.NATIVE, "native"); }
    "final"           { return symbol(sym.FINAL, "final"); }
    "interface"       { return symbol(sym.INTERFACE, "interface"); }

    /** Method and field access. */
    "."               { return symbol(sym.DOT, "."); }

    

    /** Keywords. */
    "true"            { return symbol(sym.TRUELITERAL, "true"); }
    "false"           { return symbol(sym.FALSELITERAL, "false"); }


    /** Control flow. */
    "if"              { return symbol(sym.IF, "if"); }
    "else"            { return symbol(sym.ELSE, "else"); }
    "while"           { return symbol(sym.WHILE, "while"); }
    "for"             { return symbol(sym.FOR, "for"); }
    "return"          { return symbol(sym.RETURN, "return"); }

    //"print"           { return symbol(sym.PRINT); }

    "null"            { return symbol(sym.NULLLITERAL, "null"); }
    "new"             { return symbol(sym.NEW, "new"); }
    "this"            { return symbol(sym.THISLITERAL, "this"); }

    /** Types */
    "boolean"         { return symbol(sym.BOOL, "boolean"); }
    "int"             { return symbol(sym.INT, "int"); }
    "char"            { return symbol(sym.CHAR, "char"); }
    "byte"            { return symbol(sym.BYTE, "byte"); }
    "short"           { return symbol(sym.SHORT, "short"); }
    "void"            { return symbol(sym.VOID, "void"); }



    /** Literals and names */
    //"\'"              { return symbol(sym.SINGLEQUOTE); }
    //"\""              { return symbol(sym.DOUBLEQUOTE); }

    "="                { return symbol(sym.ASSIGN, "="); }
    "=="               { return symbol(sym.EQ, "=="); }
    "<"               { return symbol(sym.LT, "<"); }
    "<="              { return symbol(sym.LE, "<="); }
    ">"               { return symbol(sym.GT, ">"); }
    ">="              { return symbol(sym.GE, ">="); }
    "!="              { return symbol(sym.NE, "!="); }
//    "+="              { return symbol(sym.PLUSEQ); }
//    "-="              { return symbol(sym.MINUSEQ); }

    ";"                { return symbol(sym.SEMICOLUMN, ";"); }
    ","                { return symbol(sym.COMMA, ","); }

    
    /** Method structure */
    "("                { return symbol(sym.LEFTPARN, "("); }
    ")"                { return symbol(sym.RIGHTPARN, ")"); }
    "["                { return symbol(sym.LEFTBRACKET, "["); }
    "]"                { return symbol(sym.RIGHTBARCKET, "]"); }
    "{"                { return symbol(sym.LEFTCURLY, "{"); }
    "}"                { return symbol(sym.RIGHTCURLY, "}"); }
    "+"                { return symbol(sym.PLUS, "+"); }
    "-"                { return symbol(sym.MINUS, "-"); }
    "*"                { return symbol(sym.STAR, "*"); }
    "%"                { return symbol(sym.MOD, "%");  }
    "/"                { return symbol(sym.DIVIDES, "/"); }

    /** logic OP */
    "&"                { return symbol(sym.BITWISEAND, "&"); }
    "|"                { return symbol(sym.BITWISEOR, "|"); }
    "!"                { return symbol(sym.NOT, "!"); }
    "||"               { return symbol(sym.BINARYOR, "||"); }
    "&&"               { return symbol(sym.BINARYAND, "&&"); }
    "instanceof"      { return symbol(sym.INSTANCEOF, "instanceof"); }
   
    {NUM}      { return symbol(sym.INTGERLITERAL, new Integer(yytext())); }
    {IDENT}       { return symbol(sym.ID, new String(yytext()));}
    {STRING}      { return symbol(sym.STRINGLITERAL, new String(yytext())); }
    {SINGLECHAR}  { return symbol(sym.CHARLITERAL, new String(yytext())); }

    {COMMENTS}      { /* do nothing */ }

    {WhiteSpace}       { /* do nothing */ }   
    <<EOF>> { return symbol(sym.EOF); }
}


/* error */ 
[^]                    { throw new Error("Illegal character <"+yytext()+">"); }
