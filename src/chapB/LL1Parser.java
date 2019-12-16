package chapB;

import stone.CodeDialog;
import stone.Lexer;
import stone.ParseException;
import stone.Token;
import sun.jvm.hotspot.debugger.cdbg.BlockSym;

import java.util.Deque;
import java.util.LinkedList;


public class LL1Parser {
    private enum NonTerminal {
        Primary, Op, Expr2, Expr, StatementOpt, Delim, StatementList2,
        StatementList, Block, Simple, ElsePart, Statement, Program,
    }

    static class TopDownRule {
        int id;
        Symbol from;
        Symbol[] to;

        TopDownRule(int id, Symbol from, Symbol[] to) {
            this.id = id;
            this.from = from;
            this.to = to;
        }
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
            PrimarySymbol = new Symbol(NonTerminal.Primary),
            OpSymbol = new Symbol(NonTerminal.Op),
            Expr2Symbol = new Symbol(NonTerminal.Expr2),
            ExprSymbol = new Symbol(NonTerminal.Expr),
            StatementOptSymbol = new Symbol(NonTerminal.StatementOpt),
            DelimSymbol = new Symbol(NonTerminal.Delim),
            StatementList2Symbol = new Symbol(NonTerminal.StatementList2),
            StatementListSymbol = new Symbol(NonTerminal.StatementList),
            BlockSymbol = new Symbol(NonTerminal.Block),
            SimpleSymbol = new Symbol(NonTerminal.Simple),
            ElsePartSymbol = new Symbol(NonTerminal.ElsePart),
            StatementSymbol = new Symbol(NonTerminal.Statement),
            ProgramSymbol = new Symbol(NonTerminal.Program),
            EmptySymbol = new Symbol(""),
            LParenSymbol = new Symbol("("),
            RParenSymbol = new Symbol(")"),
            LBracketSymbol = new Symbol("{"),
            RBracketSymbol = new Symbol("}"),
            PlusSymbol = new Symbol("+"),
            MinusSymbol = new Symbol("-"),
            SemicolonSymbol = new Symbol(";"),
            EOLSymbol = new Symbol("EOL"),
            ElseSymbol = new Symbol("else"),
            IfSymbol = new Symbol("if"),
            WhileSymbol = new Symbol("while"),
            NumberSymbol = new Symbol("NUMBER"),
            EOFSymbol = new Symbol("EOF");

    final static private Symbol[] nonTerminalSymbols = {};

    final static private Symbol[] terminalSymbols = {};

    final static private TopDownRule[] topDownRule = {
            new TopDownRule(1, PrimarySymbol, new Symbol[]{LParenSymbol, ExprSymbol, RParenSymbol}),
            new TopDownRule(2, PrimarySymbol, new Symbol[]{NumberSymbol}),
            new TopDownRule(3, OpSymbol, new Symbol[]{PlusSymbol}),
            new TopDownRule(4,OpSymbol , new Symbol[]{MinusSymbol}),
            new TopDownRule(5, Expr2Symbol, new Symbol[]{EmptySymbol}),
            new TopDownRule(6, Expr2Symbol, new Symbol[]{OpSymbol, ExprSymbol}),
            new TopDownRule(7, ExprSymbol, new Symbol[]{PrimarySymbol, Expr2Symbol}),
            new TopDownRule(8, StatementOptSymbol, new Symbol[]{EmptySymbol}),
            new TopDownRule(9, StatementOptSymbol, new Symbol[]{StatementSymbol}),
            new TopDownRule(10, DelimSymbol, new Symbol[]{SemicolonSymbol}),
            new TopDownRule(11, DelimSymbol, new Symbol[]{EOLSymbol}),
            new TopDownRule(12, StatementList2Symbol, new Symbol[]{EmptySymbol}),
            new TopDownRule(13, StatementList2Symbol, new Symbol[]{DelimSymbol, StatementOptSymbol, StatementList2Symbol}),
            new TopDownRule(14, StatementListSymbol, new Symbol[]{StatementOptSymbol, StatementList2Symbol}),
            new TopDownRule(15,BlockSymbol , new Symbol[]{LBracketSymbol, StatementListSymbol, RBracketSymbol}),
            new TopDownRule(16, SimpleSymbol, new Symbol[]{ExprSymbol}),
            new TopDownRule(17, ElsePartSymbol, new Symbol[]{EmptySymbol}),
            new TopDownRule(18, ElsePartSymbol, new Symbol[]{ElseSymbol, BlockSymbol}),
            new TopDownRule(19,StatementSymbol , new Symbol[]{IfSymbol, ExprSymbol, BlockSymbol, ElsePartSymbol}),
            new TopDownRule(20, StatementSymbol, new Symbol[]{WhileSymbol, ExprSymbol, BlockSymbol}),
            new TopDownRule(21, StatementSymbol, new Symbol[]{SimpleSymbol}),
            new TopDownRule(22,ProgramSymbol , new Symbol[]{StatementOptSymbol, EOFSymbol}),
    };

    final static private Symbol[][] rules = {
            null,
            {LParenSymbol, ExprSymbol, RParenSymbol}, // 1
            {NumberSymbol}, // 2
            {PlusSymbol}, // 3
            {MinusSymbol}, // 4
            {EmptySymbol}, // 5
            {OpSymbol, ExprSymbol}, // 6
            {PrimarySymbol, Expr2Symbol}, // 7
            {EmptySymbol}, // 8
            {StatementSymbol}, // 9
            {SemicolonSymbol}, // 10
            {EOLSymbol}, // 11
            {EmptySymbol}, // 12
            {DelimSymbol, StatementOptSymbol, StatementList2Symbol}, // 13
            {StatementOptSymbol, StatementList2Symbol}, // 14
            {LBracketSymbol, StatementListSymbol, RBracketSymbol}, // 15
            {ExprSymbol}, // 16
            {EmptySymbol}, // 17
            {ElseSymbol, BlockSymbol}, // 18
            {IfSymbol, ExprSymbol, BlockSymbol, ElsePartSymbol}, // 19
            {WhileSymbol, ExprSymbol, BlockSymbol}, // 20
            {SimpleSymbol}, // 21
            {StatementOptSymbol, EOFSymbol}, // 22
    };

    //    final static private int[][] table = {
//            {0, 0, 1, 1, 0, 0},
//            {3, 0, 0, 0, 2, 2},
//            {0, 0, 4, 4, 0, 0},
//            {5, 6, 0, 0, 5, 5},
//            {0, 0, 7, 8, 0, 0}
//    };
//
//    private static int terminalNumber(Token t) throws ParseException {
//        if (t.isNumber()) {
//            return 2;
//        }
//        switch (t.getText()) {
//            case "+":
//                return 0;
//            case "*":
//                return 1;
//            case "(":
//                return 3;
//            case ")":
//                return 4;
//            case "\\n":
//                return 5;
//            default:
//                throw new ParseException("Unknown token: " + t.getText());
//        }
//    }
//
    private LL1Parser(Lexer p) {
        lexer = p;
        stack = new LinkedList<>();
        stack.push(new Symbol(Token.EOL));
        stack.push(new Symbol(NonTerminal.Program));
    }
//
//    private void token(String name) throws ParseException {
//        Token t = lexer.read();
//        if (!(t.isIdentifier() && name.equals(t.getText())))
//            throw new ParseException(t);
//    }
//
//    private void printStack() {
//        System.out.print("[ ");
//        for (Symbol s : stack) {
//            System.out.print(s + " ");
//        }
//        System.out.print("]");
//    }
//
//    public void parse() throws ParseException {
//        while (!stack.isEmpty()) {
//            Token t = lexer.peek(0);
//            System.out.print(t.getText() + ": ");
//            printStack();
//            Symbol top = stack.peek();
//            assert top != null; // top is Nullable.
//            if (top.isTerminal) {
//                if (top == NumberSymbol) {
//                    if (t.isNumber()) {
//                        lexer.read();
//                        stack.pop();
//                    } else {
//                        throw new ParseException("Number expected: " + t.getText());
//                    }
//                } else {
//                    token(top.tokenString);
//                    stack.pop();
//                }
//                System.out.println(" match");
//            } else {
//                int ruleNumber = table[top.nt.ordinal()][terminalNumber(t)]; // get rule number from rule-table
//                if (ruleNumber == 0) throw new ParseException("Unexpected token: " + t.getText()); // error handling
//                Symbol[] symbols = rules[ruleNumber]; // get symbols
//                stack.pop();
//                for (int i = symbols.length - 1; i >= 0; i--) stack.push(symbols[i]); // replace token
//                System.out.println(" rule: " + ruleNumber); // print rule number
//            }
//        }
//    }

    public static void main(String[] args) throws ParseException {
        Lexer lexer = new Lexer(new CodeDialog());
        LL1Parser p = new LL1Parser(lexer);
        p.parse();
    }
}