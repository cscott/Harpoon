// Realtime.java, created by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;
import harpoon.Util.HClassUtil;

/**
 * <code>Realtime</code> is the top-level access point for the rest of the Harpoon compiler to
 * provide support for the Realtime Java MemoryArea extensions described in the 
 * <a href="http://java.sun.com/aboutJava/communityprocess/first/jsr001/rtj.pdf">Realtime Java Specification</a>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Realtime {
  /** Is Realtime JAVA support turned on? 
   *  <p>
   *  <p>If <code>REALTIME_JAVA</code> == false, 
   *  <p>then all methods in this class have no effect.
   */

  public static boolean REALTIME_JAVA = false;

    /** Creates a field memoryArea on <code>java.lang.Object</code>.
     *  Since primitive arrays inherit from java.lang.Object, this catches them as well. 
     */
  
  public static void setupObject(Linker linker) {
    Stats.realtimeBegin();
    // Adds realtime.MemoryArea java.lang.Object.memoryArea
    linker.forName("java.lang.Object").getMutator().addDeclaredField("memoryArea",
                                                                     linker.forName("realtime.MemoryArea"));
    Stats.realtimeEnd();
  }


  /** Adds realtime support to block of code using an <code>harpoon.ClassFile.HCodeFactory</code>. 
   *  <ul>
   *  <li> Makes sure that all classes that inherited from <code>java.lang.Thread</code> now inherit from 
   *       <code>realtime.RealtimeThread</code>. </li>
   *  <li> Makes every new <code>java.lang.Thread</code> into a new <code>realtime.RealtimeThread</code>. </li>
   *  <li> Attaches the <code>realtime.RealtimeThread</code>.currentRealtimeThread().getMemoryArea() to
   *       every new array and new object (sets the field <code>java.lang.Object.memoryArea</code>). </li>
   *  <li> Wraps checks around <code>harpoon.IR.Quads.SET</code>s and <code>harpoon.IR.Quads.ASET</code>s. </li>
   *  </ul>
   */

  public static HCodeFactory setupCode(final Linker linker, 
                                       final ClassHierarchy ch,
                                       final HCodeFactory parent) {
    Stats.realtimeBegin();
    Iterator children = ch.children(linker.forName("java.lang.Thread")).iterator();
    while (children.hasNext()) {
      HClass child = (HClass)children.next();
      if (!child.getName().equals("realtime.RealtimeThread")) {
	child.getMutator().setSuperclass(linker.forName("realtime.RealtimeThread"));
      }      
    }

    HCodeFactory result = addRealtimeSupport(linker, parent);
    Stats.realtimeEnd();
    return result;
  }


  private static HCodeFactory addRealtimeSupport(final Linker linker, final HCodeFactory parent) {  
    Util.assert(parent.getCodeName().equals("quad-with-try"), 
                "addRealtimeSupport takes a QuadWithTry HCodeFactory not a "+
                parent.getCodeName()+" HCodeFactory.");
    return new HCodeFactory() {
        public HCode convert(HMethod m) {
//            System.out.println("Converting Method: "+m.toString());
          HCode hc = parent.convert(m);
          Stats.realtimeBegin();
          if ((hc != null)&&
              (!m.getDeclaringClass().getName().startsWith("realtime."))) { // Prevent infinite recursion.
//              System.out.println("Before:");
//              hc.print(new PrintWriter(System.out));
            try {
              hc = (HCode)hc.clone();
            } catch (CloneNotSupportedException e) {
              Util.assert(false, "HCode.clone() not supported");
            }
            Realtime.addRealtimeCode(linker, hc);
//              System.out.println("After:");
//              hc.print(new PrintWriter(System.out));
          }
          Stats.realtimeEnd();
          return hc;
        }
        public String getCodeName() { return parent.getCodeName(); }
        public void clear(HMethod m) { parent.clear(m); }
      };     
  }

  /** Adds Realtime support to an <code>harpoon.ClassFile.HCode</code>. */

  private static void addRealtimeCode(final Linker linker, HCode hc) {
    QuadVisitor visitor = new QuadVisitor() {
	public void visit(ASET q) {
	  Realtime.checkAccess(linker, q, q.objectref(), q.src());
	}
	
        public void visit(ANEW q) {
          Realtime.newArrayObject(linker, q, q.dst(), q.hclass(), q.dims());
        }

        public void visit(CALL q) {
          HMethod method = q.method();
          if (method.getDeclaringClass().getName().equals("java.lang.Thread")&&
              (method.getName().equals("<init>"))) {
            CALL newQuad = new CALL(q.getFactory(), q, 
                                    linker.forName("realtime.RealtimeThread")
                                    .getDeclaredMethod("<init>", method.getParameterTypes()),
                                    q.params(), q.retval(), q.retex(), q.isVirtual(), 
                                    q.isTailCall(), q.dst(), q.src());
            Quad.replace(q, newQuad);
            q = newQuad;
          } 
        }

	public void visit(SET q) {
	  Realtime.checkAccess(linker, q, q.objectref(), q.src());
	}

        public void visit(NEW q) {
          if (q.hclass().getName().equals("java.lang.Thread")) {
            NEW newQuad = new NEW(q.getFactory(), q, q.dst(), linker.forName("realtime.RealtimeThread"));
            Quad.replace(q, newQuad);
            Realtime.newObject(linker, newQuad, newQuad.dst(), newQuad.hclass());
          } else {
            Realtime.newObject(linker, q, q.dst(), q.hclass());
          }
        }

        public void visit(Quad q) { // Called for all others
//            System.out.println(q.toString());
        }
      };
      
    Quad[] ql = (Quad[]) hc.getElements();
    Stats.addQuads(ql.length);
    for (int i=0; i<ql.length; i++) 
      ql[i].accept(visitor);
    Stats.addQuadsOut(hc.getElements().length);
  }

  /** Attaches the current memory area to a new instance of an object.
   * <p>
   * <p>obj = new foo() becomes:
   * <p>t = RealtimeThread.currentRealtimeThread().getMemoryArea();
   * <p>obj = new foo();
   * <p>t.bless(obj);
   * <p>
   */

  public static void newObject(final Linker linker, Quad inst, Temp dst, HClass hclass) {
    Stats.addBlessedObject();
    QuadFactory qf = inst.getFactory();
    HMethod hm = qf.getMethod();
    Temp memArea = addGetCurrentMemArea(linker, qf, hm, inst);
    Quad next = inst.next(0);
    Quad q0 = new CALL(qf, inst, 
                       linker.forName("realtime.MemoryArea").getMethod("bless", 
                         new HClass[] { linker.forName("java.lang.Object") }),
                       new Temp[] { memArea, dst },
                       null, 
                       null,
                       true,
                       false,
                       new Temp[0]);
    Edge splitEdge = next.prevEdge(0);
    Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
    Quad.addEdge(q0, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
    q0.addHandlers(inst.handlers());
  }
  
  /** Attaches the current memory area to a new instance of an object.
   * <p>
   * <p>obj = new foo()[1][2][3] becomes:
   * <p>t = RealtimeThread.currentRealtimeThread().getMemoryArea();
   * <p>obj = new foo()[1][2][3]
   * <p>t.bless(obj, {1, 2, 3});
   * <p>
   */

  public static void newArrayObject(final Linker linker, Quad inst, Temp dst, 
                                    HClass hclass, Temp[] dims) {
    Stats.addBlessedArrayObject();
    QuadFactory qf = inst.getFactory();
    TempFactory tf = qf.tempFactory();
    HMethod hm = qf.getMethod();
    Temp memArea = addGetCurrentMemArea(linker, qf, hm, inst);
    Quad next = inst.next(0);
    Temp dimsArray = new Temp(tf, "dimsArray");
    Temp numDims = new Temp(tf, "numDims");
    Temp newDst = new Temp(tf, "newArray");
    Quad newQuad = new ANEW(qf, inst, newDst, hclass, dims);
    Quad q0 = new CONST(qf, inst, numDims, new Integer(dims.length), HClass.Int);
    HClass intArray = HClassUtil.arrayClass(linker, HClass.Int, dims.length);
    Quad q1 = new ANEW(qf, inst, dimsArray, intArray, new Temp[] { numDims }); 
    Quad[] q2 = new Quad[2*dims.length];
    for (int i=0; i<dims.length; i++) {
      Temp t1 = new Temp(tf, "uniq");
      q2[2*i] = new CONST(qf, inst, t1, new Integer(i), HClass.Int);
      q2[2*i+1] = new ASET(qf, inst, dimsArray, t1, dims[i], HClass.Int);
      q2[2*i].addHandlers(inst.handlers());
      q2[2*i+1].addHandlers(inst.handlers());
    }
    Quad q3 = new CALL(qf, inst,
                       linker.forName("realtime.MemoryArea").getMethod("bless", 
                         new HClass[] { linker.forName("java.lang.Object"), intArray}),
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

    /** Adds a check around: a.foo = b;  a must be able to access b */


  public static void checkAccess(final Linker linker, Quad inst, Temp object, Temp src) {
    Util.assert(src != null);
    if (inst instanceof SET) {
      if (((SET)inst).field().getType().isPrimitive()) return;
    }
    if (inst instanceof ASET) {
      if (((ASET)inst).type().isPrimitive()) return;
    }
    if (needsCheck(inst, object, src)) {
      QuadFactory qf = inst.getFactory();
      TempFactory tf = qf.tempFactory();
      HMethod hm = qf.getMethod();
      Temp objArea = new Temp(tf, "objMemArea");
      Quad q0 = null;
      if (object != null) {
	  q0 = new CALL(qf, inst,
			linker.forName("realtime.MemoryArea").getMethod("getMemoryArea", new HClass[] {
			    linker.forName("java.lang.Object")}), 
			new Temp[] { object }, objArea, null, false, false, new Temp[0]);
      } else {
	  q0 = new CALL(qf, inst, 
			linker.forName("realtime.HeapMemory").getMethod("instance", new HClass[0]),
			new Temp[0], objArea, null, false, false, new Temp[0]);
      }
      Quad q1 = new CALL(qf, inst,
			 linker.forName("realtime.MemoryArea").getMethod("checkAccess", new HClass[] { 
			   linker.forName("java.lang.Object")}),
			 new Temp[] { objArea, src },
			 null, null, true, false, new Temp[0]);
      Edge splitEdge = inst.prevEdge(0);
      Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
      Quad.addEdges(new Quad[] {q0, q1});
      Quad.addEdge(q1, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
      q0.addHandlers(inst.handlers());
      q1.addHandlers(inst.handlers());
    }
  }

  /** Adds t = RealtimeThread.currentRealtimeThread().getMemoryArea() */

  private static Temp addGetCurrentMemArea(final Linker linker, QuadFactory qf, HMethod hm, Quad inst) {
    TempFactory tf = qf.tempFactory();
    Stats.addMemAreaLoad();
    Temp t1 = new Temp(tf, "realtimeThread");
    Temp currentMemArea = new Temp(tf, "memoryArea");
    Quad q0 = new CALL(qf, inst,
                       linker.forName("realtime.RealtimeThread").getMethod("currentRealtimeThread", 
                         new HClass[0]), new Temp[0], t1, null, false, false, new Temp[0]);
    Quad q1 = new CALL(qf, inst, 
                       linker.forName("realtime.RealtimeThread").getMethod("getMemoryArea", 
                         new HClass[0]), new Temp[] { t1 }, currentMemArea, null, false, false, 
                         new Temp[0]);
    Edge splitEdge = inst.prevEdge(0);
    Quad.addEdge((Quad)splitEdge.from(), splitEdge.which_succ(), q0, 0);
    Quad.addEdges(new Quad[] {q0, q1});
    Quad.addEdge(q1, 0, (Quad)splitEdge.to(), splitEdge.which_pred());
    q0.addHandlers(inst.handlers());
    q1.addHandlers(inst.handlers());
    return currentMemArea;
  }
      

  /** Indicates if the given instruction needs an access check wrapped around it. */

  private static boolean needsCheck(Quad inst, Temp object, Temp src) {
    Stats.addActualMemCheck();
    return true;
  }

  /** Print statistics about the static analysis and addition of Realtime support. */

  public static void printStats() {
    Stats.print();
  }
}




