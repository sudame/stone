package chap6;

import chap6.BasicEvaluator.ASTreeEx;
import javassist.gluonj.Require;
import javassist.gluonj.Reviser;
import stone.ast.ASTree;
import stone.ast.BlockStmnt;
import stone.ast.IfStmnt;

import java.util.List;

import static chap6.BasicEvaluator.FALSE;

@Require(BasicEvaluator.class)
@Reviser
public class MyBasicEvaluator {
    @Reviser
    public static class BlockEx extends BlockStmnt {
        public BlockEx(List<ASTree> c) {
            super(c);
        }

        public Object eval(Environment env) {
            Object result = "nothing";
            for (ASTree t : this) {
                result = ((ASTreeEx) t).eval(env);
            }
            return result;
        }
    }

    @Reviser
    public static class IfEx extends IfStmnt {
        public IfEx(List<ASTree> c) {
            super(c);
        }

        public Object eval(Environment env) {
            Object c = ((ASTreeEx) condition()).eval(env);
            if (c instanceof Integer && (Integer) c == FALSE) {
                ASTree b = elseBlock();
                if (b == null) {
                    return 0;
                } else {
                    return ((ASTreeEx) b).eval(env);
                }
            } else
                return ((ASTreeEx) thenBlock()).eval(env);
        }
    }
}