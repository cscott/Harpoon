package java.lang.reflect;

public class Constructor implements Member {
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
