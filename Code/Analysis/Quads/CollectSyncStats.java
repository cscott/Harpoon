// CollectSyncStats.java, created Thu Jul 13  2:18:28 2000 by jwhaley
// Copyright (C) 2000 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.Quads;

import java.util.Set;
import java.util.AbstractSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.NoSuchElementException;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

import java.io.FileDescriptor;
import java.util.Hashtable;
import java.net.InetAddress;
import java.lang.reflect.Member;

/**
 * <code>CollectSyncStats</code> is used at run time to collect information
 * about synchronization operations.  (<code>InstrumentSyncOps</code>
 * instruments code to add calls to methods in this class.)
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: CollectSyncStats.java,v 1.2 2002-02-25 20:59:22 cananian Exp $
 */

public abstract class CollectSyncStats {

    static boolean hasdumped = false;
    
    public static void main(String[] args) throws Throwable {
	String classname = args[0];
	Class c = Class.forName(classname);
	if (c == null) {
	    System.err.println("Error loading class "+classname);
	    return;
	}
	//Class[] argdesc = new Class[] { java.lang.reflect.Array.newInstance(String.class, 0).getClass() };
	Class[] argdesc = new Class[] { Class.forName("[Ljava.lang.String;") };
	java.lang.reflect.Method m = c.getMethod("main", argdesc);
	if (m == null) {
	    System.err.println("No main method in class "+c);
	    return;
	}
	String[] arg = new String[args.length-1];
	System.arraycopy(args, 1, arg, 0, arg.length);
	try {
	    System.runFinalizersOnExit(true);
	    CollectSyncStats.init(2, 2);
	    System.setSecurityManager(new SecurityManager() {
		public void checkExit(int status) { throw new SecurityException(); }

		public void checkCreateClassLoader() { } 
		public void checkAccess(Thread g) { }
		public void checkAccess(ThreadGroup g) { }
		public void checkExec(String cmd) { }
		public void checkLink(String lib) { }
		public void checkRead(FileDescriptor fd) { }
		public void checkRead(String file) { }
		public void checkRead(String file, Object context) { }
		public void checkWrite(FileDescriptor fd) { }
		public void checkWrite(String file) { }
		public void checkDelete(String file) { }
		public void checkConnect(String host, int port) { }
		public void checkConnect(String host, int port, Object context) { }
		public void checkListen(int port) { }
		public void checkAccept(String host, int port) { }
		public void checkMulticast(InetAddress maddr) { }
		public void checkMulticast(InetAddress maddr, byte ttl) { }
		public void checkPropertiesAccess() { }
		public void checkPropertyAccess(String key) { }
		public void checkPropertyAccess(String key, String def) { }
		public boolean checkTopLevelWindow(Object window) { return true; }
		public void checkPrintJobAccess() { }
		public void checkSystemClipboardAccess() { }
		public void checkAwtEventQueueAccess() { }
		public void checkPackageAccess(String pkg) { }
		public void checkPackageDefinition(String pkg) { }
		public void checkSetFactory() { }
		public void checkMemberAccess(Class clazz, int which) { }
		public void checkSecurityAccess(String provider) { }
	        });
	    enabled = true;
	    m.invoke(null, new Object[] { arg });
	    enabled = false;
	} catch (java.lang.reflect.InvocationTargetException x) {
	    throw x.getTargetException();
	} finally {
	    dump();
	}
    }
    
    /** The <code>WeakIdentityHashMap</code> is used to keep track of objects
     * that have been synchronized on. It uses weak keys and the
     * identityHashCode function.
     */
    static class WeakIdentityHashMap extends AbstractMap implements Map {

	static private class WeakKey extends WeakReference {
	    private int hash;
	    private WeakKey(Object k) {
	        super(k);
	        hash = System.identityHashCode(k);
	    }
	    private static WeakKey create(Object k) {
	        if (k == null) return null;
	        else return new WeakKey(k);
	    }
	    private WeakKey(Object k, ReferenceQueue q) {
	        super(k, q);
	        hash = System.identityHashCode(k);
	    }
	    private static WeakKey create(Object k, ReferenceQueue q) {
	        if (k == null) return null;
	        else return new WeakKey(k, q);
	    }
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof WeakKey)) return false;
	        Object t = this.get();
	        Object u = ((WeakKey)o).get();
	        if ((t == null) || (u == null)) return false;
	        return (t == u);
	    }
	    public int hashCode() {
	        return hash;
	    }
	}
	private Map hash;
	private ReferenceQueue queue = new ReferenceQueue();
	private void processQueue() {
	    WeakKey wk;
	    while ((wk = (WeakKey)queue.poll()) != null) {
	        hash.remove(wk);
	    }
	}
	public WeakIdentityHashMap(int initialCapacity, float loadFactor) {
	    hash = new HashMap(initialCapacity, loadFactor);
	}
	public WeakIdentityHashMap(int initialCapacity) {
	    hash = new HashMap(initialCapacity);
	}
	public WeakIdentityHashMap() {
	    hash = new HashMap();
	}
	public int size() {
	    return entrySet().size();
	}
	public boolean isEmpty() {
	    return entrySet().isEmpty();
	}
	public boolean containsKey(Object key) {
	    return hash.containsKey(WeakKey.create(key));
	}
	public Object get(Object key) {
	    return hash.get(WeakKey.create(key));
	}
	public Object put(Object key, Object value) {
	    processQueue();
	    return hash.put(WeakKey.create(key, queue), value);
	}
	public Object remove(Object key) {
	    processQueue();
	    return hash.remove(WeakKey.create(key));
	}
	public void clear() {
	    processQueue();
	    hash.clear();
	}
	static private class Entry implements Map.Entry {
	    private Map.Entry ent;
	    private Object key;
	    Entry(Map.Entry ent, Object key) {
	        this.ent = ent;
	        this.key = key;
	    }
	    public Object getKey() {
	        return key;
	    }
	    public Object getValue() {
	        return ent.getValue();
	    }
	    public Object setValue(Object value) {
	        return ent.setValue(value);
	    }
	    private static boolean valEquals(Object o1, Object o2) {
		return o1 == o2;
	    }
	    public boolean equals(Object o) {
	        if (! (o instanceof Map.Entry)) return false;
	        Map.Entry e = (Map.Entry)o;
	        return (valEquals(key, e.getKey())
	    	    && valEquals(getValue(), e.getValue()));
	    }
	    public int hashCode() {
	        Object v;
	        return (((key == null) ? 0 : System.identityHashCode(key))
	    	    ^ (((v = getValue()) == null) ? 0 : v.hashCode()));
	    }
	}
	private class EntrySet extends AbstractSet {
	    Set hashEntrySet = hash.entrySet();
	    public Iterator iterator() {
		return new Iterator() {
		    Iterator hashIterator = hashEntrySet.iterator();
		    Entry next = null;
		    public boolean hasNext() {
			while (hashIterator.hasNext()) {
			    Map.Entry ent = (Map.Entry)hashIterator.next();
			    WeakKey wk = (WeakKey)ent.getKey();
			    Object k = null;
			    if ((wk != null) && ((k = wk.get()) == null)) {
				/* Weak key has been cleared by GC */
				continue;
			    }
			    next = new Entry(ent, k);
			    return true;
			}
			return false;
		    }
		    public Object next() {
			if ((next == null) && !hasNext())
			    throw new NoSuchElementException();
			Entry e = next;
			next = null;
			return e;
		    }
		    public void remove() {
			hashIterator.remove();
		    }
		};
	    }
	    public boolean isEmpty() {
		return !(iterator().hasNext());
	    }
	    public int size() {
		int j = 0;
		for (Iterator i = iterator(); i.hasNext(); i.next()) j++;
		return j;
	    }
	    public boolean remove(Object o) {
		processQueue();
		if (!(o instanceof Map.Entry)) return false;
		Map.Entry e = (Map.Entry)o;
		Object ev = e.getValue();
		WeakKey wk = WeakKey.create(e.getKey());
		Object hv = hash.get(wk);
		if ((hv == null)
		    ? ((ev == null) && hash.containsKey(wk)) : (hv == ev)) {
		    hash.remove(wk);
		    return true;
		}
		return false;
	    }
	    public int hashCode() {
		int h = 0;
		for (Iterator i = hashEntrySet.iterator(); i.hasNext();) {
		    Map.Entry ent = (Map.Entry)i.next();
		    WeakKey wk = (WeakKey)ent.getKey();
		    Object v;
		    if (wk == null) continue;
		    h += (wk.hashCode()
			  ^ (((v = ent.getValue()) == null) ? 0 : System.identityHashCode(v)));
		}
		return h;
	    }
	}
	private Set entrySet = null;
	public Set entrySet() {
	    if (entrySet == null) entrySet = new EntrySet();
	    return entrySet;
	}
    }
    
    /** Dummy object that we use to insure atomic updates to the counters.
     */
    static Object locking = new Object();


    /** All created objects are registered here, associated with their creation site.
     */
    static WeakIdentityHashMap objmap;
    
    /** MUST call this function before enabling stats collection.
     */
    public static void init(int nNewTypes, int nLockTypes) {
	objmap = new WeakIdentityHashMap();
	monitorEnterMap = new HashMap();
	monitorExitMap = new HashMap();
	newcount = new int[nNewTypes];
	totalnewcount = 0;
	lockcount = new int[nLockTypes];
    }
    
    /** Whether stats collection is currently enabled.
     */
    public static volatile boolean enabled;
    
    public static int[] newcount;
    public static int totalnewcount;
    public static void onNew(int index, Object obj, int b) {
	synchronized (locking) {
	    if (!enabled) return;
	    enabled = false;
	    //System.err.println("New "+index+" object "+System.identityHashCode(obj));
	    // Register the created object with the given index.
	    Integer i = new Integer(index);
	    objmap.put(obj, i);
	    for (int j=0; j<newcount.length; ++j) {
		if ((b&1) != 0) ++newcount[j];
		b >>= 1;
	    }
	    ++totalnewcount;
	    enabled = true;
	}
    }

    public static int[] lockcount;
    public static int totallockcount;
    private static void onMonitorHelper(HashMap map, int index, Object obj, int b) {
	synchronized (locking) {
	    if (!enabled) return;
	    enabled = false;
	    //System.err.println("Monitor"+((map == monitorEnterMap)?"enter":"exit")+" id "+index+" on object "+System.identityHashCode(obj));
	    // Look up the object id.
	    Integer i = (Integer)objmap.get(obj);
	    int in;
	    if (i == null) in = -1;
	    else in = i.intValue();

	    // Bump counter
	    Integer j = new Integer(index);
	    LinkedList ll = (LinkedList)map.get(j);
	    if (ll == null) {
	        map.put(j, ll = new LinkedList());
	    }
	    Counter c = new Counter(in);
	    int ind = ll.indexOf(c);
	    if (ind == -1) {
	        ll.addFirst(c);
	    } else {
	        c = (Counter)ll.get(ind);
	    }
	    c.inc();
	    for (int k=0; k<lockcount.length; ++k) {
		if ((b&1) != 0) ++lockcount[k];
		b >>= 1;
	    }
	    ++totallockcount;
	    enabled = true;
	}
    }

    static HashMap monitorEnterMap;
    static HashMap monitorExitMap;
    
    public static void onMonitorEnter(int index, Object obj, int b) {
	onMonitorHelper(monitorEnterMap, index, obj, b);
    }

    public static void onMonitorExit(int index, Object obj, int b) {
	onMonitorHelper(monitorExitMap, index, obj, b);
    }

    private static int dumpMap(String name, HashMap map) {
	int badsyncs = 0;
	Iterator i = map.keySet().iterator();
	while (i.hasNext()) {
	    Integer j = (Integer)i.next();
	    System.out.println(name+" id "+j+":");
	    LinkedList ll = (LinkedList)map.get(j);
	    java.util.Collections.sort(ll);
	    Iterator k = ll.iterator();
	    int total = 0;
	    while (k.hasNext()) {
		Counter l = (Counter)k.next();
		System.out.println("\tObject id "+l.getId()+"\tnum: "+l.getCount());
		total += l.getCount();
		if (l.getId() == -1) badsyncs += l.getCount();
	    }
	    System.out.println("\t\t\ttot: "+total);
	}
	return badsyncs;
    }
    
    public static void dump() {
	int badsyncs;
	enabled = false;
	System.out.println("MONITORENTER stats:");
	badsyncs = dumpMap("MONITORENTER", monitorEnterMap);
	System.out.println("MONITOREXIT stats:");
	badsyncs += dumpMap("MONITOREXIT", monitorExitMap);
	System.out.println("#objs (total): "+totalnewcount);
	for (int j=0; j<newcount.length; ++j) {
	    System.out.println("#objs"+j+": "+newcount[j]);
	}
	System.out.println("#locks (total): "+totallockcount);
	for (int j=0; j<lockcount.length; ++j) {
	    System.out.println("#locks"+j+": "+lockcount[j]);
	}
	System.out.println("#locks on -1: "+badsyncs);
	hasdumped = true;
	enabled = true;
    }
    
    private static class Counter implements Comparable {
	final int id;
	int count;
	Counter(int id) { this.id = id; }
	public boolean equals(Object o) { if (o instanceof Counter) return equals((Counter)o); return false; }
	public boolean equals(Counter that) { return this.id == that.id; }
	public int hashCode() { return id; }
	public int inc() { return ++count; }
	public int getId() { return id; }
	public int getCount() { return count; }
	public int compareTo(Counter that) {
	    if (this.id == that.id) return 0;
	    else if (this.id < that.id) return -1;
	    else return 1;
	}
	public int compareTo(Object o) { return compareTo((Counter)o); }
    }
    
}
