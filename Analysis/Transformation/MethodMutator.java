// MethodMutator.java, created Fri Oct  6 20:57:16 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transformation;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.SerializableCodeFactory;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;
/**
 * <code>MethodMutator</code> makes it easier to implement simple
 * method code mutations.  It is meant to be subclassed.  In your
 * subclass, you will likely want to override <code>mutateHCode()</code>
 * to effect the change.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodMutator.java,v 1.1.2.3 2000-11-08 18:37:19 cananian Exp $
 */
public abstract class MethodMutator implements java.io.Serializable {
    /** This is the code factory which contains the representations of the
     *  original (unsplit) methods. */
    private final HCodeFactory parent;
    
    /** Creates a <code>MethodMutator</code>. */
    public MethodMutator(HCodeFactory parent) {
        this.parent = parent;
    }
    /** Override this method to effect transformations on split
     *  methods. */
    protected HCode mutateHCode(HCodeAndMaps input) {
	return input.hcode();
    }
    /** Returns a <code>HCodeFactory</code> containing representations for
     *  the methods split by the <code>MethodSplitter</code>. */
    public final HCodeFactory codeFactory() { return hcf; }
    /** This is the code factory which contains the representations of the
     *  new split methods. */
    private final HCodeFactory hcf = new SerializableCodeFactory() {
        private final Map cache = new HashMap();
        public String getCodeName() { return parent.getCodeName(); }
        public HCode convert(HMethod m) {
            if (cache.containsKey(m)) return (HCode) cache.get(m);
            HCode hc = parent.convert(m);
            if (hc!=null)
		try {
		    hc = mutateHCode(hc.clone(m));
		} catch (CloneNotSupportedException ex) {
		    Util.assert(false, "cloning HCode failed: "+ex);
		}
            cache.put(m, hc);
            return hc;
        }
        public void clear(HMethod m) {
            cache.remove(m); parent.clear(m);
        }
    };
}
