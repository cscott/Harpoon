// TestSet.java, created Wed Nov  7 17:44:45 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Portions copyright (C) 2001 ACUNIA; borrowed from Mauve.
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Collections.*;
import harpoon.Util.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * The <code>TestSet</code> class tests our various <code>Set</code>
 * implementations for correctness.  Large portions borrowed from Mauve.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TestSet.java,v 1.1.2.1 2001-11-07 23:25:01 cananian Exp $
 */
public class TestSet {
    private final static boolean DEBUG=false;
    abstract static class Factory {
	abstract Set build();
	abstract Set build(Collection m);
    }

    public static void main(String[] args) {
	TestSet mt;
	doit(HashSet.class);
	//doit(TreeSet.class);//sortedmap
	//doit(ArraySet.class);//immutable.
	//doit(PersistentSet.class);//immutable
	/** these don't handle 'null's. ******
	doit(UniqueVector.class);
	doit(UniqueStack.class);
	doit(WorkSet.class);
	**************************************/
	doit(LinearSet.class);
	// SetFactories.
	doit(new AggregateSetFactory(), "AggregateSetFactory");
	//doit(new BitSetFactory(), "BitSetFactory");//requires universe.
	doit(Factories.synchronizedSetFactory(Factories.hashSetFactory),
	     "synchronized HashSet");
    }

    static void doit(Class c) {
	try {
	    final Constructor c1 = c.getConstructor(new Class[0]);
	    final Constructor c2 = c.getConstructor
		(new Class[] { Class.forName("java.util.Collection") });
	    Factory f = new Factory() {
		    Set build() {
			try {
			    return (Set) c1.newInstance(new Object[0]);
			} catch (InvocationTargetException ite) {
			    throw (RuntimeException) ite.getTargetException();
			} catch (Throwable t) {
			    throw new RuntimeException(t.toString());
			}
		    }
		    Set build(Collection m) {
			try {
			    return (Set) c2.newInstance(new Object[] { m });
			} catch (InvocationTargetException ite) {
			    throw (RuntimeException) ite.getTargetException();
			} catch (Throwable t) {
			    throw new RuntimeException(t.toString());
			}
		    }
		};
	    doit(f, c.toString());
	} catch (RuntimeException re) {
	    System.err.println("SKIPPING: "+re);
	    throw re;
	} catch (Error e) {
	    System.err.println("SKIPPING: "+e);
	    throw e;
	} catch (Throwable t) {
	    System.err.println("SKIPPING: "+t);
	}
    }
    static void doit(final SetFactory mf, String str) {
	doit(new Factory() {
		Set build() { return mf.makeSet(); }
		Set build(Collection m) { return mf.makeSet(m); }
	    }, str);
    }
    static void doit(Factory f, String str) {
	System.err.println("TESTING "+str);
	TestSet mt = new TestSet(f);
	mt.test();
	if (mt.failed) {
	    System.err.println("FAILURES testing "+str);
	}
    }

    // my local state.
    final TestSet th = this;
    final Factory f;
    TestSet(Factory f) {  this.f = f; }

    // methods of test harness.
    boolean failed = false;
    void check(boolean cond) {
	if (!cond) {
	    System.err.println("FAIL("+section+"): "+last_check);
	    failed = true;
	}
    }
    void debug(String s) { if (DEBUG) System.err.println(s); }
    void checkPoint(String name) {
	section = name; 
    }
    void checkPoint2(String name) {
	last_check = name;
    }
    String section = "NONE";
    String last_check = "NONE";
    // checking methods.
    // start code from gnu.testlet.TestHarness

  public void check (Object result, Object expected)
    {
      boolean ok = (result == null
		    ? expected == null
		    : result.equals(expected));
      check (ok);
      // This debug message may be misleading, depending on whether
      // string conversion produces same results for unequal objects.
      if (! ok)
	debug ("got " + result + " but expected " + expected);
    }
  public void check (double result, double expected)
    {
      // This triple check overcomes the fact that == does not
      // compare NaNs, and cannot tell between 0.0 and -0.0;
      // and all without relying on java.lang.Double (which may
      // itself be buggy - else why would we be testing it? ;)
      // For 0, we switch to infinities, and for NaN, we rely
      // on the identity in JLS 15.21.1 that NaN != NaN is true.
      boolean ok = (result == expected
		    ? (result != 0) || (1/result == 1/expected)
		    : (result != result) && (expected != expected));
      check (ok);
      if (! ok)
	// If Double.toString() is buggy, this debug statement may
	// accidentally show the same string for two different doubles!
	debug ("got " + result + " but expected " + expected);
    }
  public void check (long result, long expected)
    {
      boolean ok = (result == expected);
      check (ok);
      if (! ok)
	debug ("got " + result + " but expected " + expected);
    }
  public void check (int result, int expected)
    {
      boolean ok = (result == expected);
      check (ok);
      if (! ok)
	debug ("got " + result + " but expected " + expected);
    }

  // These methods are like the above, but checkpoint first.
  public void check (boolean result, String name)
    {
      checkPoint2 (name);
      check (result);
    }
  public void check (Object result, Object expected, String name)
    {
      checkPoint2 (name);
      check (result, expected);
    }
  public void check (int result, int expected, String name)
    {
      checkPoint2 (name);
      check (result, expected);
    }
  public void check (long result, long expected, String name)
    {
      checkPoint2 (name);
      check (result, expected);
    }
  public void check (double result, double expected, String name)
    {
      checkPoint2 (name);
      check (result, expected);
    }
  public void fail (String name)
    {
      checkPoint2 (name);
      check (false);
    }
    // end bit from gnu.testlet.TestHarness

    // from AcuniaAbstractSetTest.java:
    public void test ()
    {
       test_equals();
       test_hashCode();
     }


/**
* implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    Set xas1 = f.build();
    Set xas2 = f.build();
    th.check( xas1.equals(xas2) , "checking equality -- 1");
    th.check(!xas1.equals(null) , "checking equality -- 2");
    th.check(!xas1.equals(this) , "checking equality -- 3");
    th.check( xas1.equals(xas1) , "checking equality -- 4");
    xas1.add(null);
    xas1.add("a");
    xas2.add("b");
    xas2.add(null);
    xas2.add("a");
    xas1.add("b");
    th.check( xas1.equals(xas2) , "checking equality -- 5");
    th.check( xas1.equals(xas1) , "checking equality -- 6");


  }
/**
* implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    Set xas = f.build();
    th.check(xas.hashCode() == 0 ,"checking hc-algorithm -- 1");
    xas.add(null);
    th.check(xas.hashCode() == 0 ,"checking hc-algorithm -- 2");
    xas.add("a");
    int hash = "a".hashCode();
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 3");
    hash += "b".hashCode();
    xas.add("b");
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 4");
    hash += "c".hashCode();
    xas.add("c");
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 5");
    hash += "d".hashCode();
    xas.add("d");
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 6");




  }
}
