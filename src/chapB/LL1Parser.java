package chapB;

import com.sun.tools.corba.se.idl.constExpr.LessThan;
import stone.CodeDialog;
import stone.Lexer;
import stone.ParseException;
import stone.Token;

import java.util.*;
import java.util.stream.IntStream;


public class LL1Parser {
    private enum NonTerminal {
        Primary, Op, Expr2, Expr, StatementOpt, Delim, StatementList2,
        StatementList, Block, Simple, ElsePart, Statement, Program, Term, Term2, Factor, Factor2
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
            TermSymbol = new Symbol(NonTerminal.Term),
            Term2Symbol = new Symbol(NonTerminal.Term2),
            FactorSymbol = new Symbol(NonTerminal.Factor),
            EmptySymbol = new Symbol("EMPTY"),
            LParenSymbol = new Symbol("("),
            RParenSymbol = new Symbol(")"),
            LBracketSymbol = new Symbol("{"),
            RBracketSymbol = new Symbol("}"),
            PlusSymbol = new Symbol("+"),
            MinusSymbol = new Symbol("-"),
            LessThanSymbol = new Symbol("<"),
            EqualSymbol = new Symbol("="),
            MultipleSymbol = new Symbol("*"),
            SemicolonSymbol = new Symbol(";"),
            EOLSymbol = new Symbol("\\n"),
            ElseSymbol = new Symbol("else"),
            IfSymbol = new Symbol("if"),
            WhileSymbol = new Symbol("while"),
            NumberSymbol = new Symbol("NUMBER"),
            IdentifierSymbol= new Symbol("str"),
            StringSymbol = new Symbol("STRING"),
            EOFSymbol = new Symbol("EOF");

    final private LinkedHashSet<Symbol> nonTerminalSymbols = new LinkedHashSet<>();

    final private LinkedHashSet<Symbol> terminalSymbols = new LinkedHashSet<>();

    final static private LinkedHashMap<Symbol, LinkedHashSet<Symbol>> first = new LinkedHashMap<>();
    final static private LinkedHashMap<Symbol, LinkedHashSet<Symbol>> follow = new LinkedHashMap<>();
    final static private LinkedHashMap<Integer, LinkedHashSet<Symbol>> directors = new LinkedHashMap<>();

    final static private TopDownRule[] topDownRules = {
            new TopDownRule(1, PrimarySymbol, new Symbol[]{LParenSymbol, ExprSymbol, RParenSymbol}),
            new TopDownRule(2, PrimarySymbol, new Symbol[]{NumberSymbol}),
            new TopDownRule(3, OpSymbol, new Symbol[]{PlusSymbol}),
            new TopDownRule(4, OpSymbol, new Symbol[]{MinusSymbol}),
            new TopDownRule(5, Expr2Symbol, new Symbol[]{}),
            new TopDownRule(6, Expr2Symbol, new Symbol[]{OpSymbol, ExprSymbol}),
            new TopDownRule(7, ExprSymbol, new Symbol[]{PrimarySymbol, Expr2Symbol}),
            new TopDownRule(8, StatementOptSymbol, new Symbol[]{}),
            new TopDownRule(9, StatementOptSymbol, new Symbol[]{StatementSymbol}),
            new TopDownRule(10, DelimSymbol, new Symbol[]{SemicolonSymbol}),
            new TopDownRule(11, DelimSymbol, new Symbol[]{EOLSymbol}),
            new TopDownRule(12, StatementList2Symbol, new Symbol[]{}),
            new TopDownRule(13, StatementList2Symbol, new Symbol[]{DelimSymbol, StatementOptSymbol, StatementList2Symbol}),
            new TopDownRule(14, StatementListSymbol, new Symbol[]{StatementOptSymbol, StatementList2Symbol}),
            new TopDownRule(15, BlockSymbol, new Symbol[]{LBracketSymbol, StatementListSymbol, RBracketSymbol}),
            new TopDownRule(16, SimpleSymbol, new Symbol[]{ExprSymbol}),
            new TopDownRule(17, ElsePartSymbol, new Symbol[]{}),
            new TopDownRule(18, ElsePartSymbol, new Symbol[]{ElseSymbol, BlockSymbol}),
            new TopDownRule(19, StatementSymbol, new Symbol[]{IfSymbol, ExprSymbol, BlockSymbol, ElsePartSymbol}),
            new TopDownRule(20, StatementSymbol, new Symbol[]{WhileSymbol, ExprSymbol, BlockSymbol}),
            new TopDownRule(21, StatementSymbol, new Symbol[]{SimpleSymbol}),
            new TopDownRule(22, ProgramSymbol, new Symbol[]{StatementOptSymbol, EOLSymbol}),

            // for test
//            new TopDownRule(1, ExprSymbol, new Symbol[]{TermSymbol, Expr2Symbol}),
//            new TopDownRule(2, Expr2Symbol, new Symbol[]{}),
//            new TopDownRule(3, Expr2Symbol, new Symbol[]{PlusSymbol, ExprSymbol}),
//            new TopDownRule(4, TermSymbol, new Symbol[]{FactorSymbol, Term2Symbol}),
//            new TopDownRule(5, Term2Symbol, new Symbol[]{}),
//            new TopDownRule(6, Term2Symbol, new Symbol[]{MultipleSymbol, TermSymbol}),
//            new TopDownRule(7, FactorSymbol, new Symbol[]{NumberSymbol}),
//            new TopDownRule(8, FactorSymbol, new Symbol[]{LParenSymbol, ExprSymbol, RParenSymbol}),
//            new TopDownRule(9, ProgramSymbol, new Symbol[]{ExprSymbol, EOLSymbol}),
    };

    private void initSymbols() {
        for (TopDownRule topDownRule : topDownRules) {
            // まずすべてのSymbolを追加
            terminalSymbols.add(topDownRule.from);
            terminalSymbols.addAll(Arrays.asList(topDownRule.to));
        }

        for (TopDownRule topDownRule : topDownRules) {
            terminalSymbols.remove(topDownRule.from);
            nonTerminalSymbols.add(topDownRule.from);
        }
    }

    private LinkedHashSet<Symbol> firstAlpha(Symbol[] alpha) {
        // 特に，First(空) = {空}
        if (alpha.length == 0) return new LinkedHashSet<>(Collections.singletonList(EmptySymbol));

        Symbol topSymbol = alpha[0];
        if (terminalSymbols.contains(topSymbol)) {
            // First(「終端記号」「記号列」) = {「終端記号」}
            return new LinkedHashSet<>(Collections.singletonList(topSymbol));
        } else {
            //  First(「非終端記号」「記号列」) =
            LinkedHashSet<Symbol> fst = new LinkedHashSet<>(first.get(topSymbol));
            if (fst.contains(EmptySymbol)) {
                // もし First(「非終端記号」) が空を含むなら
                // (First(「非終端記号」) - {空}) ∪ First(「記号列」)
                fst.remove(EmptySymbol);
                fst.addAll(firstAlpha(Arrays.copyOfRange(alpha, 1, alpha.length)));
                return fst;
            } else {
                // else First(「非終端記号」)
                return new LinkedHashSet<>(first.get(topSymbol));
            }
        }
    }

    private void genFirstSets() {
        // すべての非終端記号 A について，すべての First(A) の初期値を空集合{}とする.
        for (Symbol symbol : nonTerminalSymbols) {
            first.put(symbol, new LinkedHashSet<>());
        }

        // 以下を，変化がなくなるまで繰り返す.
        // すべての規則 A → α について，First(A) ∪ = First(α)
        while (true) {
            LinkedHashMap<Symbol, LinkedHashSet<Symbol>> oldFirst = new LinkedHashMap<>(first);

            for (TopDownRule rule : topDownRules) {
                LinkedHashSet<Symbol> fst = new LinkedHashSet<>(first.get(rule.from));
                fst.addAll(firstAlpha(rule.to));
                first.replace(rule.from, fst);
            }

            // 変化が無くなったかどうかの判定
            if (this.first.equals(oldFirst)) break;
        }

        System.out.print("");
    }


    private void genFollowSets() {
        for (Symbol symbol : nonTerminalSymbols) {
            // すべての非終端記号 B について Follow(B) を空集合に初期化する.
            follow.put(symbol, new LinkedHashSet<>());
        }
        // Follow(「開始記号」) = {EOF}
        follow.put(ProgramSymbol, new LinkedHashSet<>(Collections.singletonList(EOLSymbol)));

        // 以下を，変化しなくなるまで繰り返す.
        while (true) {
            LinkedHashMap<Symbol, LinkedHashSet<Symbol>> oldFollow = new LinkedHashMap<>(follow);

            for (TopDownRule rule : topDownRules) {
                Symbol[] toSymbols = rule.to;
                Symbol fromSymbol = rule.from;

                for (int i = 0; i < toSymbols.length; i++) {
                    Symbol toSymbol = toSymbols[i];
                    Symbol[] restSymbols = Arrays.copyOfRange(toSymbols, i + 1, toSymbols.length);
                    if (terminalSymbols.contains(toSymbol)) continue;
                    LinkedHashSet<Symbol> fst = new LinkedHashSet<>(firstAlpha(restSymbols));
                    if (fst.remove(EmptySymbol)) {
                        // First(「記号列」) が空を含むならば
                        // Follow(B) ∪ = (First(「記号列」) - {空}) ∪ Follow(A)
                        fst.addAll(new LinkedHashSet<>(follow.get(fromSymbol)));
                        fst.addAll(new LinkedHashSet<>(follow.get(toSymbol)));
                        follow.replace(toSymbol, fst);
                    } else {
                        fst.addAll(new LinkedHashSet<>(follow.get(toSymbol)));
                        follow.replace(toSymbol, fst);
                    }
                }
            }


            if (oldFollow.equals(this.follow)) break;
        }

        System.out.print("");
    }

    private void genDirectorSets() {
        for (TopDownRule rule : topDownRules) {
            int ruleId = rule.id;
            Symbol fromSymbol = rule.from;
            Symbol[] toSymbol = rule.to;

            LinkedHashSet<Symbol> fst = new LinkedHashSet<>(firstAlpha(toSymbol));
            if (fst.remove(EmptySymbol)) {
                fst.addAll(new LinkedHashSet<>(follow.get(fromSymbol)));
                directors.put(ruleId, fst);
            } else {
                directors.put(ruleId, fst);
            }
        }
    }


    private Symbol[][] rules() {
        LinkedList<LinkedList<Symbol>> ruleList = new LinkedList<>();
        // add Empty to first
        ruleList.add(new LinkedList<>(Collections.singletonList(EmptySymbol)));

        for (TopDownRule rule : topDownRules
        ) {
            ruleList.add(new LinkedList<>(Arrays.asList(rule.to)));
        }

        // List -> Array
        Symbol[][] rules = new Symbol[ruleList.size()][];
        for (int i = 0; i < rules.length; i++) {
            rules[i] = ruleList.get(i).toArray(new Symbol[0]);
        }
        return rules;
    }

    ;

    private int[][] table() {
        final LinkedHashSet<Symbol> _terminals = new LinkedHashSet<>(terminalSymbols);
        _terminals.remove(EmptySymbol);
        final LinkedList<Symbol> terminals = new LinkedList<>(_terminals);
        final LinkedList<Symbol> nonTerminals = new LinkedList<>(nonTerminalSymbols);

        final int[][] table = new int[nonTerminals.size()][terminals.size()];

        // 初期化
//        for (int i = 0; i < table.length; i++) {
//            for (int j = 0; j < table[i].length; j++) {
//                table[i][j] = 0;
//            }
//        }

        for (TopDownRule rule : topDownRules) {
            int ruleId = rule.id;
            LinkedHashSet<Symbol> director = directors.get(ruleId);
            LinkedList<Symbol> alpha = new LinkedList<>(director);

            for (Symbol t : alpha) {
                int row = nonTerminals.indexOf(rule.from);
                int column = terminals.indexOf(t);

                table[row][column] = ruleId;
            }
        }

        return table;
    }

    //
    private int terminalNumber(Token t) throws ParseException {
        final LinkedHashSet<Symbol> _terminals = new LinkedHashSet<>(terminalSymbols);
        _terminals.remove(EmptySymbol);
        final LinkedList<Symbol> terminals = new LinkedList<>(_terminals);

        String text = t.getText();

        if (t.isNumber()) text = "NUMBER";

        final String finalText = text;
        int index = IntStream.range(0, terminals.size()).map(i -> terminals.get(i).toString().equals(finalText) ? i : -1).max().orElse(-1);
        if (index < 0) {
            throw new ParseException("Unknown token: " + t.getText());
        }

        return index;
    }

    private int nonTerminalNumber(Symbol s) throws ParseException {
        final LinkedHashSet<Symbol> _nonTerminals = new LinkedHashSet<>(nonTerminalSymbols);
        _nonTerminals.remove(EmptySymbol);
        final LinkedList<Symbol> nonTerminals = new LinkedList<>(_nonTerminals);

        int index = IntStream.range(0, nonTerminals.size()).map(i -> nonTerminals.get(i).toString().equals(s.toString()) ? i : -1).max().orElse(-1);
        if (index < 0) {
            throw new ParseException("Unknown token: " + s.toString());
        }

        return index;
    }

    private LL1Parser(Lexer p) {
        lexer = p;
        stack = new LinkedList<>();
        stack.push(new Symbol(Token.EOL));
        stack.push(new Symbol(NonTerminal.Program));
    }

    //
    private void token(String name) throws ParseException {
        Token t = lexer.read();
        if (!(t.isIdentifier() && name.equals(t.getText())))
            throw new ParseException(t);
    }

    //
    private void printStack() {
        System.out.print("[ ");
        for (Symbol s : stack) {
            System.out.print(s + " ");
        }
        System.out.print("]");
    }

    //
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
                int ruleNumber = table()[nonTerminalNumber(top)][terminalNumber(t)]; // get rule number from rule-table
                if (ruleNumber == 0) throw new ParseException("Unexpected token: " + t.getText()); // error handling
                Symbol[] symbols = rules()[ruleNumber]; // get symbols
                stack.pop();
                for (int i = symbols.length - 1; i >= 0; i--) stack.push(symbols[i]); // replace token
                System.out.println(" rule: " + ruleNumber); // print rule number
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        Lexer lexer = new Lexer(new CodeDialog());
        LL1Parser p = new LL1Parser(lexer);

        p.initSymbols();
        p.genFirstSets();
        p.genFollowSets();
        p.genDirectorSets();
        int[][] t = p.table();
        System.out.println(Arrays.deepToString(t));

        p.parse();
    }
}