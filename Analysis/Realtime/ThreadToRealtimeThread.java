// ThreadToRealtimeThread.java, created by wbeebee
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
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;

import harpoon.Util.Util;

/**
 * <code>ThreadToRealtimeThread</code> is a <code>MethodMutator</code> which
 * replaces <code>java.lang.Thread</code> with 
 * <code>javax.realtime.RealtimeThread</code>.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

class ThreadToRealtimeThread extends MethodMutator {
    
    /** Creates a new <code>ThreadToRealtimeThread</code> using code from the
     *  <code>HCodeFactory</code>.  Use 
     *  (new ThreadToRealtimeThread(hcf)).codeFactory() to link it into the
     *  chain of </code>HCodeFactory</code>s.
     */
    
    ThreadToRealtimeThread(HCodeFactory parent) {
	super(parent);
	Util.assert(parent.getCodeName().equals(QuadWithTry.codename),
		    "ThreadToRealtimeThread takes a QuadWithTry HCodeFactory"
		    +" not a "+ parent.getCodeName() +" HCodeFactory.");
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
    
    /** Replaces new java.lang.Thread() with new 
     *  javax.realtime.RealtimeThread(). 
     */

    protected HCode mutateHCode(HCodeAndMaps input) {
	Stats.realtimeBegin();
	HCode hc = input.hcode();
	HClass hclass = hc.getMethod().getDeclaringClass();
	if ((hc == null)||      // Prevent infinite recursion.
	    (hclass.getName().startsWith("javax.realtime."))) {
	    Stats.realtimeEnd();
	    return hc;
	}    
	final Linker linker = hclass.getLinker();
	final HClass realtimeThread = 
	    linker.forName("javax.realtime.RealtimeThread");
	final HClass thread = linker.forName("java.lang.Thread");

	QuadVisitor visitor = new QuadVisitor() {
		public void visit(CALL q) {
		    HMethod method = q.method();
		    if ((method.getDeclaringClass() != thread) ||
			(method != thread.getConstructor(new HClass[] {})))
			return;
		    CALL newCALL = 
			new CALL(q.getFactory(), q, 
				 realtimeThread
				 .getConstructor(method.getParameterTypes()),
				 q.params(), q.retval(), q.retex(), 
				 q.isVirtual(), q.isTailCall(), 
				 q.dst(), q.src());
		    Quad.replace(q, newCALL);
		    Quad.transferHandlers(q, newCALL);
		}

		public void visit(NEW q) {
		    if (q.hclass() == thread) {
			NEW newNEW = new NEW(q.getFactory(), 
					      q, q.dst(), realtimeThread);
			Quad.replace(q, newNEW);
			Quad.transferHandlers(q, newNEW);
		    }
		}

		public void visit(Quad q) {}
	    };
	
	// System.out.println("Before ThreadToRealtimeThread:");
	// hc.print(new PrintWriter(System.out));
	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++) {
	    ql[i].accept(visitor);
	}      
	// System.out.println("After ThreadToRealtimeThread:");
	// hc.print(new PrintWriter(System.out));
	Stats.realtimeEnd();
	return hc;
    }
}
