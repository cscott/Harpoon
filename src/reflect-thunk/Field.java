package java.lang.reflect;

/**
 * The Field class represents a member variable of a class. It also allows
 * dynamic access to a member, via reflection. This works for both
 * static and instance fields. Operations on Field objects know how to
 * do widening conversions, but throw {@link IllegalArgumentException} if
 * a narrowing conversion would be necessary. You can query for information
 * on this Field regardless of location, but get and set access may be limited
 * by Java language access controls. If you can't do it in the compiler, you
 * can't normally do it here either.<p>
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
 * @see Class#getField(String)
 * @see Class#getDeclaredField(String)
 * @see Class#getFields()
 * @see Class#getDeclaredFields()
 * @since 1.1
 * @status updated to 1.4
 */
public final class Field
extends AccessibleObject implements Member
{
    // uninstantiable: all instances are static
    private Field() { }
    // native methods.
    public native Class getDeclaringClass();
    public native String getName();
    public native int getModifiers();
    public native Class getType();
    public native Object get(Object obj) throws IllegalAccessException;
    public native void set(Object obj, Object value) throws IllegalAccessException;
    // helper methods.
    public boolean getBoolean(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Boolean.TYPE))
	    return ((Boolean)get(obj)).booleanValue();
	throw new IllegalArgumentException("getBoolean() on "+this);
    }
    public byte getByte(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Byte.TYPE))
	    return ((Number)get(obj)).byteValue();
	throw new IllegalArgumentException("getByte() on "+this);
    }
    public char getChar(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getChar() on "+this);
    }
    public short getShort(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE))
	    return ((Number)get(obj)).shortValue();
	throw new IllegalArgumentException("getShort() on "+this);
    }
    public int getInt(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE) ||
	    type.equals(Integer.TYPE))
	    return ((Number)get(obj)).intValue();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getInt() on "+this);
    }
    public long getLong(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE) ||
	    type.equals(Integer.TYPE) || type.equals(Long.TYPE))
	    return ((Number)get(obj)).longValue();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getLong() on "+this);
    }
    public float getFloat(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE) ||
	    type.equals(Integer.TYPE) || type.equals(Long.TYPE) ||
	    type.equals(Float.TYPE))
	    return ((Number)get(obj)).floatValue();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getFloat() on "+this);
    }
    public double getDouble(Object obj) throws IllegalAccessException {
	Class type = getType();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	if (!type.equals(Boolean.TYPE))
	    return ((Number)get(obj)).doubleValue();
	throw new IllegalArgumentException("getDouble() on "+this);
    }
    public void setBoolean(Object obj, boolean z) throws IllegalAccessException
    { set(obj, new Boolean(z)); }
    public void setByte(Object obj, byte b) throws IllegalAccessException
    { set(obj, new Byte(b)); }
    public void setChar(Object obj, char c) throws IllegalAccessException
    { set(obj, new Character(c)); }
    public void setShort(Object obj, short s) throws IllegalAccessException
    { set(obj, new Short(s)); }
    public void setInt(Object obj, int i) throws IllegalAccessException
    { set(obj, new Integer(i)); }
    public void setLong(Object obj, long l) throws IllegalAccessException
    { set(obj, new Long(l)); }
    public void setFloat(Object obj, float f) throws IllegalAccessException
    { set(obj, new Float(f)); }
    public void setDouble(Object obj, double d) throws IllegalAccessException
    { set(obj, new Double(d)); }
    // general Object-contract methods.
    public boolean equals(Object o) {
	Field f;
	if (this==o) return true; // common case
	try { f = (Field) o; } catch (ClassCastException e) { return false; }
	return getDeclaringClass().equals(f.getDeclaringClass()) &&
	    getName().equals(f.getName()) &&
	    getType().equals(f.getType());
    }
    public int hashCode() {
	return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }
    public String toString() {
	StringBuffer r = new StringBuffer();
	int m = getModifiers();
	if (m!=0) {
	    r.append(Modifier.toString(m));
	    r.append(' ');
	}
	r.append(getTypeName(getType()));
	r.append(' ');
	r.append(getTypeName(getDeclaringClass()));
	r.append('.');
	r.append(getName());
	return r.toString();
    }
    static String getTypeName(Class hc) {
	if (hc.isArray()) {
	    StringBuffer r = new StringBuffer();
	    Class sup = hc;
	    int i=0;
	    for (; sup.isArray(); sup = sup.getComponentType())
		i++;
	    r.append(sup.getName());
	    for (int j=0; j<i; j++)
		r.append("[]");
	    return r.toString();
	}
	return hc.getName();
    }
}
