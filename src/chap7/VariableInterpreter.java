package chap7;

import stone.FuncParser;
import stone.ParseException;

import chap6.BasicInterpreter;
import stone.VariableParser;

public class VariableInterpreter extends BasicInterpreter{
    public static void main(String[] args) throws ParseException {
        run(new VariableParser(), new NestedEnv());
    }
}
