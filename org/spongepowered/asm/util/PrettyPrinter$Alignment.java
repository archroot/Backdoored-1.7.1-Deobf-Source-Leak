package org.spongepowered.asm.util;

public enum Alignment
{
    LEFT, 
    RIGHT;
    
    private static final /* synthetic */ Alignment[] $VALUES;
    
    public static Alignment[] values() {
        return Alignment.$VALUES.clone();
    }
    
    public static Alignment valueOf(final String s) {
        return Enum.<Alignment>valueOf(Alignment.class, s);
    }
    
    static {
        $VALUES = new Alignment[] { Alignment.LEFT, Alignment.RIGHT };
    }
}
