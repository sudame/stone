package chap8;

import chap7.ClosureEvaluator;
import javassist.gluonj.util.Loader;

public class MyNativeRunner {
    public static void main(String[] args) throws Throwable {
        Loader.run(MyNativeInterpreter.class, args, NativeEvaluator.class,
                   ClosureEvaluator.class);
    }
}
