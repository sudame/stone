package chap5;
import static stone.Parser.rule;

import stone.*;
import stone.ast.*;

class ExprParser {
    private Parser expr0 = rule();

    private Parser factor =  rule().or(rule().number(), rule().sep("(").ast(expr0).sep(")"));
    private Parser term =  factor.repeat(rule().token("*", "/").ast(factor)) ;
    private Parser expr =  expr0.ast(term).repeat(rule().token("+", "-").ast(factor));
    private Parser eol = rule().sep(Token.EOL);
    private Parser top = rule().ast(expr).repeat(rule().ast(eol));

    ASTree parse(Lexer lexer) throws ParseException {
        return top.parse(lexer);
    }
}