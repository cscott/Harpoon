// MapTest.java, created Sat Nov  3 17:00:03 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Portions copyright (C) 2001 ACUNIA; borrowed from Mauve.
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.HashEnvironment;
import harpoon.Util.LinearMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * The <code>MapTest</code> tests our various <code>Map</code> implementations
 * for correctness.  Large portions borrowed from Mauve.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MapTest.java,v 1.1.2.1 2001-11-04 00:37:54 cananian Exp $
 */
class MapTest {
    private final static boolean DEBUG=false;
    abstract static class Factory {
	abstract Map build();
	abstract Map build(Map m);
    }

    public static void main(String[] args) {
	MapTest mt;
	doit(HashMap.class);
	//doit(TreeMap.class);//sortedmap
	doit(HashEnvironment.class);
	//doit(PersistentEnvironment.class);//sortedmap.
	doit(LinearMap.class);
	doit(GenericInvertibleMap.class);
	doit(GenericInvertibleMultiMap.class);
	doit(GenericMultiMap.class);
    }

    static void doit(Class c) {
	try {
	    final Constructor c1 = c.getConstructor(new Class[0]);
	    final Constructor c2 = c.getConstructor
		(new Class[] { Class.forName("java.util.Map") });
	    Factory f = new Factory() {
		    Map build() {
			try {
			    return (Map) c1.newInstance(new Object[0]);
			} catch (InvocationTargetException ite) {
			    throw (RuntimeException) ite.getTargetException();
			} catch (Throwable t) {
			    throw new RuntimeException(t.toString());
			}
		    }
		    Map build(Map m) {
			try {
			    return (Map) c2.newInstance(new Object[] { m });
			} catch (InvocationTargetException ite) {
			    throw (RuntimeException) ite.getTargetException();
			} catch (Throwable t) {
			    throw new RuntimeException(t.toString());
			}
		    }
		};
	    System.err.println("TESTING "+c);
	    MapTest mt = new MapTest(f);
	    mt.test();
	    if (mt.failed) {
		System.err.println("FAILURES testing "+c);
	    }
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

    // my local state.
    final MapTest th = this;
    final Factory f;
    MapTest(Factory f) {  this.f = f; }

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

    public void test ()   {
       test_Map();
       test_get();
       test_containsKey();
       test_containsValue();
       test_isEmpty();
       test_size();
       test_clear();
       test_put();
       test_putAll();
       test_remove();
       test_entrySet(false/*don't test concurrent modification exceptions*/);
       test_keySet();
       test_values();
       test_behaviour();
  }

  protected Map buildHM() {
      Map hm = f.build();
  	String s;
  	for (int i=0 ; i < 15 ; i++) {
  		s = "a"+i;
  		hm.put(s , s+" value");
  	}
  	hm.put(null,null);
  	return hm;
  }	

/**
* implemented. <br>
*
*/
  public void test_Map(){
    Map hm;
   th.checkPoint("Map()");
    hm = f.build();
   th.checkPoint("Map(java.util.Map)");
    Map hm1 = buildHM();
    debug("AFTER BUILDING: "+hm1);
    hm = f.build(hm1);
    th.check(hm.size() == 16 , "all elements are put, got "+hm.size());
    th.check(hm.get(null) == null , "test key and value pairs -- 1");
    th.check("a1 value".equals(hm.get("a1")) , "test key and value pairs -- 2");
    th.check("a10 value".equals(hm.get("a10")) , "test key and value pairs -- 3");
    th.check("a0 value".equals(hm.get("a0")) , "test key and value pairs -- 4");
    hm = f.build(new Hashtable());
    th.check(hm.size() == 0 , "no elements are put, got "+hm.size());
    try {
   	f.build(null);
   	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException ne) {th.check(true);}
  }

/**
* implemented. <br>
*
*/
  public void test_get(){
    th.checkPoint("get(java.lang.Object)java.lang.Object");
    Map hm = buildHM();
    th.check(hm.get(null) == null , "checking get -- 1");
    th.check(hm.get(this) == null , "checking get -- 2");
    hm.put("a" ,this);
    th.check("a1 value".equals(hm.get("a1")), "checking get -- 3");
    th.check("a11 value".equals(hm.get("a11")), "checking get -- 4");
    th.check( hm.get(new Integer(97)) == null , "checking get -- 5");


  }

/**
* implemented. <br>
*
*/
  public void test_containsKey(){
    th.checkPoint("containsKey(java.lang.Object)boolean");
    Map hm = f.build();
    hm.clear();
    th.check(! hm.containsKey(null) ,"Map is empty");
    hm.put("a" ,this);
    th.check(! hm.containsKey(null) ,"Map does not containsthe key -- 1");
    th.check( hm.containsKey("a") ,"Map does contain the key -- 2");
    hm = buildHM();
    th.check( hm.containsKey(null) ,"Map does contain the key -- 3");
    th.check(! hm.containsKey(this) ,"Map does not contain the key -- 4");

  }

/**
* implemented. <br>
*
*/
  public void test_containsValue(){
    th.checkPoint("containsValue(java.lang.Object)boolean");
    Map hm = f.build();
    hm.clear();
    th.check(! hm.containsValue(null) ,"Map is empty");
    hm.put("a" ,this);
    th.check(! hm.containsValue(null) ,"Map does not containsthe value -- 1");
    th.check(! hm.containsValue("a") ,"Map does  not contain the value -- 2");
    th.check( hm.containsValue(this) ,"Map does contain the value -- 3");
    hm = buildHM();
    th.check( hm.containsValue(null) ,"Map does contain the value -- 4");
    th.check(! hm.containsValue(this) ,"Map does not contain the value -- 5");
    th.check(! hm.containsValue("a1value") ,"Map does  not contain the value -- 6");

  }

/**
* implemented. <br>
*
*/
  public void test_isEmpty(){
    th.checkPoint("isEmpty()boolean");
    Map hm = f.build();
    th.check( hm.isEmpty() ,"Map is empty");
    hm.put("a" ,this);
    th.check(! hm.isEmpty() ,"Map is not empty");

  }

/**
* implemented. <br>
*
*/
  public void test_size(){
    th.checkPoint("size()int");
    Map hm = f.build();
    th.check(hm.size() == 0 ,"Map is empty");
    hm.put("a" ,this);
    th.check(hm.size() == 1 ,"Map has 1 element");
    hm = buildHM();
    th.check(hm.size() == 16 ,"Map has 16 elements");

  }

/**
* implemented. <br>
*
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    Map hm = buildHM();
    hm.clear();
    th.check(hm.size() == 0 ,"Map is cleared -- 1");
    th.check(hm.isEmpty() ,"Map is cleared -- 2");
	
  }

/**
* implemented. <br>
* is tested also in the other parts ...
*/
  public void test_put(){
    th.checkPoint("put(java.lang.Object,java.lang.Object)java.lang.Object");
    Map hm  = f.build();
    th.check( hm.put(null , this ) == null , "check on return value -- 1");
    th.check( hm.get(null) == this , "check on value -- 1");
    th.check( hm.put(null , "a" ) == this , "check on return value -- 2");
    th.check( "a".equals(hm.get(null)) , "check on value -- 2");
    th.check( "a".equals(hm.put(null , "a" )), "check on return value -- 3");
    th.check( "a".equals(hm.get(null)) , "check on value -- 3");
    th.check( hm.size() == 1 , "only one key added");
    th.check( hm.put("a" , null ) == null , "check on return value -- 4");
    th.check( hm.get("a") == null , "check on value -- 4");
    th.check( hm.put("a" , this ) == null , "check on return value -- 5");
    th.check( hm.get("a") == this , "check on value -- 5");
    th.check( hm.size() == 2 , "two keys added");

  }

/**
* implemented. <br>
*
*/
  public void test_putAll(){
    th.checkPoint("putAll(java.util.Map)void");
    Map hm  = f.build();
    hm.putAll(new Hashtable());
    th.check(hm.isEmpty() , "nothing addad");
    hm.putAll(buildHM());
    th.check(hm.size() == 16 , "checking if all enough elements are added -- 1");
    th.check(hm.equals(buildHM()) , "check on all elements -- 1");
    hm.put(null ,this);
    hm.putAll(buildHM());
    th.check(hm.size() == 16 , "checking if all enough elements are added -- 2");
    th.check(hm.equals(buildHM()) , "check on all elements -- 2");
    try {
    	hm.putAll(null);
    	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe) { th.check(true); }	
  }

/**
* implemented. <br>
*
*/
  public void test_remove(){
    th.checkPoint("remove(java.lang.Object)java.lang.Object");
    Map hm  = buildHM();
    th.check(hm.remove(null) == null , "checking return value -- 1");
    th.check(hm.remove(null) == null , "checking return value -- 2");
    th.check(!hm.containsKey(null) , "checking removed key -- 1");
    th.check(!hm.containsValue(null) , "checking removed value -- 1");
    for (int i = 0 ; i < 15 ; i++) {
    	th.check( ("a"+i+" value").equals(hm.remove("a"+i)), " removing a"+i);
    }
    th.check(hm.isEmpty() , "checking if al is gone");
  }

/**
* implemented. <br>
* uses AbstractSet --> check only the overwritten methods ... !
* iterator and size
* fail-fast iterator !
* add not supported !
* check the Map.Entry Objects ...
*/
  public void test_entrySet(boolean test_cme){
    th.checkPoint("entrySet()java.util.Set");
    Map hm  = buildHM();
    Set s = hm.entrySet();
    Iterator it= s.iterator();
    Map.Entry me=null;
    it.next();
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }
    th.check( s.size() == 16 );
    hm.remove("a12");
    th.check( s.size() == 15 );
    th.check(it.hasNext());
    if (test_cme) {
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 1");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
    try {
    	it.remove();
    	th.fail("should throw a ConcurrentModificationException -- 2");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
//    th.debug(hm.debug());
    }
    it= s.iterator();
    try {
    	me = (Map.Entry)it.next();
//    	Thread.sleep(600L);
    	if (me.getKey()==null) me = (Map.Entry)it.next();
    	th.check( me.hashCode() , (me.getValue().hashCode() ^ me.getKey().hashCode()),"verifying hashCode");
    	th.check(! me.equals(it.next()));
    	
    	}
    catch(Exception e) { th.fail("got unwanted exception ,got "+e);
    	th.debug("got ME key = "+me+" and value = "+me.getKey());}

    /* setValue on the entry set contents is okay!
    try {
    	//th.debug("got ME key = "+me.getKey()+" and value = "+me.getValue());
    	me.setValue(this);
    	th.fail("should throw an UnsupportedOperationException");
    	}
    catch(UnsupportedOperationException uoe) { th.check(true);}
    */
    it= s.iterator();
    Vector v = new Vector();
    Object ob;
    v.addAll(s);
    try {
    while (it.hasNext()) {
    	ob = it.next();
    	it.remove();
     	if (!v.remove(ob))
        th.debug("Object "+ob+" not in the Vector");
     }
    } catch (UnsupportedOperationException uoe) {
	th.fail("EntrySet mutation not supported");
	v.clear(); hm.clear();
    }
     th.check( v.isEmpty() , "all elements gone from the vector");
//     for (int k=0 ; k < v.size() ; k++ ) { th.debug("got "+v.get(k)+" as element "+k); }
     th.check( hm.isEmpty() , "all elements removed from the Map");
    it= s.iterator();
    hm.put(null,"sdf");
    if (test_cme) {
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 3");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
    it= s.iterator();
    hm.clear();
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 4");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
    }
  }

/**
* implemented. <br>
* uses AbstractSet --> check only the overwritten methods ... !
* iterator and size
* fail-fast iterator !
* add not supported !
*/
  public void test_keySet(){
    th.checkPoint("keySet()java.util.Set");
    Map hm = buildHM();
    th.check( hm.size() == 16 , "checking map size(), got "+hm.size());
    Set s=null;
    Object [] o;
    Iterator it;
    try {
        s = hm.keySet();
        th.check( s != null ,"s != null");
        th.check(s.size() == 16 ,"checking size keyset, got "+s.size());
        o = s.toArray();
        th.check( o != null ,"o != null");
        th.check( o.length == 16 ,"checking length, got "+o.length);
//        for (int i = 0 ; i < o.length ; i++ ){ th.debug("element "+i+" is "+o[i]); }
	it = s.iterator();
	Vector v = new Vector();
	Object ob;
	v.addAll(s);
	while ( it.hasNext() ) {
        	ob = it.next();
        	it.remove();
        	if (!v.remove(ob))
        	th.debug("Object "+ob+" not in the Vector");
        }
        th.check( v.isEmpty() , "all elements gone from the vector");
        th.check( hm.isEmpty() , "all elements removed from the Map");
    }
    catch (Exception e) { th.fail("got bad Exception -- got "+e); }
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }

  }

/**
* implemented. <br>
* uses AbstractCollection --> check only the overwritten methods ... !
* iterator and size
* fail-fast iterator !
* add not supported !
*/
  public void test_values(){
    th.checkPoint("values()java.util.Collection");
    Map hm = buildHM();
    th.check( hm.size() == 16 , "checking map size(), got "+hm.size());
    Collection s=null;
    Object [] o;
    Iterator it;
    try {
        s = hm.values();
        th.check( s != null ,"s != null");
        th.check(s.size() == 16 ,"checking size keyset, got "+s.size());
        o = s.toArray();
        th.check( o != null ,"o != null");
        th.check( o.length == 16 ,"checking length, got "+o.length);
//        for (int i = 0 ; i < o.length ; i++ ){ th.debug("element "+i+" is "+o[i]); }
	it = s.iterator();
	Vector v = new Vector();
	Object ob;
	v.addAll(s);
	while ( it.hasNext() ) {
        	ob = it.next();
        	it.remove();
        	if (!v.remove(ob))
        	th.debug("Object "+ob+" not in the Vector");
        }
        th.check( v.isEmpty() , "all elements gone from the vector");
        th.check( hm.isEmpty() , "all elements removed from the Map");
    }
    catch (Exception e) { th.fail("got bad Exception -- got "+e); }
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }


  }

/**
* the goal of this test is to see how the hashtable behaves if we do a lot put's and removes. <br>
* we perform this test for different loadFactors and a low initialsize <br>
* we try to make it difficult for the table by using objects with same hashcode
*/
  private final String st ="a";
  private final Byte b =new Byte((byte)97);
  private final Short sh=new Short((short)97);
  private final Integer i = new Integer(97);
  private final Long l = new Long(97L);
  private int sqnce = 1;

  public void test_behaviour(){
    th.checkPoint("behaviour testing");
//    do_behaviourtest(0.2f);
    do_behaviourtest(0.70f);
    do_behaviourtest(0.75f);
    do_behaviourtest(0.95f);
    do_behaviourtest(1.0f);

    }
  protected void sleep(int time){
  	try { Thread.sleep(time); }
  	catch (Exception e) {}	
  }

  protected void check_presence(Map h){
    th.check( h.get(st) != null, "checking presence st -- sequence "+sqnce);
    th.check( h.get(sh) != null, "checking presence sh -- sequence "+sqnce);
    th.check( h.get(i) != null, "checking presence i -- sequence "+sqnce);
    th.check( h.get(b) != null, "checking presence b -- sequence "+sqnce);
    th.check( h.get(l) != null, "checking presence l -- sequence "+sqnce);
    sqnce++;
  }

  protected void do_behaviourtest(float loadFactor) {

    th.checkPoint("behaviour testing with loadFactor "+loadFactor);
    Map h = f.build();
    int j=0;
    Float f;
    h.put(st,"a"); h.put(b,"byte"); h.put(sh,"short"); h.put(i,"int"); h.put(l,"long");
    check_presence(h);
    sqnce = 1;
    for ( ; j < 100 ; j++ )
    {   f = new Float((float)j);
        h.put(f,f);
       // sleep(5);
    }
    th.check(h.size() == 105,"size checking -- 1 got: "+h.size());
    check_presence(h);
//    sleep(500);
    for ( ; j < 200 ; j++ )
    {   f = new Float((float)j);
        h.put(f,f);
      //  sleep(10);
    }
    th.check(h.size() == 205,"size checking -- 2 got: "+h.size());
    check_presence(h);
//    sleep(50);

    for ( ; j < 300 ; j++ )
    {   f = new Float((float)j);
        h.put(f,f);
      //  sleep(10);
    }
    th.check(h.size() == 305,"size checking -- 3 got: "+h.size());
    check_presence(h);
//    sleep(50);
// replacing values -- checking if we get a non-zero value
    th.check("a".equals(h.put(st,"na")), "replacing values -- 1 - st");
    th.check("byte".equals(h.put(b,"nbyte")), "replacing values -- 2 - b");
    th.check("short".equals(h.put(sh,"nshort")), "replacing values -- 3 -sh");
    th.check("int".equals(h.put(i,"nint"))  , "replacing values -- 4 -i");
    th.check("long".equals(h.put(l,"nlong")), "replacing values -- 5 -l");


    for ( ; j > 199 ; j-- )
    {   f = new Float((float)j);
        h.remove(f);
      //  sleep(10);
    }
//    sleep(150);
    th.check(h.size() == 205,"size checking -- 4 got: "+h.size());
    check_presence(h);
    for ( ; j > 99 ; j-- )
    {   f = new Float((float)j);
        h.remove(f);
      //  sleep(5);
    }
    th.check(h.size() == 105,"size checking -- 5 got: "+h.size());
    check_presence(h);
   // sleep(1500);
    for ( ; j > -1 ; j-- )
    {   f = new Float((float)j);
        h.remove(f);
     //   sleep(5);
    }
    th.check(h.size() == 5  ,"size checking -- 6 got: "+h.size());

    th.debug(h.toString());
    check_presence(h);
   // sleep(500);

    }

}
