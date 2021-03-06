package javassist.tools;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import java.util.UUID;
import java.util.HashMap;

public abstract class Callback
{
    public static HashMap callbacks;
    private final String sourceCode;
    
    public Callback(final String s) {
        super();
        final String string = UUID.randomUUID().toString();
        Callback.callbacks.put(string, this);
        this.sourceCode = "((javassist.tools.Callback) javassist.tools.Callback.callbacks.get(\"" + string + "\")).result(new Object[]{" + s + "});";
    }
    
    public abstract void result(final Object[] p0);
    
    @Override
    public String toString() {
        return this.sourceCode();
    }
    
    public String sourceCode() {
        return this.sourceCode;
    }
    
    public static void insertBefore(final CtBehavior ctBehavior, final Callback callback) throws CannotCompileException {
        ctBehavior.insertBefore(callback.toString());
    }
    
    public static void insertAfter(final CtBehavior ctBehavior, final Callback callback) throws CannotCompileException {
        ctBehavior.insertAfter(callback.toString(), false);
    }
    
    public static void insertAfter(final CtBehavior ctBehavior, final Callback callback, final boolean b) throws CannotCompileException {
        ctBehavior.insertAfter(callback.toString(), b);
    }
    
    public static int insertAt(final CtBehavior ctBehavior, final Callback callback, final int n) throws CannotCompileException {
        return ctBehavior.insertAt(n, callback.toString());
    }
    
    static {
        Callback.callbacks = new HashMap();
    }
}
