// BypassLatchSchedule.java, created Wed Sep 27 20:01:40 EDT 2000 by witchel
// Copyright (C) 1999 Emmett Witchel <witchel@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.BasicBlock;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Backend.Generic.RegUseDefer;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Properties.UseDefer;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <code>BypassLatchSchedule</code> is a transformation on low level,
 * register allocated assembly code that annotates opcode to indicate
 * if a given use is the last use of a register.  E.g.
 * add t0, t1, t2
 * xor t3, t0, t5
 * If t0 is not live out of xor, then we replace the xor instruction with
 * xor.l1 t3, t0, t5
 * which says that this use of operand 1 (RS) is the last use.  The
 * assembler will use this information to not write the t0 value
 * generated in the add instruction back to the register file.
 * @author  Emmett Witchel <witchel@lcs.mit.edu>
 * @version $Id: 
 */

public class BypassLatchSchedule {

    /** Returns a new <code>Instr</code> if this instr needs to be
     * modified.  Otherwise, it returns null.
     */
   private Instr lastUseInstr(Instr instr) {
      Set lb = lt.getLiveBefore(instr);
      Set la = lt.getLiveAfter(instr);
      Temp[] tmp_use = instr.use();
      Temp[] tmp_def = instr.def();
      String assem = instr.getAssem();
      // XXX omit compound instructions for now
      if(assem.indexOf('\n') != -1) return null;

      // Omit calls, and remnants of move coalescing
      if(tmp_use.length > 0
         && tmp_use.length <= 2
         && instr.def().length <= 1 /* excludes jal _lookup_handler */
         && assem.length() > 0
         ) {
         Util.assert(tmp_use.length <= 2, " tmp_use.len=" + tmp_use.length 
                     + " tmp_use=" + tmp_use + " isntr=" + instr);
         List reg_use0 = null;
         List reg_use1 = null;
         List reg_def  = tmp_def.length == 0 ? null
             : code.getRegisters(instr, tmp_def[0]);
         boolean oneLast = false;
         if(tmp_use.length > 0) {
            reg_use0 = code.getRegisters(instr, tmp_use[0]);
            oneLast = lb.containsAll(reg_use0) 
               && (la.containsAll(reg_use0) == false
                   || (reg_def != null && reg_use0.containsAll(reg_def)));
         }
         boolean twoLast = false;
         if(tmp_use.length > 1) {
            reg_use1 = code.getRegisters(instr, tmp_use[1]);
            twoLast = lb.containsAll(reg_use1) 
               && (la.containsAll(reg_use1) == false
                   || (reg_def != null && reg_use1.containsAll(reg_def)));
         }
         if(trace) {
            System.out.println(instr + "\n\tlb=" + lb + "\n\tla=" + la);
            if(tmp_def.length > 1) {
               System.out.print("\tdef multiple");
               for(int i = 0; i < tmp_def.length; ++i) {
                  System.out.print(" " + tmp_def[i] + " " 
                                   + code.getRegisters(instr, tmp_def[i]));
               }
               System.out.println();
            }
            if(tmp_def.length == 1) {
               System.out.println("\tdef " + tmp_def[0] + " " + reg_def);
            }
            if(tmp_use.length == 2) {
               System.out.println("\tuse(0) " + tmp_use[0] 
                                  + " " + reg_use0
                                  + " use(1) " + tmp_use[1]
                                  + " " + reg_use1
                  );
            } else if (tmp_use.length == 1) {
               System.out.println("\tuse(0) " + tmp_use[0] 
                                  + " " + reg_use0);
            }
         }
         if( oneLast || twoLast ) {
            int space = assem.indexOf(' ');
            String opcode = assem.substring(0, space);
            boolean reverse = false;
            if(opcode.startsWith("sw")
               || opcode.startsWith("sh")
               || opcode.startsWith("sb")
               || opcode.startsWith("sc")
               || opcode.startsWith("sdc1")
               || opcode.startsWith("sllv")
               || opcode.startsWith("sll")
               || opcode.startsWith("sra")
               || opcode.startsWith("srav")
               || opcode.startsWith("srl")
               || opcode.startsWith("srlv")
               || opcode.startsWith("negu")
               ) {
               reverse = true;
            }
            String suffix = "";
            if( oneLast && twoLast ) {
               suffix = ".l12";
            } else if( oneLast ) {
               if(reverse)
                  suffix = ".l2";
               else
                  suffix = ".l1";
            } else if( twoLast ) {
               if(reverse)
                  suffix = ".l1";
               else
                  suffix = ".l2";
            }
            String new_assem = opcode + suffix + assem.substring(space);

            if(trace)
               System.out.println("  " + instr.getAssem() + " ==> "+ new_assem);
            Instr new_i = instr.cloneMutateAssem(new_assem);
            // XXX This is a gross hack to manually copy register
            // assignment information because the instr interface
            // doesn't have a good copy method.
            Temp[] refs = new_i.use();
            for(int i = 0; i < refs.length; ++i) {
               code.assignRegister(new_i, refs[i], 
                                   code.getRegisters(instr, refs[i]));
            }
            refs = new_i.def();
            for(int i = 0; i < refs.length; ++i) {
               code.assignRegister(new_i, refs[i], 
                                   code.getRegisters(instr, refs[i]));
            }
            return new_i;
         }
      }
      return null;
   }

   public BypassLatchSchedule(Code _code, Frame _frame) {
      code  = _code;
      frame = _frame;
      RegUseDefer ud = new RegUseDefer(code);
      lt = LiveTemps.make(code, ud,
                          frame.getRegFileInfo().liveOnExit());
      BasicBlock.Factory bbFact = new BasicBlock.Factory(code);
      for(Iterator bbIt = bbFact.blocksIterator(); bbIt.hasNext();) {
         BasicBlock bb = (BasicBlock)bbIt.next();
         List instrs = bb.statements();
         for(int i = 0; i < instrs.size(); ++i) {
            Instr instr = (Instr)instrs.get(i);
            Instr new_instr = lastUseInstr(instr);
            if(new_instr != null) {
               Instr.replace(instr, new_instr);
            }
         }
      }
   }
   
   private final Code code;
   private final Frame frame;
   private LiveTemps lt;
   private boolean trace = false;
}
