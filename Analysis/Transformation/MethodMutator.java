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
 * @version $Id: MethodMutator.java,v 1.4 2002-04-10 03:01:54 cananian Exp $
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
    /** Override this method to change the codename which this
     *  <code>MethodMutator</code>'s codefactory reports. */
    protected String mutateCodeName(String codeName) {
	return codeName;
    }
    /** Override this method if you do not want the mutatable HCode to be
     *  a straight clone of the original HCode: for example, if the
     *  input HCodes were <code>QuadSSI</code> and you wanted to
     *  clone them into <code>QuadRSSI</code>s before mutating.
     *  By default, this method returns <code>hc.clone(newmethod)</code>. */
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod)
	throws CloneNotSupportedException {
	return hc.clone(newmethod);
    }
    /** Returns a <code>HCodeFactory</code> containing representations for
     *  the methods altered by the <code>MethodMutator</code>. */
    public HCodeFactory codeFactory() { return hcf; }
    /** This is the code factory which contains the representations of the
     *  new split methods. */
    private final HCodeFactory hcf = new SerializableCodeFactory() {
        private final Map cache = new HashMap();
        public String getCodeName() {
	    return mutateCodeName(parent.getCodeName());
	}
        public HCode convert(HMethod m) {
            if (cache.containsKey(m)) return (HCode) cache.get(m);
            HCode hc = parent.convert(m);
            if (hc!=null)
		try {
		    hc = mutateHCode(cloneHCode(hc, m));
		} catch (CloneNotSupportedException ex) {
		    assert false : ("cloning HCode failed: "+ex);
		}
            cache.put(m, hc);
            return hc;
        }
        public void clear(HMethod m) {
            cache.remove(m); parent.clear(m);
        }
    };
}
