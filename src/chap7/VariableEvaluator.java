package chap7;

import chap6.Environment;
import javassist.gluonj.Require;
import javassist.gluonj.Reviser;
import stone.ast.ASTree;
import stone.ast.VariableStmnt;

import java.util.List;

@Require(FuncEvaluator.class)
@Reviser public class VariableEvaluator {
    @Reviser public static class VariableStmntEx extends VariableStmnt {
        public VariableStmntEx(List<ASTree> list) {
            super(list);
        }

        public Object eval(Environment env) {
            ((FuncEvaluator.EnvEx)env).putNew(name(), value());
            return name();
        }
    }
}
