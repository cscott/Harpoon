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
import harpoon.Analysis.Tree.CacheEquivalence;
import harpoon.Util.Util;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Backend.Generic.Frame;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.BitSet;
import java.util.List;
import java.util.Comparator;
import java.util.Collection;

/**
 * <code>DominatingMemoryAccess</code> is an analysis that uses
 information about memory operations to determine which of them need
 to do cache tag checks, and which don't.  If one access dominates
 another, and they are provably on the same cache line, we do not need
 to check the later's tag.

 We assume that the system allocator does not break objects over cache
 lines.

 We assign direct address register numbers to the most important
 references.  The defining access set the register to point to the
 cache location of the data, and the using accesses, um, use the data
 at that cache location.  This is a standard register allocation
 problem, but we 
 don't spill, since the contents of a direct address register is a
 hardware decoded cache location which is kept consistent in the face
 of replacements by the hardware.  To restore an old value of the
 register would potentially allow a process access into protected
 data.  Anyway, there is no CPU interface to get at the contents of
 direct address registers for precisely the reason that it shouldn't
 use them.

 The backend needs to know which direct address register numbers we
 used since it needs to invalidate them on a function return.
 * 
 * @author  Emmett Witchel <witchel@lcs.mit.edu>
 * @version $Id: DominatingMemoryAccess.java,v 1.1.2.9 2001-06-12 01:19:17 witchel Exp $
 */
public class DominatingMemoryAccess {

   // An extention to CFGrapher that deals with MOVEs the way I need
   // them to be dealt with.  Namely, control flows from the outer
   // MOVE to the src, and then to the dst where it flows to the
   // regular CFGrapher successor of the move.
   public class MoveCFGrapher extends CFGrapher {

      private void collectElts(harpoon.ClassFile.HCodeElement hce, 
                               final CFGrapher cfgr) {
         Tree tr = (Tree)hce;
         MOVE m = null;
         // MOVE -> src -> dst -> edges out of MOVE
         if(tr.kind() == TreeKind.MOVE) {
            m = (MOVE)tr;
            nodes.add(m.getSrc());
            nodes.add(m.getDst());
            edges.put(m, new HashSet());
            edges.put(m.getSrc(), new HashSet());
            edges.put(m.getDst(), new HashSet());
            ((Set)edges.get(m)).add(m.getSrc());
            ((Set)edges.get(m.getSrc())).add(m.getDst());
         }
         nodes.add(hce);
         HCodeEdge[] edge = cfgr.succ(hce);
         for(int i = 0; i < edge.length; ++i) {
            if(m != null) {
               ((Set)edges.get(m.getDst())).add(edge[i].to());
            }
            if(nodes.contains(edge[i].to()) == false)
               collectElts(edge[i].to(), cfgr);
         }
      }
      private void init(final CFGrapher cfgr, final HCode code) {
         HCodeElement[] hcroots = cfgr.getFirstElements(code);
         for(int i = 0; i < hcroots.length; ++i) {
            collectElts(hcroots[i], cfgr);
         }
      }

      public MoveCFGrapher(harpoon.IR.Tree.Code code) {
         cfgr = code.getGrapher();
         nodes = new HashSet();
         edges = new HashMap();
         init(cfgr, code);
      }
      public HCodeElement getFirstElement(HCode hcode) {
         return cfgr.getFirstElement(hcode);
      }
      public HCodeElement[] getFirstElements(HCode hcode) {
         return cfgr.getFirstElements(hcode);
      }
      public HCodeElement[] getLastElements(HCode hcode) {
         return cfgr.getLastElements(hcode);
      }
      public Collection predC(HCodeElement hc) {
         Util.assert(false);
         return null;
      }
      public Collection succC(HCodeElement hc) {
         Util.assert(false);
         return null;
      }

      public Set succS(HCodeElement hce) {
         Util.assert(nodes.contains(hce), "hce " + hce 
                     + " was not part of original graph");
         Set succ;
         if(edges.containsKey(hce)) {
            succ = (Set)edges.get(hce);
         } else {
            succ = new HashSet();
            HCodeEdge[] edges = cfgr.succ(hce);
            Util.assert(edges != null);
            for(int i = 0; i < edges.length; ++i) {
               succ.add(edges[i].to());
            }
         }
         return succ;
      }
      public Set nodes() {
         return new HashSet(nodes);
      }

      private Set nodes;
      private CFGrapher cfgr;
      private Map edges;
   }
/** Solves data flow equations for live variables
 */
   class Live {
      // Buid a version of the graph
      private void collectElts(harpoon.ClassFile.HCodeElement hce, 
                               final MoveCFGrapher mcfgr,
                               Set nodes) {
         nodes.add(hce);
         Set edge = mcfgr.succS(hce);
         for(Iterator it = edge.iterator(); it.hasNext();) {
            HCodeElement succ = (HCodeElement) it.next();
            if(nodes.contains(succ) == false) {
               collectElts(succ, mcfgr, nodes);
            }
         }
      }

      private void init(final MoveCFGrapher mcfgr, final HCode code,
                        Set nodes) {
         HCodeElement[] hcroots = mcfgr.getFirstElements(code);
         for(int i = 0; i < hcroots.length; ++i) {
            collectElts(hcroots[i], mcfgr, nodes);
         }
         this.bsf = new BitSetFactory (nodes);
      }
      // Not strictly necessary, but then in/out is never null
      private void init_inout(Set nodes) {
         this.in  = new HashMap(nodes.size());
         this.out = new HashMap(nodes.size());
         for(Iterator it = nodes.iterator(); it.hasNext();) {
            HCodeElement hce = (HCodeElement) it.next();
            in.put(hce, bsf.makeSet());
            out.put(hce, bsf.makeSet());
         }
      }
      public Live(MoveCFGrapher mcfgr, HCode code,
                  Map _defUseMap, Map _useDefMap) {
         Set nodes = new HashSet();
         init(mcfgr, code, nodes);
         this.defUseMap = _defUseMap;
         this.useDefMap = _useDefMap;
         init_inout(nodes);
         boolean change;
         Set inbs;
         Set outbs;
         int pass = 0;
         do {
            change = false;
            if(trace) {
               System.err.println("pass " + pass);
            }
            for(Iterator it = nodes.iterator();
                it.hasNext();) {
               HCodeElement hce = (HCodeElement) it.next();
               inbs  = bsf.makeSet(out(hce));
               outbs = union_out(mcfgr, hce);
               if(def(hce) != null)
                  inbs.remove(def(hce));
               if(use(hce) != null) {
                  if(trace)
                     System.err.println("USE " + use(hce));
                  inbs.add(use(hce));
               }
               if(trace) {
                  if(!inbs.equals(in(hce))) {
                     System.err.println(hce.hashCode()
                                        + " inbs "
                                        + in(hce).toString()
                                        + " -> "
                                        + inbs.toString());
                  }
                  System.err.println(hce.hashCode()
                                     + " outbs "
                                     + out(hce).toString()
                                     + " -> "
                                     + outbs.toString());
               }
               change = change 
                  || !inbs.equals(in(hce))
                  || !outbs.equals(out(hce));
               out.put(hce, outbs);
               in.put(hce, inbs);
            }
            pass++;
         } while(change);
      }


      ////////////////////////////////////////////////////////////
      // Query functions
      public HCodeElement use(HCodeElement hce) {
         if(useDefMap.containsKey(hce)) {
            HCodeElement dom = (HCodeElement)useDefMap.get(hce);
            Util.assert(defUseMap.containsKey(dom));
            return dom;
         }
         return null;
      }
      public HCodeElement def(HCodeElement hce) {
         if(defUseMap.containsKey(hce)) {
            return hce;
         }
         return null;
      }
      public Set in(HCodeElement hce) {
         if(in.containsKey(hce) == true) {
            return ((Set)in.get(hce));
         }
         //Util.assert(false);
         return null;
      }
      public Set out(HCodeElement hce) {
         if(out.containsKey(hce) == true) {
            return ((Set)out.get(hce));
         }
         //Util.assert(false);
         return null;
      }

      private Set union_out(final MoveCFGrapher mcfgr, HCodeElement hce) {
         Set succ = mcfgr.succS(hce);
         Set bs = bsf.makeSet();
         if(trace) {
            System.err.print(" union out " 
                             + hce.hashCode()
                             + " -> ");
            if(succ.isEmpty()) {
               System.err.print("\n");
            }
         }
         for(Iterator it = succ.iterator(); it.hasNext(); ) {
            HCodeElement suck = (HCodeElement)it.next();
            if(trace)
               System.err.println( suck.hashCode()
                                   + " " + in(suck));
            bs.addAll(in(suck));
         }
         return bs;
      }
      
      private Map in;
      private Map out;
      private Map useDefMap;
      private Map defUseMap;
      private harpoon.Util.Collections.BitSetFactory bsf;
      private static final boolean trace = false;
   }

   public class daNum {
      public daNum(int val, boolean def) {
         this.val = val;
         this.def = def;
      }
      public boolean isDef() {
         return def;
      }
      public boolean isUse() {
         return !def;
      }
      public int num() {
         return val;
      }
      public int val() {
         return val;
      }
      private int val;
      private boolean def;
   }

   /** Use live range information to compute interference graph and
       allocate DA registers.
    */
   class DARegAlloc {

      // All DA variables that are simultaneously live interfere with
      // each other
      private void addInter(Map inter, Set in) {
         for(Iterator it = in.iterator(); it.hasNext();) {
            HCodeElement me = (HCodeElement) it.next();
            Util.assert(defUseMap.containsKey(me));
            Set interset = (Set)inter.get(me);
            if(interset == null) {
               interset = new HashSet();
               inter.put(me, interset);
            }
            for(Iterator it2 = in.iterator(); it2.hasNext();) {
               HCodeElement him = (HCodeElement) it2.next();
               if(!me.equals(him)) {
                  interset.add(him);
               }
            }
         }
      }

      // At each program point, find the set of live DA
      // variables--that is the interference set for this program
      // point.  Each of these 
      // point in the interference set.
      // This is an interference graph
      private Map interfereClasses() {
         Map inter = new HashMap(nodes.size()/4);
         for(Iterator it = nodes.iterator(); it.hasNext(); ) {
            HCodeElement node = (HCodeElement)it.next();
            addInter(inter, live.in(node));
            addInter(inter, live.out(node));
         }
         return inter;
      }
      private void removeEdgesTo(HCodeElement hce, Map interGrph) {
         for(Iterator it = interGrph.values().iterator(); it.hasNext();) {
            Set interset = (Set)it.next();
            interset.remove(hce);
         }
      }
      /** 
          The score is the number of tag checks this reference
          provides, minus the numer of tag checks it prevents by
          disallowing the references from all of the members of its
          interference set.  This is not accurate, since it doesn't
          necessarily prevent all of the member of the interference
          set from being colored, but it seems like a good heuristic.
       */
      private int score(HCodeElement hce, Set interset) {
         Util.assert(defUseMap.containsKey(hce));
         int provides = ((Set)defUseMap.get(hce)).size();
         int prevents = 0;
         for(Iterator it = interset.iterator(); it.hasNext();) {
            HCodeElement inter = (HCodeElement)it.next();
            prevents += ((Set)defUseMap.get(inter)).size();
         }
         return provides - prevents;
      }
      /** Do traditional simplification
       */
      // This dismantles the interGrph data structure
      private void simplify(Map interGrph, Stack colorable) {
        // For each iteration, find the 
         boolean done = true;
         Set nodes = interGrph.keySet();
         do {
            done = true;
            boolean simpWorked;
            do {
               simpWorked = false;
               // Read-only copy for iteration
               Set nodesRO = new HashSet(nodes);
               for(Iterator it = nodesRO.iterator();it.hasNext();) {
                  HCodeElement hce = (HCodeElement)it.next();
                  Set interset = (Set)interGrph.get(hce);
                  if(interset.size() < maxRegs) {
                     simpWorked = true;
                     colorable.push(hce);
                     nodes.remove(hce);
                     removeEdgesTo(hce, interGrph);
                  }
               }
            } while (simpWorked);
            // When we get stuck, find the "spill" that is least
            // attractive, then restart the simplification process
            int lowest_score = 10000;
            HCodeElement spillme = null;
            for(Iterator it = nodes.iterator(); it.hasNext();) {
               HCodeElement hce = (HCodeElement)it.next();
               Set interset = (Set)interGrph.get(hce);
               if(interset.size() > maxRegs) {
                  done = false;
                  int score = score(hce, interset);
                  if(lowest_score > score) {
                     lowest_score = score;
                     spillme = hce;
                  }
               } else {
                  Util.assert(interset.size() == 0);
               }
            }
            if(spillme != null) {
               colorable.push(spillme);
               nodes.remove(spillme);
               removeEdgesTo(spillme, interGrph);
            }
         } while( !done );
      }
      private void select(Map interGrph, Stack interNodes) {
         while(!interNodes.empty()) {
            HCodeElement hce = (HCodeElement)interNodes.pop();
            Set interset = (Set)interGrph.get(hce);
            Set takenDA = new HashSet();
            for(Iterator it = interset.iterator(); it.hasNext();) {
               HCodeElement inter = (HCodeElement)it.next();
               if(ref2dareg.containsKey(inter)) {
                  takenDA.add(new Integer(((daNum)ref2dareg.get(inter)).num()));
               }
            }
            for(int i = 0; i < maxRegs; ++i) {
               // There can't be a single use and def of a DA regstier
               daNum defda = new daNum(i, true);
               daNum useda = new daNum(i, false);
               Integer danum = new Integer(i);
               usedDANum.add(danum);
               // If all the danums are taken, just don't assign this
               // one a da reg.  No spilling.
               if(takenDA.contains(danum) == false) {
                  Util.assert(ref2dareg.containsKey(hce) == false);
                  ref2dareg.put(hce, defda);
                  for(Iterator it = ((Set)defUseMap.get(hce)).iterator(); 
                      it.hasNext();) {
                     HCodeElement use = (HCodeElement) it.next();
                     Util.assert(ref2dareg.containsKey(use) == false);
                     ref2dareg.put(use, useda);
                  }
                  break;
               }
            }
         }
      }
      public Set usedDANum() {
         return usedDANum;
      }
      DARegAlloc(MoveCFGrapher mcfgr, Live _live, 
                 Map _defUseMap, Map _useDefMap) {
         nodes = mcfgr.nodes();
         live = _live;
         defUseMap = _defUseMap;
         useDefMap = _useDefMap;
         ref2dareg = new HashMap(nodes.size()/4);
         usedDANum = new HashSet();
         // Register allocation algorithm is inspired by Appel "Modern
         // compiler implementation in Java" Chapter 11 -- Register
         // Allocation.  We modify it because we have a clear metric
         // of usefulness--number of tag checks eliminated, and we
         // have no notion of spilling.
         Map interGrph = interfereClasses();
         Stack interNodes = new Stack();
         simplify(new HashMap(interGrph), interNodes);
         select(interGrph, interNodes);
      }
      public boolean isDef(HCodeElement hce) {
         return defUseMap.containsKey(hce);
      }
      public Map getRef2Dareg() {
         return ref2dareg;
      }

      private final int maxRegs = 8;
      private Live live;
      private Set nodes;
      private Map defUseMap;
      private Map useDefMap;
      private Map ref2dareg;
      private Set usedDANum;
      private static final boolean trace = false;
   }


   /** Memory equivalence classes.  Each class corresponds to a unique
   /** base pointer.
    */
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
      public Map getClasses() {
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
               System.err.println(cl + "\tgets hce " + hce + "\thcecode=" 
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
         int sz = getSize(hce, T);
         EqClass newcl = new EqClass(++class_num, sz);
         if(trace) {
            System.err.println(newcl + " gets hce " + hce + " hcecode=" 
                               + hce.hashCode());
         }
         classes.put(hce, newcl);
         Util.assert(currtemps.containsKey(t) == false);
         currtemps.put(t, newcl);
      }

      private Map currtemps;
      private Map classes;
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
                     System.err.println("ADDING TO A PTR -- " + m 
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
                            EqClasses eqClasses, Map visited) {
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
                               Map active, Map defUseMap, Map useDefMap) {
      EqClasses.EqClass eqc = eqClasses.getClass(hce);
      if(eqc != null) {
         if(active.containsKey(eqc)) {
            // This is it, this use is dominated
            if(eqc.useNoTagCheck(0)) {
               if(trace) {
                  System.err.println("hce " + hce.hashCode() + "not tag checked");
               }
               HCodeElement dom = (HCodeElement)active.get(eqc);
               Util.assert(defUseMap.containsKey(dom) == true);
               Set subs = (Set)defUseMap.get(dom);
               subs.add(hce);
               Util.assert(useDefMap.containsKey(hce) == false);
               useDefMap.put(hce, dom);
            }
         } else {
            active.put(eqc, hce);
            defUseMap.put(hce, new HashSet());
         }
      }
   }

   private int getSize(HCodeElement hce, TEMP T) {
      Temp t = T.temp;
      HClass hclass = ((CanonicalTreeCode)hc).getTreeDerivation().typeMap(T);
      int sz = 0;
      if(hclass == null) {
         // System.out.println("Yikes TEMP = " + T + " Temp=" + t + " hce " + hce + " hcecode=" + hce.hashCode());
         sz = 37; /* XXX */
      } else {
         sz = frame.getRuntime().treeBuilder.objectSize(hclass);
      }
      return sz;
   }
   // For every access that dominates another access to the same base
   // pointer (memory equiv class), make the dominating access the Def
   // and the subordinate accesses uses
   private void findDADefUse(DomTree dt, HCodeElement hce, 
                             harpoon.IR.Tree.Code code, CFGrapher cfgr,
                             final EqClasses eqClasses, Map active,
                             Map defUseMap, Map useDefMap) {
      HCodeElement[] children = dt.children(hce);
      switch(((Tree)hce).kind()) {
      case TreeKind.MOVE:
         MOVE m = (MOVE)hce;
         if(trace) {
            System.err.println("hce " + hce + " code " + hce.hashCode() + " (" + children.length + ") active " + active);
         }
         tryDominate(m.getSrc(), eqClasses, active, defUseMap, useDefMap);
         tryDominate(m.getDst(), eqClasses, active, defUseMap, useDefMap);
         break;
      case TreeKind.CALL:
      case TreeKind.NATIVECALL:
      case TreeKind.THROW:
         // Calls can clear DA state, but possibly not, so don't clear
         // the active list
      }
      for(int i = 0; i < children.length; ++i) {
         findDADefUse(dt, children[i], code, cfgr, eqClasses, 
                      new HashMap(active), defUseMap, useDefMap);
      }
   }

   // For every access that dominates another access to the same base
   // pointer (memory equiv class), make the dominating access the Def
   // and the subordinate accesses uses
   private void findDADefUse(HCodeElement[] elts,
                             final CacheEquivalence eqClasses, 
                             Map defUseMap, Map useDefMap) {
      for(int i = 0; i < elts.length; ++i) {
         HCodeElement hce = elts[i];
         if(((Tree)hce).kind() == TreeKind.MEM) {
            MEM mem = (MEM)hce;
            // XXX Someday soon this restriction will go away as large
            // objects will  be split into different eq classes at
            // this boundary.
            if(mem.getExp().kind() == TreeKind.TEMP
               && getSize(mem, (TEMP)mem.getExp()) <= 32) {
               if(eqClasses.needs_tag_check(mem)) {
                  if(eqClasses.num_using_this_tag(mem) > 1) {
                     // Then this is a def that is used
                     defUseMap.put(mem, eqClasses.ops_using_this_tag(mem));
                  }
               } else {
                  useDefMap.put(mem, eqClasses.whose_tag_check(mem));
               }
            }
         }
      }
   }

   private void printDA (harpoon.IR.Tree.Code code,
                         final Live live, 
                         final Map defUseMap,
                         final Map useDefMap,
                         final Map ref2dareg) {
      HCodeElement[] nodes = code.getElements();
      Map _h2n = new HashMap(nodes.length);
      for(int i = 0; i < nodes.length; ++i) {
         _h2n.put(nodes[i], new Integer(i));
      }
      final Map h2n = _h2n;
      harpoon.IR.Tree.Print.print(
         new java.io.PrintWriter(System.out), code, 
         new HCode.PrintCallback(){
               public void printAfter(java.io.PrintWriter pw, 
                                      HCodeElement hce) {
                  if(defUseMap.containsKey(hce)) {
                     pw.print(" NODE " + (Integer)h2n.get(hce));
                  }
                  if(useDefMap.containsKey(hce)) {
                     pw.print(" DOM BY " 
                              + (Integer)h2n.get(useDefMap.get(hce)));
                  }
                  if(ref2dareg.containsKey(hce)) {
                     daNum danum = (daNum)ref2dareg.get(hce);
                     pw.print(" DA ");
                     if(danum.isUse()) {
                        pw.print(" USE ");
                     } else {
                        pw.print(" DEF ");
                     }
                     pw.print(danum.num());
                  }
                  if(live.in(hce) != null) {
                     boolean notlive = true;
                     if(live.in(hce).size() > 0) {
                        notlive = false;
                        pw.print(" LIVE IN { ");
                        for(Iterator it = live.in(hce).iterator();
                            it.hasNext();) {
                           HCodeElement in = (HCodeElement)it.next();
                           pw.print(in.hashCode() + " ");
                        }
                        pw.print("}");
                     } 
                     if(live.out(hce).size() > 0) {
                        notlive = false;
                        pw.print(" LIVE OUT { ");
                        for(Iterator it = live.out(hce).iterator();
                            it.hasNext();) {
                           HCodeElement out = (HCodeElement)it.next();
                           pw.print(out.hashCode() + " ");
                        }
                        pw.print("}");
                     }
                     if(notlive) {
                        pw.print(" NO LIVE ");
                     }
                  }
               }
            }
         );
   }

   private void report_stats (harpoon.IR.Tree.Code code,
                         final Live live, 
                         final Map defUseMap,
                         final Map useDefMap,
                         final DARegAlloc alloc) {
      Map ref2dareg = alloc.getRef2Dareg();
      Set useda = alloc.usedDANum();
      int tot_defs = defUseMap.keySet().size();
      int tot_uses = 0;
      for(Iterator it = defUseMap.values().iterator(); it.hasNext();) {
         Set uses = (Set)it.next();
         tot_uses += uses.size();
      }
      float avg = tot_defs != 0 ? (float)tot_uses/tot_defs : 0;
      System.err.print(" DEF-USE " + tot_defs + "-" + tot_uses);
      if(avg != 0.0 && avg != 1.0) {
         System.err.print(" (" + avg + ")");
      }
      int alloc_def = 0;
      int alloc_use = 0;
      for(Iterator it = ref2dareg.values().iterator(); it.hasNext(); ) {
         daNum danum = (daNum)it.next();
         if(danum.isDef()) alloc_def++; else alloc_use++;
      }
      avg = alloc_def != 0 ? (float)alloc_use/alloc_def : 0;
      System.err.print(" REGDEF-USE " + alloc_def + "-" + alloc_use);
      if(avg != 0.0 && avg != 1.0) {
         System.err.print(" (" + avg + ")");
      }
      if(useda.size() > 1) {
         System.err.print(" ALLOC:");
         for(Iterator it = useda.iterator(); it.hasNext(); ) {
            Integer danum = (Integer)it.next();
            System.err.print(" " + danum);
         }
      }
      System.err.println("");

   }

   /**
      Returns true if the MEM operation that returned this specifier
      (from <code>harpoon.Backend.MIPS.Frame.daNum</code>) is the
      defining access. 
      A defining access does a tag check for the cache line it is on.
    */
   static public boolean isDef(Object _danum) {
      daNum danum = (daNum) _danum;
      return danum.isDef();
   }
   /**
      Returns true if the MEM operation that returned this specifier
      (from <code>harpoon.Backend.MIPS.Frame.daNum</code>) is a use.
      A use memory access can skip the tag check, since it has been
      done for this line.
    */
   static public boolean isUse(Object _danum) {
      daNum danum = (daNum) _danum;
      return danum.isUse();
   }
   /**
      Returns the direct address register used by the MEM operation
      that returned this specifier (from
      <code>harpoon.Backend.MIPS.Frame.daNum</code>).  A direct
      address register is 
      an on-cache register that points to a specific cache location.
      You can think of it as a cache line identifier.  A dominant
      accesses might do the tag check and set direct address register
      3.  A subordinate access can skip the tag check, and use direct
      address register 3 instead.  The direct address register number
      substitutes for the virtual address to identify the cache line.
    */
   static public int num(Object _danum) {
      daNum danum = (daNum) _danum;
      return danum.num();
   }

   /** Standard interface to run this analysis */
   public HCodeFactory codeFactory() {
      Util.assert(parent.getCodeName().equals(CanonicalTreeCode.codename));
      return new HCodeFactory() {
            public HCode convert(HMethod m) {
               hc = parent.convert(m);
               harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
               final CFGrapher cfgr = code.getGrapher();
               // My crapo analysis
                EqClasses eqClasses = new EqClasses(hc);
               doEqClasses((Tree)cfgr.getFirstElement(code), cfgr, eqClasses, 
                           new HashMap());
               if( trace ) {
                  harpoon.IR.Tree.Print.print(
                     new java.io.PrintWriter(System.out), code, eqClasses.getClasses());
               }
               // Scott's neato analysis
               CacheEquivalence cacheEq = new CacheEquivalence(code);
               Map defUseMap = new HashMap();
               Map useDefMap = new HashMap();
               DomTree dt = new DomTree(hc, cfgr, false);
               HCodeElement[] roots = dt.roots();
               for(int i = 0; i < roots.length; ++i) {
                  if(emmett_crappy) {
                     findDADefUse(dt, roots[i], code, cfgr, eqClasses, 
                                  new HashMap(), defUseMap, useDefMap);
                  } else {
                     findDADefUse(code.getElements(), cacheEq,
                                  defUseMap, useDefMap);
                  }
               }
               MoveCFGrapher mcfgr = new MoveCFGrapher(code);
               Live live = new Live(mcfgr, code, defUseMap, useDefMap);
               alloc = new DARegAlloc(mcfgr, live, defUseMap, useDefMap);
               Map ref2dareg = alloc.getRef2Dareg();
               ((harpoon.Backend.MIPS.Frame)frame).setNoTagCheckMap(ref2dareg);
               ((harpoon.Backend.MIPS.Frame)frame).setUsedDANum(alloc.usedDANum());
               if(trace)
                  printDA(code, live, defUseMap, useDefMap, ref2dareg);
               if(static_stats)
                  report_stats(code, live, defUseMap, useDefMap, alloc);

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

   private HCode hc;
   private HCodeFactory parent;
   private Frame frame;
   private DARegAlloc alloc;
   private boolean trace = false;
   private boolean static_stats = false;
   // If true, use Emmett's crappy memory analysis
   private boolean emmett_crappy = true;
}
