package java.lang;

/** A nonstandard, quick-and-dirty console I/O class to support the
 *  "minilib" class libraries without dragging in <code>java.io.*</code>.
 */
public final class Console {
    public static native void print(String s);
    public static native void println();
    public static final void println(String s) {
	print(s); println();
    }
}
