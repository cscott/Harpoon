import java.lang.reflect.*;

public class TestReflect {
    static boolean f0 = false;
    static int f1 = 1;
    static long f2 = 2;
    static float f3 = 3;
    static double f4 = 4;

    public static void main(String[] args) throws Exception { feed_CH();
	Class c = TestReflect.class;
	System.out.println("THIS CLASS IS: "+c.getName());
	System.out.println("DECLARED FIELDS ARE:");
	Field[] field = c.getDeclaredFields();
	printfields2(c, field);
	getvalues(field);
	//
	System.out.println((A) makeone(TestReflect.A.class));
	testmakearray();
    }

    static void feed_CH() {
	/* bogus method to feed information to class hierarchy analysis */
	A a = new A(1);
	A[] aa = new A[0];
	A[][] aaa = new A[0][0];
	byte[][][] bbbb = new byte[0][0][0];
    }
    static void printfields(Class c, Field[] field) throws Exception {
	for (int i=0; i<field.length; i++)
	    System.out.println("  "+field[i].getName());
    }
    static void printfields2(Class c, Field[] field) throws Exception {
	for (int i=0; i<field.length; i++)
	    System.out.println("  "+c.getDeclaredField(field[i].getName())
			       .getName());
    }
    static void getvalues(Field[] field) throws Exception {
	for (int i=0; i<field.length; i++)
	    System.out.println(field[i].getName()+" = "+field[i].get(null));
    }

    static Object makeone(Class c) throws Exception {
	Constructor cons = c.getConstructor(new Class[] { Integer.TYPE });
	return cons.newInstance(new Object[] { new Integer(5) });
    }
    static void testmakearray() throws Exception {
	Class component = TestReflect.A.class;
	A[] aa = (A[]) Array.newInstance(component, 5);
	aa[2] = (A) makeone(component);
	for (int i=0; i<aa.length; i++)
	    System.out.println("aa["+i+"] = "+aa[i]);
	// and again.
	A[][] aaa = (A[][]) Array.newInstance(component, new int[] { 1, 2 });
	aaa[0][1] = (A) makeone(component);
	// test more.
	byte[][][] bbbb = (byte[][][]) Array.newInstance
	    (Byte.TYPE, new int[] { 1, 2, 3 });
	bbbb[0][1][2] = 5;
    }

    private static class A {
	int i;
	public A(int i) { this.i = i; }
	public String toString() { return "A("+i+")"; }
    }
}
