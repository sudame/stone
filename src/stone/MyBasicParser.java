package stone;

import stone.ast.*;

import static stone.Parser.Operators;
import static stone.Parser.rule;

public class MyBasicParser extends BasicParser {
    public MyBasicParser() {


        statement0 = rule();
        block = rule(BlockStmnt.class)
                .sep("{").option(statement0)
                .repeat(rule().sep(";", Token.EOL).option(statement0))
                .sep("}");
        simple = rule(PrimaryExpr.class).ast(expr);
        statement = statement0.or(
                rule(IfStmnt.class).sep("if").ast(expr).ast(block)
                        .repeat(rule().sep("elsif").ast(expr).ast(block))
                        .option(rule().sep("else").ast(block)),
                rule(WhileStmnt.class).sep("while").ast(expr).ast(block),
                simple);

        program = rule().or(statement, rule(NullStmnt.class))
                .sep(";", Token.EOL, ".");

        operators.add("^", 4, Operators.RIGHT);
    }
}
