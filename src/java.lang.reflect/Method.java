package java.lang.reflect;

public class Method implements Member {
    // native methods.
    public native Class getDeclaringClass();
    public native String getName();
    public native int getModifiers();
    public native Class getReturnType();
    public native Class[] getParameterTypes();
    public native Class[] getExceptionTypes();
    public native Object invoke(Object o, Object[] oa)
	throws IllegalAccessException, IllegalArgumentException,
	InvocationTargetException;
    // object-contract methods.
    public boolean equals(Object o) {
	Method m;
	try { m = (Method) o; } catch (ClassCastException e) { return false; }
	if (!getDeclaringClass().equals(m.getDeclaringClass())) return false;
	if (!getName().equals(m.getName())) return false;
	if (!getReturnType().equals(m.getReturnType())) return false;
	Class[] mypt=getParameterTypes(), yourpt=m.getParameterTypes();
	if (mypt.length!=yourpt.length) return false;
	for (int i=0; i<mypt.length; i++)
	    if (!mypt[i].equals(yourpt[i])) return false;
	return true;
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
	r.append(Field.getTypeName(getReturnType()));
	r.append(' ');
	r.append(Field.getTypeName(getDeclaringClass()));
	r.append('.');
	r.append(getName());
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
