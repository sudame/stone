package chap5;

import stone.ast.ASTree;
import stone.*;

public class MyParserRunner {
    public static void main(String[] args) throws ParseException {
        Lexer lex = new Lexer(new CodeDialog());
        BasicParser bp = new MyBasicParser();
        while (lex.peek(0) != Token.EOF) {
            ASTree ast = bp.parse(lex);
            System.out.println("=> " + ast.toString());
        }
    }
}