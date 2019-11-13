package chap5;
import stone.ast.ASTree;
import stone.*;

public class ExprParserRunner {
    public static void main(String[] args) throws ParseException {
        Lexer lex = new Lexer(new CodeDialog());
        ExprParser p = new ExprParser();
        while (lex.peek(0) != Token.EOF) {
            ASTree ast = p.parse(lex);
            System.out.println("=> " + ast.toString());
        }
    }
}
