package javassist.bytecode.stackmap;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;

public class TypedBlock extends BasicBlock
{
    public int stackTop;
    public int numLocals;
    public TypeData[] localsTypes;
    public TypeData[] stackTypes;
    
    public static TypedBlock[] makeBlocks(final MethodInfo methodInfo, final CodeAttribute codeAttribute, final boolean b) throws BadBytecode {
        final TypedBlock[] array = (TypedBlock[])new Maker().make(methodInfo);
        if (b && array.length < 2 && (array.length == 0 || array[0].incoming == 0)) {
            return null;
        }
        array[0].initFirstBlock(codeAttribute.getMaxStack(), codeAttribute.getMaxLocals(), methodInfo.getConstPool().getClassName(), methodInfo.getDescriptor(), (methodInfo.getAccessFlags() & 0x8) != 0x0, methodInfo.isConstructor());
        return array;
    }
    
    protected TypedBlock(final int n) {
        super(n);
        this.localsTypes = null;
    }
    
    @Override
    protected void toString2(final StringBuffer sb) {
        super.toString2(sb);
        sb.append(",\n stack={");
        this.printTypes(sb, this.stackTop, this.stackTypes);
        sb.append("}, locals={");
        this.printTypes(sb, this.numLocals, this.localsTypes);
        sb.append('}');
    }
    
    private void printTypes(final StringBuffer sb, final int n, final TypeData[] array) {
        if (array == null) {
            return;
        }
        for (int i = 0; i < n; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            final TypeData typeData = array[i];
            sb.append((typeData == null) ? "<>" : typeData.toString());
        }
    }
    
    public boolean alreadySet() {
        return this.localsTypes != null;
    }
    
    public void setStackMap(final int stackTop, final TypeData[] stackTypes, final int numLocals, final TypeData[] localsTypes) throws BadBytecode {
        this.stackTop = stackTop;
        this.stackTypes = stackTypes;
        this.numLocals = numLocals;
        this.localsTypes = localsTypes;
    }
    
    public void resetNumLocals() {
        if (this.localsTypes != null) {
            int length;
            for (length = this.localsTypes.length; length > 0 && this.localsTypes[length - 1].isBasicType() == TypeTag.TOP && (length <= 1 || !this.localsTypes[length - 2].is2WordType()); --length) {}
            this.numLocals = length;
        }
    }
    
    void initFirstBlock(final int n, final int n2, final String s, final String s2, final boolean b, final boolean b2) throws BadBytecode {
        if (s2.charAt(0) != '(') {
            throw new BadBytecode("no method descriptor: " + s2);
        }
        this.stackTop = 0;
        this.stackTypes = TypeData.make(n);
        final TypeData[] make = TypeData.make(n2);
        if (b2) {
            make[0] = new TypeData.UninitThis(s);
        }
        else {
            make[0] = new TypeData.ClassName(s);
        }
        int numLocals = b ? -1 : 0;
        int descToTag = 1;
        try {
            while ((descToTag = descToTag(s2, descToTag, ++numLocals, make)) > 0) {
                if (make[numLocals].is2WordType()) {
                    make[++numLocals] = TypeTag.TOP;
                }
            }
        }
        catch (StringIndexOutOfBoundsException ex) {
            throw new BadBytecode("bad method descriptor: " + s2);
        }
        this.numLocals = numLocals;
        this.localsTypes = make;
    }
    
    private static int descToTag(final String s, int n, final int n2, final TypeData[] array) throws BadBytecode {
        final int n3 = n;
        int n4 = 0;
        char c = s.charAt(n);
        if (c == ')') {
            return 0;
        }
        while (c == '[') {
            ++n4;
            c = s.charAt(++n);
        }
        if (c == 'L') {
            int index = s.indexOf(59, ++n);
            if (n4 > 0) {
                array[n2] = new TypeData.ClassName(s.substring(n3, ++index));
            }
            else {
                array[n2] = new TypeData.ClassName(s.substring(n3 + 1, ++index - 1).replace('/', '.'));
            }
            return index;
        }
        if (n4 > 0) {
            array[n2] = new TypeData.ClassName(s.substring(n3, ++n));
            return n;
        }
        final TypeData primitiveTag = toPrimitiveTag(c);
        if (primitiveTag == null) {
            throw new BadBytecode("bad method descriptor: " + s);
        }
        array[n2] = primitiveTag;
        return n + 1;
    }
    
    private static TypeData toPrimitiveTag(final char c) {
        switch (c) {
            case 'B':
            case 'C':
            case 'I':
            case 'S':
            case 'Z':
                return TypeTag.INTEGER;
            case 'J':
                return TypeTag.LONG;
            case 'F':
                return TypeTag.FLOAT;
            case 'D':
                return TypeTag.DOUBLE;
            default:
                return null;
        }
    }
    
    public static String getRetType(final String s) {
        final int index = s.indexOf(41);
        if (index < 0) {
            return "java.lang.Object";
        }
        final char char1 = s.charAt(index + 1);
        if (char1 == '[') {
            return s.substring(index + 1);
        }
        if (char1 == 'L') {
            return s.substring(index + 2, s.length() - 1).replace('/', '.');
        }
        return "java.lang.Object";
    }
}
