package java.lang.reflect;

public class Field implements Member {
    // native methods.
    public native Class getDeclaringClass();
    public native String getName();
    public native int getModifiers();
    public native Class getType();
    public native Object get(Object obj);
    public native void set(Object obj, Object value);
    // helper methods.
    public boolean getBoolean(Object obj) {
	Class type = getType();
	if (type.equals(Boolean.TYPE))
	    return ((Boolean)get(obj)).booleanValue();
	throw new IllegalArgumentException("getBoolean() on "+this);
    }
    public byte getByte(Object obj) {
	Class type = getType();
	if (type.equals(Byte.TYPE))
	    return ((Number)get(obj)).byteValue();
	throw new IllegalArgumentException("getByte() on "+this);
    }
    public char getChar(Object obj) {
	Class type = getType();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getChar() on "+this);
    }
    public short getShort(Object obj) {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE))
	    return ((Number)get(obj)).shortValue();
	throw new IllegalArgumentException("getShort() on "+this);
    }
    public int getInt(Object obj) {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE) ||
	    type.equals(Integer.TYPE))
	    return ((Number)get(obj)).intValue();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getInt() on "+this);
    }
    public long getLong(Object obj) {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE) ||
	    type.equals(Integer.TYPE) || type.equals(Long.TYPE))
	    return ((Number)get(obj)).longValue();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getLong() on "+this);
    }
    public float getFloat(Object obj) {
	Class type = getType();
	if (type.equals(Byte.TYPE) || type.equals(Short.TYPE) ||
	    type.equals(Integer.TYPE) || type.equals(Long.TYPE) ||
	    type.equals(Float.TYPE))
	    return ((Number)get(obj)).floatValue();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	throw new IllegalArgumentException("getFloat() on "+this);
    }
    public double getDouble(Object obj) {
	Class type = getType();
	if (type.equals(Character.TYPE))
	    return ((Character)get(obj)).charValue();
	if (!type.equals(Boolean.TYPE))
	    return ((Number)get(obj)).doubleValue();
	throw new IllegalArgumentException("getDouble() on "+this);
    }
    public void setBoolean(Object obj, boolean z) { set(obj, new Boolean(z)); }
    public void setByte(Object obj, byte b) { set(obj, new Byte(b)); }
    public void setChar(Object obj, char c) { set(obj, new Character(c)); }
    public void setShort(Object obj, short s) { set(obj, new Short(s)); }
    public void setInt(Object obj, int i) { set(obj, new Integer(i)); }
    public void setLong(Object obj, long l) { set(obj, new Long(l)); }
    public void setFloat(Object obj, float f) { set(obj, new Float(f)); }
    public void setDouble(Object obj, double d) { set(obj, new Double(d)); }
    // general Object-contract methods.
    public boolean equals(Object o) {
	Field f;
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
