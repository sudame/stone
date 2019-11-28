package chapB;

import stone.CodeDialog;
import stone.Lexer;
import stone.ParseException;
import stone.Token;
import stone.ast.ASTLeaf;
import stone.ast.ASTree;
import stone.ast.BinaryExpr;
import stone.ast.NumberLiteral;

import java.util.Arrays;

public class MyExprParser {
    private Lexer lexer;

    public MyExprParser(Lexer p) {
        lexer = p;
    }
    public ASTree expression() throws ParseException {
        ASTree left = term();
        while (isToken("+")) {
            ASTLeaf op = new ASTLeaf(lexer.read());
            ASTree right = expression();
            left = new BinaryExpr(Arrays.asList(left, op, right));
        }
        return left;
    }
    public ASTree term() throws ParseException {
        ASTree left = factor();
        while (isToken("*")) {
            ASTLeaf op = new ASTLeaf(lexer.read());
            ASTree right = term();
            left = new BinaryExpr(Arrays.asList(left, op, right));
        }
        return left;
    }
    public ASTree factor() throws ParseException {
        if (isToken("(")) {
            token("(");
            ASTree e = expression();
            token(")");
            return e;
        }
        else {
            Token t = lexer.read();
            if (t.isNumber()) {
                NumberLiteral n = new NumberLiteral(t);
                return n;
            }
            else
                throw new ParseException(t);
        }
    }
    void token(String name) throws ParseException {
        Token t = lexer.read();
        if (!(t.isIdentifier() && name.equals(t.getText())))
            throw new ParseException(t);
    }
    boolean isToken(String name) throws ParseException {
        Token t = lexer.peek(0);
        return t.isIdentifier() && name.equals(t.getText());
    }

    public static void main(String[] args) throws ParseException {
        Lexer lexer = new Lexer(new CodeDialog());
        MyExprParser p = new MyExprParser(lexer);
        ASTree t = p.expression();
        System.out.println("=> " + t);
    }
}