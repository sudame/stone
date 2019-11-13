package chap4;

import stone.CodeDialog;
import stone.Lexer;
import stone.ParseException;
import stone.Token;
import stone.ast.ASTLeaf;
import stone.ast.ASTree;
import stone.ast.BinaryExpr;
import stone.ast.NumberLiteral;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class TreeBuilder {
    private static Set<String> OPERATORS = new LinkedHashSet<>(Arrays.asList("+", "-", "*", "/"));

    public static void main(String[] args) throws ParseException {
        Lexer l = new Lexer(new CodeDialog());
        LinkedList<ASTree> stack = new LinkedList<>();
        for (Token t; (t = l.read()) != Token.EOF; ) {
            if (t.isNumber()) {

                stack.push(new NumberLiteral(t));
            }
            else if(OPERATORS.contains(t.getText())) {
                ASTree first = stack.pop();
                ASTree second = stack.pop();
                BinaryExpr binaryExpr = new BinaryExpr(Arrays.asList(second, new ASTLeaf(t), first));
                stack.push(binaryExpr);
            }
        }
        for (ASTree tree : stack) {
            System.out.println("=> " + tree);
        }
    }
}
