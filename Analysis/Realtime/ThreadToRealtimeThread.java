// ThreadToRealtimeThread.java, created Tue Jan 23 16:09:50 2001 by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.util.Iterator;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Transformation.MethodMutator;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;

import harpoon.Util.Util;

/**
 * <code>ThreadToRealtimeThread</code> is a <code>ClassReplacer</code> which
 * replaces <code>java.lang.Thread</code> with 
 * <code>javax.realtime.RealtimeThread</code>.
 *
 * @author Wes Beebee <wbeebee@mit.edu>
 * @version $Id: ThreadToRealtimeThread.java,v 1.1.2.5 2001-06-17 23:07:32 cananian Exp $
 */

class ThreadToRealtimeThread extends ClassReplacer {
    
    /** Creates a new <code>ThreadToRealtimeThread</code> using code from the
     *  <code>HCodeFactory</code>.  Use 
     *  (new ThreadToRealtimeThread(hcf)).codeFactory() to link it into the
     *  chain of </code>HCodeFactory</code>s.
     */
    
    ThreadToRealtimeThread(HCodeFactory parent, Linker linker) {
	super(parent, 
	      linker.forName("java.lang.Thread"), 
	      linker.forName("javax.realtime.RealtimeThread"));
	addIgnorePackage("javax.realtime");

	HConstructor[] threadConstructors = 
	    linker.forName("java.lang.Thread").getConstructors();
	HConstructor[] realtimeThreadConstructors = 
	    linker.forName("javax.realtime.RealtimeThread").getConstructors();
	for (int i=0; i<threadConstructors.length; i++) {
	    for (int j=0; j<realtimeThreadConstructors.length; j++) {
		HClass[] types = threadConstructors[i].getParameterTypes();
		HClass[] types2 = realtimeThreadConstructors[i].getParameterTypes();
		if (types.length == types2.length) {
		    boolean mapMethod = true;
		    for (int k=0; k<types.length; k++) {
			if (!types[k].equals(types2[k])) {
			    mapMethod = false;
			    break;
			}
		    }
		    if (mapMethod) {
			map(threadConstructors[i],
			    realtimeThreadConstructors[j]);
		    }
		}
	    }
	}
    }
    
    /** Updates the <code>ClassHierarchy</code> so that all classes that
     *  inherited from <code>java.lang.Thread</code> now inherit
     *  from <code>javax.realtime.RealtimeThread</code>.
     */

    static void updateClassHierarchy(Linker linker, ClassHierarchy ch) {
	HClass thread = linker.forName("java.lang.Thread");
	HClass realtimeThread = linker.forName("javax.realtime.RealtimeThread");
	for (Iterator children = ch.children(thread).iterator(); 
	     children.hasNext();) {
	    HClass child = (HClass)children.next();
	    if (child != realtimeThread) {
		Util.assert(child != null);
		child.getMutator().setSuperclass(realtimeThread);
	    }
	}
    }
}
