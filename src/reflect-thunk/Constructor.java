package java.lang.reflect;

/**
 * The Constructor class represents a constructor of a class. It also allows
 * dynamic creation of an object, via reflection. Invocation on Constructor
 * objects knows how to do widening conversions, but throws
 * {@link IllegalArgumentException} if a narrowing conversion would be
 * necessary. You can query for information on this Constructor regardless
 * of location, but construction access may be limited by Java language
 * access controls. If you can't do it in the compiler, you can't normally
 * do it here either.<p>
 *
 * <B>Note:</B> This class returns and accepts types as Classes, even
 * primitive types; there are Class types defined that represent each
 * different primitive type.  They are <code>java.lang.Boolean.TYPE,
 * java.lang.Byte.TYPE,</code>, also available as <code>boolean.class,
 * byte.class</code>, etc.  These are not to be confused with the
 * classes <code>java.lang.Boolean, java.lang.Byte</code>, etc., which are
 * real classes.<p>
 *
 * Also note that this is not a serializable class.  It is entirely feasible
 * to make it serializable using the Externalizable interface, but this is
 * on Sun, not me.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author John Keiser
 * @author Eric Blake <ebb9@email.byu.edu>
 * @see Member
 * @see Class
 * @see java.lang.Class#getConstructor(Object[])
 * @see java.lang.Class#getDeclaredConstructor(Object[])
 * @see java.lang.Class#getConstructors()
 * @see java.lang.Class#getDeclaredConstructors()
 * @since 1.1
 * @status updated to 1.4
 */
public final class Constructor
extends AccessibleObject implements Member
{
    // uninstantiable: all instances are static
    private Constructor() { }
    // native methods
    public native Class getDeclaringClass();
    public native String getName();
    public native int getModifiers();
    public native Class[] getParameterTypes();
    public native Class[] getExceptionTypes();
    public native Object newInstance(Object[] oa)
	throws InstantiationException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException;
    // object-contract methods.
    public boolean equals(Object o) {
	Method m;
	if (this==o) return true; // common case.
	try { m = (Method) o; } catch (ClassCastException e) { return false; }
	if (!getDeclaringClass().equals(m.getDeclaringClass())) return false;
	Class[] mypt=getParameterTypes(), yourpt=m.getParameterTypes();
	if (mypt.length!=yourpt.length) return false;
	for (int i=0; i<mypt.length; i++)
	    if (!mypt[i].equals(yourpt[i])) return false;
	return true;
    }
    public int hashCode() {
	return getDeclaringClass().getName().hashCode();
    }
    public String toString() {
	StringBuffer r = new StringBuffer();
	int m = getModifiers();
	if (m!=0) {
	    r.append(Modifier.toString(m));
	    r.append(' ');
	}
	r.append(Field.getTypeName(getDeclaringClass()));
	r.append('(');
	Class hcp[] = getParameterTypes();
	for (int i=0; i<hcp.length; i++) {
	    r.append(Field.getTypeName(hcp[i]));
	    if (i < hcp.length-1)
		r.append(',');
	}
	r.append(')');
	Class ecp[] = getExceptionTypes();
	if (ecp.length > 0) {
	    r.append(" throws ");
	    for (int i=0; i<ecp.length; i++) {
		r.append(ecp[i].getName());// can't be primitive or array type.
		if (i < ecp.length-1)
		    r.append(',');
	    }
	}
	return r.toString();
    }
}
