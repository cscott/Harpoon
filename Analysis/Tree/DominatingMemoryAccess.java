// DominatingMemoryAccess.java created Fri Oct 27 16:33:24 EDT 2000
// Copyright (C) 2000 Emmett Witchel <witchel@lcs.mit.edu>

package harpoon.Analysis.Tree;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.Analysis.DomTree;
import harpoon.IR.Properties.CFGrapher;

import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CONST;
// harpoon.IR.Tree.TEMP are tree temps, basically all unique
// Closer to a virtual register 
import harpoon.IR.Tree.TEMP;
import harpoon.Temp.Temp;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.Exp;
import harpoon.Util.Util;
import harpoon.Backend.Generic.Frame;

import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

public class DominatingMemoryAccess {

   class EqClasses {
      class EqClass {
         // An arbitrary (but it should be unique) class_num identifier
         // And the size of the object pointed to in this class
         EqClass(int class_num, int size) {
            this.class_num = class_num;
            this.size = size;
         }
         public boolean equals(Object obj) {
            if(!(obj instanceof EqClass)) return false;
            EqClass eqc = (EqClass)obj;
            if(class_num != eqc.class_num) return false;
            return true;
         }
         /** Pass in the offset in the object being used, and we tell
         you if this access doesn't need to use a tag check (of course
         assuming it is in the same eq class as a dominating checked access
         */
         public boolean useNoTagCheck(int offset) {
            if(size <= 32) return true;
            return false;
         }
         public String toString() {
            return " MEMC " + class_num + "(" + size + ")";
         }
         private int class_num;
         private int size;
      }
      class TempMap {
         TempMap(Temp oldt, Temp newt) {
            this.oldt = oldt;
            this.newt = newt;
         }
         public String toString() {
            return " MEMC " + newt + " <- " + oldt;
         }
         private Temp oldt;
         private Temp newt;
      }
      public EqClasses(HCode hc) {
         currtemps = new HashMap();
         classes   = new HashMap();
         class_num = 0;
         this.hc = hc;
      }
      private EqClasses(EqClasses eqc) {
         this.currtemps = new HashMap(eqc.currtemps);
         this.classes   = new HashMap(eqc.classes);
         this.class_num = eqc.class_num;
      }
      public void clearTemps() {
         currtemps.clear();
      }
      public HashMap getClasses() {
         return classes;
      }

      public void clearTemp(Temp t) {
         currtemps.remove(t);
      }
      public EqClasses saveTemps() {
         return new EqClasses(this);
      }
      public EqClasses restoreTemps(EqClasses eqc) {
         currtemps = eqc.currtemps;
         return this;
      }
      public void addTemp(HCodeElement hce, Temp oldt, Temp newt) {
         if(oldt != newt) {
            if(currtemps.containsKey(oldt)) {
               EqClasses.EqClass cl = (EqClass)currtemps.get(oldt);
               currtemps.put(newt, cl);
            }
            // This is for tracing
            classes.put(hce, new TempMap(oldt, newt));
         }
      }
      public void useTemp(HCodeElement hce, TEMP T) {
         Temp t = T.temp;
         if(currtemps.containsKey(t)) {
            EqClass cl = (EqClass)currtemps.get(t);
            if(trace) {
               System.out.println(cl + "\tgets hce " + hce + "\thcecode=" 
                                  + hce.hashCode());
            }
            classes.put(hce, cl);
         } else {
            mkClass(hce, T);
         }
      }
      public EqClass getClass(HCodeElement hce) {
         Object val = classes.get(hce);
         if(!(val instanceof TempMap)) {
            return (EqClass)classes.get(hce);
         }
         return null;
      }

      private void mkClass(HCodeElement hce, TEMP T) {
         Temp t = T.temp;
         Util.assert(classes.containsKey(hce) == false);
         HClass hclass = ((CanonicalTreeCode)hc).getTreeDerivation().typeMap(T);
         int sz = 0;
         if(hclass == null) {
            // System.out.println("Yikes TEMP = " + T + " Temp=" + t + " hce " + hce + " hcecode=" + hce.hashCode());
            sz = 37; /* XXX */
         } else {
            sz = frame.getRuntime().treeBuilder.objectSize(hclass);
         }
         EqClass newcl = new EqClass(++class_num, sz);
         if(trace) {
            System.out.println(newcl + " gets hce " + hce + " hcecode=" 
                               + hce.hashCode());
         }
         classes.put(hce, newcl);
         Util.assert(currtemps.containsKey(t) == false);
         currtemps.put(t, newcl);
      }

      private HashMap currtemps;
      private HashMap classes;
      private HCode hc;
      int class_num = 0;
   }
   private void eqMem(EqClasses eqClasses, MEM mem, MOVE move) {
      switch(mem.getExp().kind()) {
      case TreeKind.TEMP:{
         //Temp t = ((harpoon.IR.Tree.TEMP)mem.getExp()).temp;
         //eqClasses.useTemp(mem, t);
         Temp t = ((harpoon.IR.Tree.TEMP)mem.getExp()).temp;
         eqClasses.useTemp(mem, (TEMP)mem.getExp());

         break;
      }
      case TreeKind.BINOP:{
         BINOP binop = (BINOP)mem.getExp();
         if(binop.op == Bop.ADD) {
            if(binop.getRight().kind() == TreeKind.CONST
               && binop.getLeft().kind() == TreeKind.TEMP) {
               Number c = ((CONST)binop.getRight()).value;
               eqClasses.useTemp(mem, (TEMP)binop.getLeft());
            }
         }
      }
      }
   }

   private void eqMove(EqClasses eqClasses, MOVE m) {
      int srcKind = m.getSrc().kind();
      int dstKind = m.getDst().kind();
      // There are memory to memory moves, but that is ok
      if(srcKind == TreeKind.MEM) {
         eqMem(eqClasses, (MEM)m.getSrc(), m);
      }
      if(dstKind == TreeKind.MEM) {
         eqMem(eqClasses, (MEM)m.getDst(), m);
      } 
      if(srcKind == TreeKind.TEMP && dstKind == TreeKind.TEMP) {
         // Temp move
         int oldType = ((harpoon.IR.Tree.TEMP) m.getSrc()).type();
         int newType = ((harpoon.IR.Tree.TEMP) m.getDst()).type();
         Temp oldt = ((harpoon.IR.Tree.TEMP) m.getSrc()).temp;
         Temp newt = ((harpoon.IR.Tree.TEMP) m.getDst()).temp;
         // We can only get to pointers from pointers, right?
         Util.assert(!
                     (oldType == Typed.POINTER && newType != Typed.POINTER)
                     || 
                     (oldType != Typed.POINTER && newType == Typed.POINTER));
         if(oldType == Typed.POINTER && newType == Typed.POINTER) {
            eqClasses.addTemp(m, oldt, newt);
         }
      } else if(dstKind == TreeKind.TEMP 
                && ((harpoon.IR.Tree.TEMP) m.getDst()).type() == Typed.POINTER) {
         boolean processed = false;
         if(srcKind == TreeKind.BINOP) {
            BINOP binop = (BINOP)m.getSrc();
            if(binop.op == Bop.ADD) {
               if(binop.getRight().kind() == TreeKind.CONST
                  && binop.getLeft().kind() == TreeKind.TEMP) {
                  Number c = ((CONST)binop.getRight()).value;
                  Temp   t = ((harpoon.IR.Tree.TEMP)binop.getLeft()).temp;
                  if(trace) {
                     System.out.println("ADDING TO A PTR -- " + m 
                                        + " hc=" + m.hashCode());
                  }
                  eqClasses.clearTemp(((harpoon.IR.Tree.TEMP)m.getDst()).temp);
                  processed = true;
               }
            }
         }
         if(!processed)
            eqClasses.clearTemp(((harpoon.IR.Tree.TEMP)m.getDst()).temp);
      }
   }

   private void doEqClasses(Tree tr, CFGrapher cfgr, 
                            EqClasses eqClasses, HashMap visited) {
      visited.put(tr, new Integer(0));
      if(cfgr.pred(tr).length > 1) {
         // This is a join point
         eqClasses.clearTemps();
      }
      switch(tr.kind()) {
      case TreeKind.MOVE:
         eqMove(eqClasses, (MOVE)tr);
         break;
      case TreeKind.CALL:
      case TreeKind.NATIVECALL:
      case TreeKind.THROW:
         eqClasses.clearTemps();
         break;
      }
      HCodeEdge[] succ = cfgr.succ(tr);
      for(int i = 0; i < succ.length; ++i) {
         Tree newtr = (Tree)succ[i].to();
         if(visited.containsKey(newtr) == false) {
            EqClasses eqc = eqClasses.saveTemps();
            doEqClasses(newtr, cfgr, eqClasses.restoreTemps(eqc), 
                        visited);
         }
      }
   }
   private void tryDominate(HCodeElement hce, final EqClasses eqClasses,
                            HashMap active, HashMap noTagCheck) {
      EqClasses.EqClass eqc = eqClasses.getClass(hce);
      if(eqc != null) {
         if(active.containsKey(eqc)) {
            // This is it, this use is dominated
            if(eqc.useNoTagCheck(0)) {
               if(trace) {
                  System.out.println("hce " + hce.hashCode() + "not tag checked");
               }
               noTagCheck.put(hce, new Integer(0));
            }
         } else {
            active.put(eqc, new Integer(0));
         }
      }
   }
   private void doDominance(DomTree dt, HCodeElement hce, 
                            harpoon.IR.Tree.Code code, CFGrapher cfgr,
                            final EqClasses eqClasses, HashMap active,
                            HashMap noTagCheck) {
      HCodeElement[] children = dt.children(hce);
      if(hce instanceof MOVE) {
         MOVE m = (MOVE)hce;
         if(trace) {
            System.out.println("hce " + hce + " code " + hce.hashCode() + " (" + children.length + ") active " + active);
         }
         tryDominate(m.getSrc(), eqClasses, active, noTagCheck);
         tryDominate(m.getDst(), eqClasses, active, noTagCheck);
      }
      for(int i = 0; i < children.length; ++i) {
         doDominance(dt, children[i], code, cfgr, eqClasses, 
                     new HashMap(active), noTagCheck);
      }
   }
   
   public HCodeFactory codeFactory() {
      Util.assert(parent.getCodeName().equals(CanonicalTreeCode.codename));
      return new HCodeFactory() {
            public HCode convert(HMethod m) {
               HCode hc = parent.convert(m);
               harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
               final CFGrapher cfgr = code.getGrapher();
               EqClasses eqClasses = new EqClasses(hc);
               HashMap visited = new HashMap();
               doEqClasses((Tree)cfgr.getFirstElement(code), cfgr, eqClasses, 
                           visited);
               if( trace ) {
                  harpoon.IR.Tree.Print.print(
                     new java.io.PrintWriter(System.out), code, eqClasses.getClasses());
               }

               HashMap noTagCheck = new HashMap();
               DomTree dt = new DomTree(hc, cfgr, false);
               HCodeElement[] roots = dt.roots();
               for(int i = 0; i < roots.length; ++i) {
                  doDominance(dt, roots[i], code, cfgr, eqClasses, 
                              new HashMap(), noTagCheck);
               }
               ((harpoon.Backend.MIPS.Frame)frame).setNoTagCheckHashMap(noTagCheck);
               return hc;
            }
            public String getCodeName() { return parent.getCodeName(); }
            public void clear(HMethod m) { parent.clear(m); }
         };
   }

   public DominatingMemoryAccess(final HCodeFactory parent, 
                                 final Frame frame) {
      this.parent = parent;
      this.frame  = frame;
   }
   private HCodeFactory parent;
   private Frame frame;
   private boolean trace = false;
}
