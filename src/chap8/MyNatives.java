package chap8;

import chap6.Environment;
import stone.StoneException;

import javax.swing.*;
import java.lang.reflect.Method;

public class MyNatives {
    public Environment environment(Environment env) {
        appendNatives(env);
        return env;
    }
    protected void appendNatives(Environment env) {
        append(env, "print", MyNatives.class, "print", Object.class);
        append(env, "read", MyNatives.class, "read");
        append(env, "length", MyNatives.class, "length", String.class);
        append(env, "toInt", MyNatives.class, "toInt", Object.class);
        append(env, "currentTime", MyNatives.class, "currentTime");
        append(env, "charAt", MyNatives.class, "charAt", String.class, int.class);
        append(env, "nativeFib", MyNatives.class, "nativeFib",  int.class);
    }
    protected void append(Environment env, String name, Class<?> clazz,
                          String methodName, Class<?> ... params) {
        Method m;
        try {
            m = clazz.getMethod(methodName, params);
        } catch (Exception e) {
            throw new StoneException("cannot find a native function: "
                                     + methodName);
        }
        env.put(name, new NativeFunction(methodName, m));
    }

    // native methods
    public static int print(Object obj) {
        System.out.println(obj.toString());
        return 0;
    }
    public static String read() {
        return JOptionPane.showInputDialog(null);
    }
    public static int length(String s) { return s.length(); }
    public static int toInt(Object value) {
        if (value instanceof String)
            return Integer.parseInt((String)value);
        else if (value instanceof Integer)
            return ((Integer)value).intValue();
        else
            throw new NumberFormatException(value.toString());
    }
    private static long startTime = System.currentTimeMillis();
    public static int currentTime() {
        return (int)(System.currentTimeMillis() - startTime);
    }
    public static String charAt(String word, int index) {
        return String.valueOf(word.charAt(index));
    }
    public static int nativeFib(int n) {
        if(n < 2) {
            return n;
        } else {
            return nativeFib(n - 1) + nativeFib(n - 2);
        }
    }
}
