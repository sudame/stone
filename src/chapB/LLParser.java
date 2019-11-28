package chapB;

import stone.CodeDialog;
import stone.Lexer;
import stone.ParseException;
import stone.Token;

import java.util.Deque;
import java.util.LinkedList;

public class LLParser {
    private enum NonTerminal {
        Expression, Expression2, Term, Term2, Factor
    }

    static class Symbol {
        boolean isTerminal;
        String tokenString;
        NonTerminal nt;

        Symbol(String str) {
            isTerminal = true;
            tokenString = str;
        }

        Symbol(NonTerminal nt) {
            isTerminal = false;
            this.nt = nt;
        }

        public String toString() {
            return isTerminal ? tokenString : nt.name();
        }
    }

    private Lexer lexer;
    private Deque<Symbol> stack;

    final static private Symbol
            ExpressionSymbol = new Symbol(NonTerminal.Expression),
            Expression2Symbol = new Symbol(NonTerminal.Expression2),
            TermSymbol = new Symbol(NonTerminal.Term),
            Term2Symbol = new Symbol(NonTerminal.Term2),
            FactorSymbol = new Symbol(NonTerminal.Factor),
            LparenSymbol = new Symbol("("),
            RparenSymbol = new Symbol(")"),
            AddSymbol = new Symbol("+"),
            MultSymbol = new Symbol("*"),
            NumberSymbol = new Symbol("NUMBER");
    final static private Symbol[][] rules = {
            null,
            {TermSymbol, Expression2Symbol},  // 1
            {}, // 2

            // {AddSymbol, TermSymbol}, // 3

            {AddSymbol, ExpressionSymbol}, // 3  2019-11-22訂正
            {FactorSymbol, Term2Symbol}, // 4
            {}, // 5
            {MultSymbol, TermSymbol}, // 6
            {NumberSymbol}, // 7
            {LparenSymbol, ExpressionSymbol, RparenSymbol}  //8
    };
    final static private int[][] table = {
            {0, 0, 1, 1, 0, 0},
            {3, 0, 0, 0, 2, 2},
            {0, 0, 4, 4, 0, 0},
            {5, 6, 0, 0, 5, 5},
            {0, 0, 7, 8, 0, 0}
    };

    private static int terminalNumber(Token t) throws ParseException {
        if (t.isNumber()) {
            return 2;
        }
        switch (t.getText()) {
            case "+":
                return 0;
            case "*":
                return 1;
            case "(":
                return 3;
            case ")":
                return 4;
            case "\\n":
                return 5;
            default:
                throw new ParseException("Unknown token: " + t.getText());
        }
    }

    private LLParser(Lexer p) {
        lexer = p;
        stack = new LinkedList<>();
        stack.push(new Symbol(Token.EOL));
        stack.push(new Symbol(NonTerminal.Expression));
    }

    private void token(String name) throws ParseException {
        Token t = lexer.read();
        if (!(t.isIdentifier() && name.equals(t.getText())))
            throw new ParseException(t);
    }

    private void printStack() {
        System.out.print("[ ");
        for (Symbol s : stack) {
            System.out.print(s + " ");
        }
        System.out.print("]");
    }

    public void parse() throws ParseException {
        while (!stack.isEmpty()) {
            Token t = lexer.peek(0);
            System.out.print(t.getText() + ": ");
            printStack();
            Symbol top = stack.peek();
            assert top != null; // top is Nullable.
            if (top.isTerminal) {
                if (top == NumberSymbol) {
                    if (t.isNumber()) {
                        lexer.read();
                        stack.pop();
                    } else {
                        throw new ParseException("Number expected: " + t.getText());
                    }
                } else {
                    token(top.tokenString);
                    stack.pop();
                }
                System.out.println(" match");
            } else {
                int ruleNumber = table[top.nt.ordinal()][terminalNumber(t)]; // get rule number from rule-table
                if (ruleNumber == 0) throw new ParseException("Unexpected token: " + t.getText()); // error handling
                Symbol[] symbols = rules[ruleNumber]; // get symbols
                stack.pop();
                for (int i = symbols.length - 1; i >= 0; i--) stack.push(symbols[i]); // replace token
                System.out.println(" rule: " + ruleNumber); // print rule number
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        Lexer lexer = new Lexer(new CodeDialog());
        LLParser p = new LLParser(lexer);
        p.parse();
    }
}