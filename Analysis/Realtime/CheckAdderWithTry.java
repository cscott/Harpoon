// CheckAdder.java, created Fri Mar 23 10:31:56 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.io.PrintWriter;

import harpoon.Analysis.Transformation.MethodMutator;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.SET;

import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

/**
 * <code>CheckAdderWithTry</code> attaches <code>javax.realtime.MemoryArea</code>s to
 * <code>NEW</code>s and <code>ANEW</code>s.  It also adds checks around 
 * <code>SET</code>s and <code>ASET</code>s only if the 
 * <code>CheckRemoval</code> indicates that the check cannot be removed.
 * It takes QuadsWithTry form code as input. 
 *
 * @author Wes Beebee <wbeebee@mit.edu>
 * @version $Id: CheckAdderWithTry.java,v 1.3 2002-02-26 22:41:57 cananian Exp $
 */

// Fix to be non-static...

class CheckAdderWithTry extends CheckAdder {
    private METHOD currentMethod;  // For smartMemAreaLoads
    private Temp currentMemArea;

    /** Creates a new <code>CheckAdderWithTry</code>, adding only the checks that
     *  can't be removed as specified by <code>CheckRemoval</code> and 
     *  <code>NoHeapCheckRemoval</code>.
     *  Use <code>hcf = (new CheckAdderWithTry(cr, nhcr, hcf)).codeFactory(); to link
     *  this <code>CheckAdder</code> into the <code>HCodeFactory</code> chain.
     */

    CheckAdderWithTry(CheckRemoval cr, 
		      NoHeapCheckRemoval nhcr, 
		      HCodeFactory parent) {
	super(cr, nhcr, parent);
	Util.ASSERT(parent.getCodeName().equals(QuadWithTry.codename),
		    "CheckAdderWithTry takes a QuadWithTry HCodeFactory not a " +
		    parent.getCodeName() + " HCodeFactory.");
    }

    /** Adds the checks to the code <code>input</code>. */

    protected HCode mutateHCode(HCodeAndMaps input) {
	currentMethod = null;
	currentMemArea = null;
	final Linker linker = input.ancestorHCode().getMethod()
	    .getDeclaringClass().getLinker();
	return mutateHCode(input, new QuadVisitor() {
		public void visit(ARRAYINIT q) {
		    Util.ASSERT(false, "ArrayInitRemover has not been run.");
		}

		public void visit(ASET q) {
		    if (!q.type().isPrimitive()) {
			CheckAdderWithTry.this.checkAccess(linker, q, 
							   q.objectref(), 
							   q.src());
		    }
		}
		
		public void visit(ANEW q) {
		    if (fastNew) {
			CheckAdderWithTry.this.newArrayObjectFast(linker, q,
								  q.dst(),
								  q.hclass(),
								  q.dims());
		    } else {
			CheckAdderWithTry.this.newArrayObject(linker, q, 
							      q.dst(), 
							      q.hclass(), 
							      q.dims());
		    }
		}
		
		public void visit(SET q) {
		    if (!q.field().getType().isPrimitive()) {
			CheckAdderWithTry.this.checkAccess(linker, q, 
							   q.objectref(), 
							   q.src());
		    }
		}
		
		public void visit(METHOD q) {
		    currentMethod = q;
		}
		
		public void visit(NEW q) {
		    if (fastNew) {
			CheckAdderWithTry.this.newObjectFast(linker, q, 
							     q.dst(), 
							     q.hclass());
		    } else {
			CheckAdderWithTry.this.newObject(linker, q, q.dst(), 
							 q.hclass());
		    }
		}
		
		public void visit(Quad q) {}
	    });
    }

    /** Attaches the current memory area to a new instance of an object.
     * <p>
     * <p>obj = new foo() becomes:
     * <p>t = RealtimeThread.currentRealtimeThread().memoryArea();
     * <p>obj = new foo();
     * <p>obj.memoryArea = t;
     * <p>
     */

    private void newObjectFast(Linker linker, Quad inst,
			       Temp dst, HClass hclass) {
	Stats.addNewObject();
	QuadFactory qf = inst.getFactory();
	HMethod hm = qf.getMethod();
	Temp memArea = addGetCurrentMemArea(linker, qf, hm, inst);
	Quad next = inst.next(0);
	Quad q0 = new SET(qf, inst, 
			  linker.forName("java.lang.Object")
			  .getDeclaredField("memoryArea"), 
			  dst, memArea);
	Edge splitEdge = next.prevEdge(0);
	Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
	if (Realtime.COLLECT_RUNTIME_STATS) {
	    Quad q1 = 
		new CALL(qf, inst, 
			 linker.forName("javax.realtime.Stats")
			 .getMethod("addNewObject",
				    new HClass[] {
					linker
					.forName("javax.realtime.MemoryArea")
				    }),
			 new Temp[] { memArea },
			 null, null, true, false, new Temp[0]);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
	    q1.addHandlers(inst.handlers());
	} else {
	    Quad.addEdge(q0, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
	}
	q0.addHandlers(inst.handlers());
	
    }


    /** Attaches the current memory area to a new instance of an object.
     * <p>
     * <p>obj = new foo() becomes:
     * <p>t = RealtimeThread.currentRealtimeThread().memoryArea();
     * <p>obj = new foo();
     * <p>t.bless(obj);
     * <p>
     */

    private void newObject(Linker linker, Quad inst, 
			   Temp dst, HClass hclass) {
	Stats.addNewObject();
	QuadFactory qf = inst.getFactory();
	HMethod hm = qf.getMethod();
	Temp memArea = addGetCurrentMemArea(linker, qf, hm, inst);
	Quad next = inst.next(0);
	Quad q0 = new CALL(qf, inst, 
			   linker.forName("javax.realtime.MemoryArea")
			   .getMethod("bless", 
				      new HClass[] { 
					  linker.forName("java.lang.Object") 
				      }),
			   new Temp[] { memArea, dst },
			   null, null, true, false, new Temp[0]);
	Edge splitEdge = next.prevEdge(0);
	Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
	Quad.addEdge(q0, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
	q0.addHandlers(inst.handlers());
    }
    

    /** Attaches the current memory area to a new instance of an object.
     * <p>
     * <p>obj = new foo()[1][2][3] becomes:
     * <p>t = RealtimeThread.currentRealtimeThread().memoryArea();
     * <p>obj = new foo()[1][2][3]
     * <p>obj.memoryArea = t;
     * <p>
     */

    private void newArrayObjectFast(Linker linker, Quad inst,
				    Temp dst, HClass hclass, 
				    Temp[] dims) {
	Stats.addNewArrayObject();
	QuadFactory qf = inst.getFactory();
	HMethod hm = qf.getMethod();
	Temp memArea = addGetCurrentMemArea(linker, qf, hm, inst);
	Quad next = inst.next(0);
	Quad q0 = new SET(qf, inst,
			  linker.forName("java.lang.Object")
			  .getDeclaredField("memoryArea"),
			  dst, memArea);
	Edge splitEdge = next.prevEdge(0);
	Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
	if (Realtime.COLLECT_RUNTIME_STATS) {
	    Quad q1 = 
		new CALL(qf, inst,
			 linker.forName("javax.realtime.Stats")
			 .getMethod("addNewArrayObject",
				    new HClass[] {
					linker
					.forName("javax.realtime.MemoryArea")
				    }),
			 new Temp[] { memArea },
			 null, null, true, false, new Temp[0]);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
	    q1.addHandlers(inst.handlers());
	} else {
	    Quad.addEdge(q0, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
	}
	q0.addHandlers(inst.handlers());
    }

    /** Attaches the current memory area to a new instance of an object.
     * <p>
     * <p>obj = new foo()[1][2][3] becomes:
     * <p>t = RealtimeThread.currentRealtimeThread().memoryArea();
     * <p>obj = new foo()[1][2][3]
     * <p>t.bless(obj, {1, 2, 3});
     * <p>
     */

    private void newArrayObject(final Linker linker, Quad inst, 
				Temp dst, HClass hclass, Temp[] dims) {
	Stats.addNewArrayObject();
	QuadFactory qf = inst.getFactory();
	TempFactory tf = qf.tempFactory();
	HMethod hm = qf.getMethod();
	Temp memArea = addGetCurrentMemArea(linker, qf, hm, inst);
	Quad next = inst.next(0);
	Temp dimsArray = new Temp(tf, "dimsArray");
	Temp numDims = new Temp(tf, "numDims");
	Temp newDst = new Temp(tf, "newArray");
	Quad newQuad = new ANEW(qf, inst, newDst, hclass, dims);
	Quad q0 = new CONST(qf, inst, numDims, 
			    new Integer(dims.length), HClass.Int);
	HClass intArray = 
	    HClassUtil.arrayClass(linker, HClass.Int, dims.length);
	Quad q1 = new ANEW(qf, inst, dimsArray, intArray, 
			   new Temp[] { numDims }); 
	Quad[] q2 = new Quad[2*dims.length];
	for (int i=0; i<dims.length; i++) {
	    Temp t1 = new Temp(tf, "uniq");
	    q2[2*i] = new CONST(qf, inst, t1, new Integer(i), HClass.Int);
	    q2[2*i+1] = new ASET(qf, inst, dimsArray, t1, dims[i], HClass.Int);
	    q2[2*i].addHandlers(inst.handlers());
	    q2[2*i+1].addHandlers(inst.handlers());
	}
	Quad q3 = new CALL(qf, inst,
			   linker.forName("javax.realtime.MemoryArea")
			   .getMethod("bless", 
				      new HClass[] { 
					  linker.forName("java.lang.Object"), 
					  intArray}),
			   new Temp[] { memArea, newDst, dimsArray }, 
			   null, null, true, false, new Temp[0]);
	Quad q4 = new MOVE(qf, inst, dst, newDst);
	Edge splitEdge = next.prevEdge(0);
	Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
	Quad.addEdges(new Quad[] {q0, q1, q2[0]});
	Quad.addEdges(q2);
	Quad.addEdges(new Quad[] {q2[q2.length-1], q3, q4});
	Quad.addEdge(q4, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
	Quad.replace(inst, newQuad);
	q0.addHandlers(inst.handlers());
	q1.addHandlers(inst.handlers());
	q3.addHandlers(inst.handlers());
	q4.addHandlers(inst.handlers());
    }
    
    /** Adds a check around: a.foo = b; or a[foo]=b;
     *  a must be able to access b.
     */
       
    private void checkAccess(Linker linker, Quad inst, 
			     Temp object, Temp src) {
	if (needsCheck(inst)) {
	    QuadFactory qf = inst.getFactory();
	    TempFactory tf = qf.tempFactory();
	    HMethod hm = qf.getMethod();
	    Temp objArea = new Temp(tf, "objMemArea");
	    Quad q0 = null;
	    HClass memoryArea = linker.forName("javax.realtime.MemoryArea");
	    if (object != null) {
		q0 = new CALL(qf, inst, memoryArea
			      .getMethod("getMemoryArea", new HClass[] {
				  linker.forName("java.lang.Object")}), 
			      new Temp[] { object }, objArea, null, 
			      false, false, new Temp[0]);
	    } else {
		q0 = new CALL(qf, inst,
			      linker.forName("javax.realtime.ImmortalMemory")
			      .getMethod("instance", new HClass[0]),
			      new Temp[0], objArea, null, 
			      false, false, new Temp[0]);
	    }
	    Quad q1 = new CALL(qf, inst, memoryArea
			       .getMethod("checkAccess", new HClass[] { 
				   linker.forName("java.lang.Object")}),
			       new Temp[] { objArea, src },
			       null, null, true, false, new Temp[0]);
	    Edge splitEdge = inst.prevEdge(0);
	    Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), 
			 q0, 0);
	    if (Realtime.COLLECT_RUNTIME_STATS) {
		Temp srcArea = new Temp(tf, "srcMemArea");
		Quad q2 = new CALL(qf, inst, memoryArea
				   .getMethod("getMemoryArea", new HClass[] {
				       linker.forName("java.lang.Object")}),
				   new Temp[] { src }, srcArea, null,
				   false, false, new Temp[0]);
		Quad q3 = 
		    new CALL(qf, inst, 
			     linker.forName("javax.realtime.Stats")
			     .getMethod("addCheck",
					new HClass[] {
					    memoryArea, 
					    memoryArea
					}),
			     new Temp[] { objArea, srcArea },
			     null, null, true, false, new Temp[0]);
		Quad.addEdges(new Quad[] {q0, q1, q2, q3});
		Quad.addEdge(q3, 0, (Quad)splitEdge.to(),
			     splitEdge.which_pred());
		q2.addHandlers(inst.handlers());
		q3.addHandlers(inst.handlers());
	    } else {
		Quad.addEdges(new Quad[] {q0, q1});
		Quad.addEdge(q1, 0, (Quad)splitEdge.to(), 
			     splitEdge.which_pred());
	    }	    
	    q0.addHandlers(inst.handlers());
	    q1.addHandlers(inst.handlers());
	}
    }
    
    /** */

//      private void checkNoHeapWrite(Linker linker, Quad inst,
    //  					 Temp object, Temp src) {
//  	if (needsNoHeapWriteCheck(inst)) {
	    

//  	}
//      }

    /** */

    private void checkNoHeapRead(Linker linker) {


    }

    private Temp addGetCurrentMemArea(Linker linker,
				      QuadFactory qf, HMethod hm,
				      Quad inst) {
	if (!smartMemAreaLoads) {
	    return realAddGetCurrentMemArea(linker, qf, hm, inst);
	} else if (currentMemArea == null) {
	    currentMemArea = 
		realAddGetCurrentMemArea(linker, qf, hm, currentMethod.next(0));
	} 
	return currentMemArea;
    }

    /** Adds t = RealtimeThread.currentRealtimeThread().memoryArea() */

    private Temp realAddGetCurrentMemArea(Linker linker, 
					  QuadFactory qf, HMethod hm, 
					  Quad inst) {
	TempFactory tf = qf.tempFactory();
	Stats.addMemAreaLoad();
	Temp t1 = new Temp(tf, "realtimeThread");
	Temp e = new Temp(tf, "exception");
	Temp currentMemArea = new Temp(tf, "memoryArea");
	HClass realtimeThread = 
	    linker.forName("javax.realtime.RealtimeThread");
	Quad q0 = new CALL(qf, inst, realtimeThread
			   .getMethod("currentRealtimeThread", new HClass[0]), 
			   new Temp[0], t1, null, false, false, new Temp[0]);
	Quad q1 = new CALL(qf, inst, realtimeThread
			   .getMethod("memoryArea", new HClass[0]), 
			   new Temp[] { t1 }, currentMemArea, null, false, 
			   false, new Temp[0]);
	Edge splitEdge = inst.prevEdge(0);
	Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
	Quad.addEdges(new Quad[] {q0, q1});
	Quad.addEdge(q1, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
	q0.addHandlers(inst.handlers());
	q1.addHandlers(inst.handlers());
	return currentMemArea;
    }
}

