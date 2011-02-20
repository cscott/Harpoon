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
 * @version $Id: ThreadToRealtimeThread.java,v 1.5 2002-08-14 20:51:45 wbeebee Exp $
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
	mapAll(linker.forName("java.lang.Thread"), linker.forName("javax.realtime.RealtimeThread"));
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
		assert child != null;
		child.getMutator().setSuperclass(realtimeThread);
	    }
	}
    }
}
