// StackInfo.java, created Mon Aug 21 21:02:54 EDT 2000 by witchel
// Copyright (C) 2000 Emmett Witchel <witchel@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Util.Util;
import harpoon.Temp.TempList;
import harpoon.Temp.Temp;
import harpoon.Temp.LabelList;
import harpoon.Temp.Label;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.PreciselyTyped;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.ExpList;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.PrintStream;

/** 
 * This class encapsulates information about a given stack frame,
 * e.g., does it grow up or down, where is the return address stored,
 * that sort of thing.
 * <p>
 * INPUT
 * <p>
 * <code>callInfo</code> is called up front for every call made by
 * this frame.  This information is necessary for construction of the
 * MIPS stack frame, but it is not necessary for conventions that use a
 * non-virtual frame pointer.
 * <code>regAllocUsedRegs</code> is called after register allocation
 * to give the stackinfo object knowledge of which callee saved
 * registers were used.
 * <code>regAllocLocalWords</code> is called after the register
 * allocation to give the stackinfo object knowledge of how many temps
 * the spill code needs.  It would be nice to give the types of the
 * temps to the stackinfo object and let it map temp index to registers
 * and stack offset, but that is not the current interface to the spill
 * information. 
 * <p>
 * OUTPUT
 * <P>
 * Lots of useful offsets.
 * The multi-word (or multi-stack slot) temp support is currently
 * limited to two word temps.  It is ugly, but its not clear that
 * generalizing the solution is a good thing at this stage.
 */

public class StackInfo {
   // Values so the stack object can tell the argument loading code
   // where to load arguments
   public static final int REGISTER      = 0;
   public static final int STACK         = 1;
   public static final int REGSTACKSPLIT = 2;
   // Every stack frame is aligned on a 32 byte boundary to facilitate
   // the tag unchecked load/store optimization
   public static final int BYTEALIGNMENT = 32;

   private class CallInfo {
      public CallInfo() {
         // 8 entries should be sufficient for most calls.
         arg2word = new ArrayList(8);
      }
      public void setArg2Word(int arg, int word) {
         arg2word.add(new Integer(word));
         assert arg == arg2word.size() - 1;
      }
      public int getArg2Word(int arg) {
         assert arg >= 0 && arg < arg2word.size();
         Integer i = (Integer)arg2word.get(arg);
         return i.intValue();
      }
      public int getArg2StackSlot(int arg) {
         Integer i = (Integer)arg2word.get(arg);
         return word2StackWords(i.intValue());
      }
      public int nArgs() {
         // Array has extra entry at the end so we know how big the
         // last arg is
         return arg2word.size() - 1;
      }
      public void print(PrintStream ps) {
         ps.print(" len=" + arg2word.size());
         for(int i = 0; i < arg2word.size(); ++i) {
            ps.print(" a" + i + "=" + ((Integer)arg2word.get(i)).intValue());
         }
         ps.println("");
      }

      private ArrayList arg2word;
      private int word2StackWords(int w) {
         assert w >= 0;
         if(w < NARGREGS) return 0;
         return w - NARGREGS;
      }
   }

   /**
    * Each new frame should create a new stack info object.
    */
   public StackInfo(RegFileInfo _regfile) {
      max_arg_words = 0;
      locals_done = callee_done = false;
      regfile = _regfile;
      fixed_words = 2;           // fp and ra
      inv2info = new HashMap(5);
   }

   /**
    * Argument build are is the lowest region on the mips stack,
    * starting at 0(sp)
    */
   public int argOffset(Object inv, int arg_idx) {
      CallInfo ci = (CallInfo)inv2info.get(inv);
      if(trace >= TRACE_FUNCTION) {
         psout.print("a" + arg_idx + " " 
                     + (REGSIZE*ci.getArg2Word(arg_idx)) + "(sp)");
      }
      return REGSIZE * ci.getArg2Word(arg_idx);
   }
   /**
    * Give a higher offset.  If you are big endian, this is the least
    * signifigant word.
    */
   public int argSecondOffset(Object inv, int arg_idx) {
      CallInfo ci = (CallInfo)inv2info.get(inv);
      if(trace >= TRACE_FUNCTION) {
         psout.print("alow" + arg_idx + " " 
                     + (REGSIZE*(ci.getArg2Word(arg_idx)+1)) + "(sp)");
      }
      assert ci.getArg2Word(arg_idx) + 1 < ci.getArg2Word(arg_idx + 1);
      return REGSIZE * (ci.getArg2Word(arg_idx) + 1);
   }
   /**
    * Call this to find out if a given argument is to be passed in a
    * register, on the stack, or (for multi-word temporaries) split
    * between register and stack.
    */
   public int argWhere(Object inv, int arg_idx) {
      CallInfo ci = (CallInfo)inv2info.get(inv);
      if(trace >= TRACE_FUNCTION) {
         psout.print("WHERE ");
         if(inv instanceof INVOCATION) {
            print(psout, (INVOCATION)inv);
         } else {
            assert inv instanceof METHOD;
            print(psout, (METHOD)inv);
         }
         ci.print(psout);
         psout.print("a" + arg_idx);
      }
      if(ci.getArg2Word(arg_idx) < NARGREGS) {
         if(trace >= TRACE_FUNCTION) psout.println(" REG");
         return REGISTER;
      }
      if(trace >= TRACE_FUNCTION) psout.println(" STK");
      return STACK;
   }
   private int argWhereInternal(Object inv, int arg_idx) {
      int old_trace = trace;
      int ret;
      trace = TRACE_NONE;
      ret = argWhere(inv, arg_idx);
      trace = old_trace;
      return ret;
   }

   /**
    * Return which argument register this argument goes in
    */
   public Temp argReg(Object inv, int arg_idx) {
      assert argWhereInternal(inv, arg_idx) == REGISTER;
      CallInfo ci = (CallInfo)inv2info.get(inv);
      return idx2ArgReg(ci.getArg2Word(arg_idx));
   }
   /**
    * Return the second argument register for a two word temporary.
    */
   public Temp argSecondReg(Object inv, int arg_idx) {
      assert argWhereInternal(inv, arg_idx) == REGISTER;
      CallInfo ci = (CallInfo)inv2info.get(inv);
      assert ci.getArg2Word(arg_idx) + 1 < ci.getArg2Word(arg_idx + 1);
      return idx2ArgReg(ci.getArg2Word(arg_idx) + 1);
   }

   /**
    * Functions with the <code>regAlloc</code> prefix are functions
    * that take information  about register allocation to fill out our
    * model of the stack 
    */

   /**
    * Give the stack frame model the array of used callee saved
    * registers.  There should be no duplicates, and you probably want
    * to sort the list (it probably should be a LinearSet)
    */
   public void regAllocUsedRegs(ArrayList used) {
      callee_regs = new ArrayList(used);
      callee_regs.retainAll(regfile.calleeSave());
      // These are always dealt with, we don't need extra stack space
      // for them
      callee_regs.remove(regfile.SP);
      callee_regs.remove(regfile.FP);
      callee_done = true;
   }
   /**
    * Return how many callee saved registers there are for this frame
    */
   public int calleeSaveTotal() {
      assert callee_done;
      return callee_regs.size();
   }
   /**
    * Return the register for this callee saved register index
    */
   public Temp calleeReg(int callee_idx) {
      assert callee_done;
      assert callee_idx < callee_regs.size() && callee_idx >= 0 : "Callee idx=" + callee_idx + " Size=" + callee_regs.size();
      return (Temp)callee_regs.get(callee_idx);
   }
   /**
    * Return the offset for a given callee saved register index.
    */
   public int calleeSaveOffset(int callee_idx) {
      assert callee_done;
      assert callee_idx < callee_regs.size() && callee_idx >= 0;
      return frameSize() + fp_off - (REGSIZE * (callee_idx + 1));
   }
   /**
    * On top (highest address) of the MIPS stack frame are the
    * locals/temporaries This isn't a 
    * great interface, but it is the info we get from RegAlloc.  I
    * would have expected somethine like  an ExpList, just like arguments.
    */
   public void regAllocLocalWords(int w) {
      locals_done = true;
      local_words = w;
   }
   /**
    * Return the offset for a given local index
    */
   public int localSaveOffset(int local_idx) {
      assert callee_done && locals_done;
      assert local_idx < local_words && local_idx >= 0;
      return frameSize() + fp_off - (REGSIZE * (callee_regs.size() + 1 + 
                                                local_idx));
   }
   public int getFPOffset() {
      return frameSize() + fp_off;
   }
   public int getRAOffset() {
      return frameSize() + ra_off;
   }
   public int frameSize() {
      assert locals_done && callee_done;
      int fs = REGSIZE * (max_arg_words 
                          + callee_regs.size()
                          + local_words
                          + fixed_words);
      if(fs/BYTEALIGNMENT * BYTEALIGNMENT == fs)
         return fs;
      else
         return (fs/BYTEALIGNMENT + 1) * BYTEALIGNMENT;
   }
   
   /**
    * Call <code>callInfo</code> with the destination label and argument list
    * for all calls made by this activation frame.  The stack then
    * knows about every function it has to call.
    */
   public void callInfo(INVOCATION inv) {
      ExpList elist = inv.getArgs();
      int words = 0;
      CallInfo ci = new CallInfo();
      int narg = 0;
      if(trace >= TRACE_INTERNAL_CALLS) {
         print(psout, inv);
      }
      for(; elist != null; ++narg, elist = elist.tail) {
         // Like gcc we don't break doubles into one
         // register and one stack word 
         if(words == NARGREGS - 1 && nWords(elist.head) == 2) {
            words ++;
         }
         ci.setArg2Word(narg, words);
         if(trace >= TRACE_INTERNAL_CALLS) {
            psout.print(" a" + narg + "=" + words);
         }
         words += nWords(elist.head);
      }
      // Set the value after the last entry so we know the total number
      ci.setArg2Word(narg, words);
      if(trace >= TRACE_INTERNAL_CALLS) {
         psout.print(" a" + narg + "=" + words);
         psout.println(" END");
      }
      // XXX is this right? If we need any params, leave at least 16 bytes.
      // If this is the largest call we have seen, then increase max_arg_words
      if(words > max_arg_words)
         if(words < 4)
            max_arg_words = 4;
         else
            max_arg_words = words;
      inv2info.put(inv, ci);
   }
   /**
    * For callee argument information call <code>callInfo</code> with
    * METHOD object, and we will interpret the parameter list.
    */
   public void callInfo(METHOD meth) {
      // This code duplicates the algorithm from the INVOCATION
      // version which annoys me, but merging them would also be
      // annoying because of the differences between INVOCATION and
      // METHOD 
      CallInfo ci = new CallInfo();
      int words = 0;
      if(trace >= TRACE_INTERNAL_CALLS) {
         print(psout, meth);
      }
      // skip param[0], which is the explicit 'exceptional return
      // address'
      int narg = 0;
      for(int param_narg = 1; param_narg < meth.getParamsLength(); 
          ++param_narg) {
         // Like gcc we don't break doubles into one
         // register and one stack word 
         if(words == NARGREGS - 1 
            && nWords(meth.getParams(param_narg)) == 2) {
            words ++;
         }
         ci.setArg2Word(narg, words);
         if(trace >= TRACE_INTERNAL_CALLS) {
            psout.print(" Ma" + narg + "=" + words);
         }
         words += nWords(meth.getParams(param_narg));
         narg++;
      }
      // Set the value after the last entry so we know the total number
      ci.setArg2Word(narg, words);
      if(trace >= TRACE_INTERNAL_CALLS) {
         psout.print(" Ma" + narg + "=" + words);
         psout.println(" MEND");
      }
      inv2info.put(meth, ci);
   }


   private final int NARGREGS = 4; // how many arg regs
   private final int REGSIZE  = 4; // bytes per reg
   private final int ra_off   = -4;
   private final int fp_off   = -8;
   
   private int max_arg_words;
   private HashMap inv2info;
   private RegFileInfo regfile;
   private boolean locals_done;
   private int local_words;
   private boolean callee_done;
   private final int TRACE_NONE           = 0;
   private final int TRACE_FUNCTION       = 1;
   private final int TRACE_INTERNAL_CALLS = 2;
   private int trace = TRACE_NONE;
   private final PrintStream psout = System.out;
   private ArrayList callee_regs;
   private int fixed_words;
   private void print(PrintStream ps, INVOCATION inv) {
      if(inv.getFunc() instanceof NAME)
         ps.print("func=" + (NAME)inv.getFunc());
      else if(inv.getFunc() instanceof MEM)
         ps.print("func=" + (MEM)inv.getFunc());
      else
         ps.print("Ufunc=" + inv.getFunc());
   }
   private void print(PrintStream ps, METHOD meth) {
      ps.print("meth=" + meth.toString());
   }

   private Temp idx2ArgReg(int idx) {
      assert idx < NARGREGS;
      switch(idx) {
      case 0: return regfile.A0;
      case 1: return regfile.A1;
      case 2: return regfile.A2;
      case 3: return regfile.A3;
      }
      assert false;
      return null;
   }
   private int  nWords(Typed ty) {
      switch (ty.type()) {
      case Type.LONG: case Type.DOUBLE: return 2;
      default: return 1;
      }
   }
}
