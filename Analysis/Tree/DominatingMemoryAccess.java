// DominatingMemoryAccess.java, created Fri Oct 27 16:33:24 2000 by witchel
// Copyright (C) 2001 Emmett Witchel <witchel@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.IR.Properties.CFGrapher;

import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CONST;
// harpoon.IR.Tree.TEMP are tree temps, basically all unique
// Closer to a virtual register 
// CSA Note: actually, harpoon.Temp.Temp's are exactly virtual registers,
//           Tree.TEMP is just a wrapper object to make an lvalue/rvalue
//           out of the Temp.
import harpoon.IR.Tree.TEMP;
import harpoon.Temp.Temp;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.Util.Util;
import harpoon.Util.Collections.BitSetFactory;

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
import java.util.ArrayList;

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
 * @author  Emmett Witchel <witchel@mit.edu>
 * @version $Id: DominatingMemoryAccess.java,v 1.1.2.16 2002-02-06 17:48:05 witchel Exp $
 */
public class DominatingMemoryAccess {

/** Solves data flow equations for live variables.
    This version relies on the fact that there is at most one MEM per
    Stm, which is guaranteed by the MemHoisting pass.
    The in/out sets hold MEMs, but we traverse Stms.
 */
   class Live {

      private void doOne(Stm root, Tree m) {
         for(Tree t = m.getFirstChild(); t != null; t = t.getSibling()) {
            doOne(root, t);
         }
         if(m.kind() == TreeKind.MEM) {
            Util.assert(stmToMem.containsKey(root) == false,
                        "******* Stm*" 
                        + harpoon.IR.Tree.Print.print(root) 
                        + "\n******* tree* " 
                        + harpoon.IR.Tree.Print.print(m));
            Util.assert(memToStm.containsKey(m) == false);
            stmToMem.put(root, m);
            memToStm.put(m, root);
         }
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
      private void initStmMemMaps(final CFGrapher cfgr, final HCode code) {
         // Build the stm <-> MEM maps
         for(Iterator it = cfgr.getElements(code).iterator(); it.hasNext(); ) {
            Stm stm = (Stm) it.next();
            Util.assert(stm != null);
            doOne(stm, (Tree)stm);
         }
      }
      private void init(final CFGrapher cfgr, final HCode code) {
         stmToMem = new HashMap();
         memToStm = new HashMap();
         initStmMemMaps(cfgr, code);
         HashSet universe = new HashSet(defUseMap.keySet());
         universe.addAll(useDefMap.keySet());
         this.bsf = new BitSetFactory (universe);
         init_inout(cfgr.getElements(code));
      }
      public Live(CFGrapher cfgr, HCode code, 
                  Map _defUseMap, Map _useDefMap) {
         this.defUseMap = _defUseMap;
         this.useDefMap = _useDefMap;
         init(cfgr, code);
         boolean change;
         Set inbs;
         Set outbs;
         int pass = 0;
         do {
            change = false;
            if(trace) {
               System.err.println("pass " + pass);
            }
            for(Iterator it = cfgr.getElements(code).iterator();it.hasNext();) {
               HCodeElement hce = (HCodeElement) it.next();
               inbs  = bsf.makeSet(out(hce));
               outbs = union_out(cfgr, hce);
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
         if(stmToMem.containsKey(hce)) {
            MEM m = (MEM)stmToMem.get(hce);
            if(useDefMap.containsKey(m)) {
               HCodeElement dom = (HCodeElement)useDefMap.get(m);
               Util.assert(defUseMap.containsKey(dom));
               Util.assert(memToStm.containsKey(dom));
               return dom;
            }
         }
         return null;
      }
      public HCodeElement def(HCodeElement hce) {
         if(stmToMem.containsKey(hce)) {
            MEM m = (MEM)stmToMem.get(hce);
            if(defUseMap.containsKey(m)) {
               return m;
            }
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

      private Set union_out(final CFGrapher cfgr, HCodeElement hce) {
         Collection succ = cfgr.succElemC(hce);
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
      // Map from Stm to enclosing MEM and back
      private Map stmToMem;
      private Map memToStm;
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
         ArrayList regs = new ArrayList(maxRegs);
         // Don't shuffle in register 0, stick it on the end
         for(int i = 1; i < maxRegs; ++i) {
            regs.add(new Integer(i));
         }
         java.util.Random rand = new java.util.Random(seed++);
         // Shuffle the register numbers
         for(int i = 0; i < 70; ++i) {
            int idx = rand.nextInt()% (maxRegs - 1);
            // signed only in java
            idx = idx < 0 ? -idx : idx;
            regs.add(regs.remove(idx));
         }
         // Allocate reg 0 last since it is used in function entry/exit
         regs.add(new Integer(0));
            
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
            for(Iterator rit = regs.iterator(); rit.hasNext(); ) {
               Integer danum = (Integer)rit.next();
               // If all the danums are taken, just don't assign this
               // one a da reg.  No spilling.
               if(takenDA.contains(danum) == false) {
                  Util.assert(ref2dareg.containsKey(hce) == false);
                  int i = danum.intValue();
                  // There can't be a single use and def of a DA regstier
                  daNum defda = new daNum(i, true);
                  daNum useda = new daNum(i, false);
                  usedDANum.add(danum);
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
      DARegAlloc(CFGrapher cfgr, HCode code, Live _live, 
                 Map _defUseMap, Map _useDefMap) {
         nodes = cfgr.getElements(code);
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
               CFGrapher cfgr = code.getGrapher();
               // Scott's neato analysis
               CacheEquivalence cacheEq = new CacheEquivalence(code, ch);
               Map defUseMap = new HashMap();
               Map useDefMap = new HashMap();
               findDADefUse(code.getElements(), cacheEq,defUseMap, useDefMap);
               Live live = new Live(cfgr, code, defUseMap, useDefMap);
               alloc = new DARegAlloc(cfgr, code, live, defUseMap, useDefMap);
               Map ref2dareg = alloc.getRef2Dareg();
               ((harpoon.Backend.MIPS.Frame)frame).setNoTagCheckMap(new HashMap(ref2dareg));
               ((harpoon.Backend.MIPS.Frame)frame).setUsedDANum(new HashSet(alloc.usedDANum()));
               if(print_decorated_graph)
                  printDA(code, live, defUseMap, useDefMap, ref2dareg);
               if(static_stats)
                  report_stats(code, live, defUseMap, useDefMap, alloc);

               live = null;
               cacheEq = null;
               cfgr = null;
               alloc = null;
               defUseMap = useDefMap = null;
               return hc;
            }
            public String getCodeName() { return parent.getCodeName(); }
            public void clear(HMethod m) { parent.clear(m); }
         };
   }

   public DominatingMemoryAccess(final HCodeFactory parent, 
                                 final Frame frame, final ClassHierarchy ch) {
      this.parent = parent;
      this.frame  = frame;
      this.ch = ch;
   }

   private static int seed = 8675309;
   private HCode hc;
   private HCodeFactory parent;
   private Frame frame;
   private ClassHierarchy ch;
   private DARegAlloc alloc;
   private boolean trace = false;
   // This is more useful than trace.
   private boolean print_decorated_graph = false;
   // If true print some stats about how many uses per def
   private boolean static_stats = false;
}

