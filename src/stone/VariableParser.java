package stone;

import stone.ast.VariableStmnt;

import static stone.Parser.rule;

public class VariableParser extends FuncParser {
    /* ヒント: insertChoice()を活用すること */
    Parser variable = rule(VariableStmnt.class).sep("variable").identifier(reserved);

    public VariableParser() {
        statement.insertChoice(variable);
    }
}