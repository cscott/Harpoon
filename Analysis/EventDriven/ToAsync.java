// ToAsync.java, created Thu Nov 11 12:41:56 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.ContBuilder.ContBuilder;
import harpoon.Analysis.EnvBuilder.EnvBuilder;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassSyn;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodSyn;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>ToAsync</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ToAsync.java,v 1.1.2.3 1999-11-22 21:38:53 bdemsky Exp $
 */
public class ToAsync {
    protected final UpdateCodeFactory ucf;
    protected final HCode hc;
    protected final ClassHierarchy ch;
    
    /** Creates a <code>ToAsync</code>. */
    public ToAsync(UpdateCodeFactory ucf, HCode hc, ClassHierarchy ch) {
        this.ucf = ucf;
	this.hc = hc;
	this.ch = ch;
    }
    
    public HMethod transform() {
	System.out.println("Entering ToAsync.transform()");
	AllCallers ac = new AllCallers(this.ch, this.ucf);
	BlockingMethods bm = new BlockingMethods();
	Set s = ac.getCallers(bm);

	for (Iterator i=s.iterator(); i.hasNext(); ) {
	    System.out.println("Blocks: " + ((HMethod)i.next()).toString());
	}

	final HMethod gis = 
	    HClass.forName("java.net.Socket").getDeclaredMethod
	    ("getInputStream", new HClass[0]);				

	Quad q = null;
	WorkSet toSwop = new WorkSet();
	for (ListIterator li = this.hc.getElementsL().listIterator();
	     li.hasNext(); ) {
	    q = (Quad)li.next();
	    if (q instanceof CALL) {
		System.out.println(q.toString());
		if (gis.equals(((CALL)q).method())) {
		    // need to swop
		    toSwop.push(q);
		} else if (s.contains(((CALL)q).method())) {
		    System.out.println(((CALL)q).method().toString());
		    final HMethod callee = ((CALL)q).method();
		    
		    // get the return type of the method we're transforming
		    final HMethod ohm = this.hc.getMethod();
		    String pref = ContBuilder.getPrefix(ohm.getReturnType());
		    
		    // new class, replace original
		    final HClassSyn nhc = 
			new HClassSyn(ohm.getDeclaringClass(), true);

		    // clone methods
		    HMethod[] toClone = 
			ohm.getDeclaringClass().getDeclaredMethods();
		    for (int i=0; i<toClone.length; i++) {
			HMethod curr = nhc.getDeclaredMethod
			    (toClone[i].getName(),
			     toClone[i].getParameterTypes());
			ucf.update(curr, ((QuadNoSSA)ucf.convert
					  (toClone[i])).clone(curr));
		    }

		    // set the return type of the transformed method
		    HClass rt = 
			HClass.forName("harpoon.Analysis.ContBuilder." +
				       pref + "Continuation");
		    final HMethodSyn nhm = 
			new HMethodSyn(nhc, ohm.getName() + "Async",
				       ohm.getParameterTypes(), rt);
		    nhm.setExceptionTypes(ohm.getExceptionTypes());
		    nhm.setModifiers(ohm.getModifiers());
		    nhm.setParameterNames(ohm.getParameterNames());
		    nhm.setSynthetic(ohm.isSynthetic());

		    // build environment
		    EnvBuilder eb = new EnvBuilder(this.ucf, this.hc, q);
		    HClass envClass = eb.makeEnv();
		    Util.assert(envClass.getConstructors().length == 1,
				"New environment class should have one " +
				"constructor, not " +
				envClass.getConstructors().length);
		    
		    // build continuation
		    ContBuilder cb = new ContBuilder(this.ucf, this.hc, 
						     (CALL)q, envClass, 
						     eb.liveout);
		    HClass contClass = cb.makeCont();
		    
		    // get the transformed version of the method we're calling
		    HMethod transformed = bm.swop(callee);
		    if (transformed == null)
			transformed = (new ToAsync(this.ucf, 
						   this.ucf.convert(callee), 
						   this.ch)).transform();
		    if (transformed == null) continue;
	
		    AsyncCode as = new AsyncCode(nhm, this.hc, (CALL)q, 
						 transformed, envClass, 
						 contClass, eb.liveout, 
						 toSwop);
		    ucf.update(nhm, as);
		    System.out.println("Leaving ToAsync.transform()");
		    return nhm;
		}
	    }
	}
	return null;
    }

    static class BlockingMethods implements AllCallers.MethodSet {

	/** Returns true if the <code>HMethod</code> blocks, false
	 *  otherwise. Checks against a list of known blocking methods.
	 */
	public boolean select (final HMethod m) {
	    if (swop(m) != null) {
		System.out.println(m.toString());
		return true;
	    } else
		return false;
	}

	/** Returns the corresponding asynchronous method for a given
	 *  blocking method if one exists.
	 */
	final private Map cache = new HashMap();
	public HMethod swop (final HMethod m) {
	    final HClass is = HClass.forName("java.io.InputStream");
	    final HClass ss = HClass.forName("java.net.ServerSocket");
	    final HClass b = HClass.Byte;

	    HMethod retval = (HMethod)cache.get(m);
	    if (retval == null) {
		if (is.equals(m.getDeclaringClass())) {
		    if (m.getName().equals("read")) {
			final HMethod bm1 = 
			    is.getDeclaredMethod("read", new HClass[0]);
			final HMethod bm2 = is.getDeclaredMethod("read", 
                            new HClass[] {HClassUtil.arrayClass(b, 1)});
			final HMethod bm3 = is.getDeclaredMethod("read", 
			    new HClass[] {HClassUtil.arrayClass(b, 1),
					  HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = is.getMethod("readAsync", 
						  new HClass[0]);
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = is.getMethod("readAsync", 
			        new HClass[] {HClassUtil.arrayClass(b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = is.getMethod("readAsync", 
				new HClass[] {HClassUtil.arrayClass(b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (ss.equals(m.getDeclaringClass())) {
		    final HMethod bm4 = 
			ss.getDeclaredMethod("accept", new HClass[0]);
		    if (bm4.equals(m)) {
			retval = ss.getDeclaredMethod("acceptAsync", 
						      new HClass[0]);
			cache.put(m, retval);
		    }
		}
	    }
	    return retval;
	} // swop

    } // BlockingMethods

} // ToAsync
