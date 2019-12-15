package chapB;

import stone.CodeDialog;
import stone.Lexer;
import stone.ParseException;
import stone.Token;

import java.util.Deque;
import java.util.LinkedList;

public class LRParser {
    private enum NonTerminal {
        S, A
    }

    static class Symbol {
        boolean isTerminal;
        String tokenString;
        NonTerminal nt;
        int num;

        Symbol(String str, int num) {
            isTerminal = true;
            tokenString = str;
            this.num = num;
        }

        Symbol(NonTerminal nt, int num) {
            isTerminal = false;
            this.nt = nt;
            this.num = num;
        }

        public String toString() {
            return isTerminal ? tokenString : nt.name();
        }
    }

    static class Rule {
        Symbol lhs;
        int length;

        Rule(Symbol sym, int len) {
            lhs = sym;
            length = len;
        }
    }

    static abstract class Command {
        abstract void exec() throws ParseException;
    }

    class Shift extends Command {
        int state;

        Shift(int state) {
            this.state = state;
        }

        void exec() throws ParseException {
            System.out.println("shift: " + state);
            lexer.read();
            nextSymbol();
            stack.push(state);
        }
    }

    class Reduce extends Command {
        int ruleNum;

        Reduce(int ruleNum) {
            this.ruleNum = ruleNum;
        }

        void exec() throws ParseException {
            System.out.println("reduce: " + ruleNum);
            Rule rule = rules[ruleNum];
            if (rule == null) {
                throw new ParseException("error.");
            }

            for (int i = 0; i < rule.length - 1; i++) {
                stack.pop();
            }
            x = rule.lhs;
        }
    }

    class Accept extends Command {
        void exec() {
            System.out.println("accept.");
            done = true;
        }
    }

    class Error extends Command {
        void exec() {
            System.out.println("error.");
            done = true;
        }
    }

    private boolean done;
    private Lexer lexer;
    private Deque<Integer> stack;
    private Symbol x;

    private void nextSymbol() throws ParseException {
        // 現在の入力トークン列の先頭に対応するシンボルをxに入れる
        String str = lexer.peek(0).getText();
        switch (str) {
            case "<":
                x = LparenSymbol;
                break;
            case ">":
                x = RparenSymbol;
                break;
            case Token.EOL:
                x = EOFSymbol;
                break;
            default:
                throw new ParseException("Illegal token " + str);
        }
    }

    final static private Symbol
            LparenSymbol = new Symbol("<", 0),
            RparenSymbol = new Symbol(">", 1),
            EOFSymbol = new Symbol("EOF", 2),
            A_Symbol = new Symbol(NonTerminal.A, 3),
            S_Symbol = new Symbol(NonTerminal.S, 4);
    final static private Rule[] rules = {
            null,
            new Rule(A_Symbol, 2), // 1
            new Rule(A_Symbol, 3), // 2
            new Rule(S_Symbol, 2)  // 3
    };
    final private Command[][] table = new Command[9][S_Symbol.num + 1];

    private LRParser(Lexer p) {
        lexer = p;
        stack = new LinkedList<>();
        Command error = new Error();
        for (int state = 0; state < 9; state++) {
            for (int symNum = 0; symNum <= S_Symbol.num; symNum++) {
                table[state][symNum] = error;
            }
        }

        // 遷移のルールを設定
        table[0][LparenSymbol.num] = new Shift(1);
        table[0][A_Symbol.num] = new Shift(3);
        table[0][S_Symbol.num] = new Shift(7);
        table[1][LparenSymbol.num] = new Shift(1);
        table[1][RparenSymbol.num] = new Reduce(1);
        table[1][A_Symbol.num] = new Shift(4);
        table[3][LparenSymbol.num] = new Shift(1);
        table[3][A_Symbol.num] = new Reduce(3);
        table[4][RparenSymbol.num] = new Reduce(2);
        table[7][EOFSymbol.num] = new Accept();

    }

    private void printStack() {
        System.out.print("[ ");
        for (int s : stack) {
            System.out.print(s + " ");
        }
        System.out.print("]");
    }

    public void parse() throws ParseException {
        stack.push(0);
        nextSymbol();
        done = false;
        while (!done) {
            System.out.print(x + ": ");
            printStack();
            table[stack.peek()][x.num].exec();


        }
    }

    public static void main(String[] args) throws ParseException {
        Lexer lexer = new Lexer(new CodeDialog());
        LRParser p = new LRParser(lexer);
        p.parse();
    }
}