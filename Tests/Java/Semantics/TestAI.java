// TestAI.java, created Fri Sep 21 14:13:45 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

/**
 * <code>TestAI</code> tests interface array instanceof.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TestAI.java,v 1.1 2001-09-21 20:03:52 cananian Exp $
 */
public class TestAI {
    static Object ca = new C[1];
    static Object caa = new C[2][3];
    private static void check(String str, boolean b, boolean correct) {
	if (b==correct) System.out.println("PASSED: "+str);
	else System.out.println("FAILED: "+str);
    }
    public static void checkTrue(String str, boolean b){ check(str, b, true); }
    public static void checkFalse(String str, boolean b){check(str, b, false);}
    private static void checkComponent(String str,
				       Object[] arr, Object obj,
				       boolean shouldpass) {
	String okString = shouldpass ? "PASSED: " : "FAILED: ";
	String noString = shouldpass ? "FAILED: " : "PASSED: ";
	try {
	    arr[0] = obj;
	    System.out.println(okString+str);
	} catch (ArrayStoreException ase) {
	    System.out.println(noString+str);
	}
    }
    public static void checkGoodComponent(String str,
					  Object[] arr, Object obj) {
	checkComponent(str, arr, obj, true);
    }
    public static void checkBadComponent(String str,
					  Object[] arr, Object obj) {
	checkComponent(str, arr, obj, false);
    }

    public static void main(String[] args) {
	/* instanceof tests. */
	checkTrue("C[] instanceof A[]", (ca instanceof A[]));
	checkTrue("C[] instanceof B[]", (ca instanceof B[]));
	checkTrue("C[] instanceof C[]", (ca instanceof C[]));
	checkTrue("C[] instanceof Object[]", (ca instanceof Object[]));
	checkTrue("C[] instanceof Object", (ca instanceof Object));
	checkTrue("C[] instanceof Cloneable", (ca instanceof Cloneable));

	checkFalse("A[] instanceof C[]", (new A[1] instanceof C[]));
	checkFalse("B[] instanceof C[]", (new B[1] instanceof C[]));
	checkFalse("Object[] instanceof C[]", (new Object[1] instanceof C[]));

	checkTrue("C[][] instanceof A[][]", (caa instanceof A[][]));
	checkTrue("C[][] instanceof B[][]", (caa instanceof B[][]));
	checkTrue("C[][] instanceof C[][]", (caa instanceof C[][]));
	checkTrue("C[][] instanceof Object[][]", (caa instanceof Object[][]));
	checkTrue("C[][] instanceof Object[]", (caa instanceof Object[]));
	checkTrue("C[][] instanceof Object", (caa instanceof Object));
	checkTrue("C[][] instanceof Cloneable", (caa instanceof Cloneable));

	checkFalse("C[][] instanceof A[]", (caa instanceof A[]));
	checkFalse("C[][] instanceof B[]", (caa instanceof B[]));
	checkFalse("C[][] instanceof C[]", (caa instanceof C[]));

	/* componentof tests. */
	checkGoodComponent("CC componentof A[]", new A[1], new CC());
	checkGoodComponent("CC componentof B[]", new B[1], new CC());
	checkGoodComponent("CC componentof C[]", new C[1], new CC());
	checkGoodComponent("CC componentof Object[]", new Object[1], new CC());
	checkBadComponent("AA componentof C[]", new C[1], new AA());
	checkBadComponent("BB componentof C[]", new C[1], new BB());
	checkBadComponent("Object componentof C[]", new C[1], new Object());

	checkGoodComponent("C[] componentof A[][]", new A[1][1], new C[1]);
	checkGoodComponent("C[] componentof B[][]", new B[1][1], new C[1]);
	checkGoodComponent("C[] componentof C[][]", new C[1][1], new C[1]);
	checkGoodComponent("C[] componentof Object[][]", new Object[1][1], new C[1]);
	checkGoodComponent("C[] componentof Object[]", new Object[1], new C[1]);
	checkBadComponent("C[] componentof A[]", new A[1], new C[1]);
	checkBadComponent("C[] componentof B[]", new B[1], new C[1]);
	checkBadComponent("C[] componentof C[]", new C[1], new C[1]);
    }


    private static interface A { }
    private static interface B { }
    private static interface C extends A, B { }

    private static class AA implements A { }
    private static class BB implements B { }
    private static class CC implements C { }
}
