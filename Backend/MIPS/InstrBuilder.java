// InstrBuilder.java, created Fri Sep 10 23:37:52 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Backend.StrongARM.TwoWordTemp;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;
import harpoon.Util.Util;
import harpoon.Util.ArrayIterator;

import java.util.List;
import java.util.Iterator;
import java.util.Arrays;

/** <code>MIPS.InstrBuilder</code> is an <code>Generic.InstrBuilder</code> for the
    MIPS architecture.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @author  Emmett Witchel <witchel@mit.edu>
    @version $Id: InstrBuilder.java,v 1.3 2002-02-26 22:43:59 cananian Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {

   RegFileInfo rfInfo;
   private Frame frame;

   /* helper macro. */
   private final Temp SP() { 
      Util.ASSERT(rfInfo.SP != null);
      return rfInfo.SP;
   }
    
   InstrBuilder(RegFileInfo rfInfo, Frame _frame) {
      super();
      this.rfInfo = rfInfo;
      frame = _frame;
   }

   public int getSize(Temp t) {
      if (t instanceof TwoWordTemp) {
         return 2; 
      } else {
         return 1;
      }
   }

   public List makeLoad(Temp r, int offset, Instr template) {
      StackInfo stack = ((CodeGen)frame.getCodeGen()).getStackInfo();
      offset = stack.localSaveOffset(offset);
      String[] strs = getLdrAssemStrs(r, offset);
      Util.ASSERT(strs.length == 1 || strs.length == 2 );

      if (strs.length == 2) {
         InstrMEM load1 = 
            new InstrMEM(template.getFactory(), template,
                         strs[0],
                         new Temp[]{ r },
                         new Temp[]{ SP()  });
         InstrMEM load2 = 
            new InstrMEM(template.getFactory(), template,
                         strs[1],
                         new Temp[]{ r },
                         new Temp[]{ SP()  });
         load2.layout(load1, null);
         return Arrays.asList(new InstrMEM[] { load1, load2 });
      } else {
         InstrMEM load = 
            new InstrMEM(template.getFactory(), template,
                         strs[0],
                         new Temp[]{ r },
                         new Temp[]{ SP()  });
         return Arrays.asList(new InstrMEM[] { load });
      }
   }

   private String[] getLdrAssemStrs(Temp r, int offset) {
      if (r instanceof TwoWordTemp) {
         return new String[] {
            "lw `d0h, ", + offset     + "(`s0)        # tmp restore (h)" ,
            "lw `d0l, ", + (offset+4) + "(`s0)        # tmp restore (l)" };
      } else {
         return new String[] { 
            "lw `d0, " + offset       + "(`s0)        # tmp restore" };
      }
   }

   private String[] getStrAssemStrs(Temp r, int offset) {
      if (r instanceof TwoWordTemp) {
         return new String[] {
            "sw `s0h, " + offset     + "(`s1)        # tmp save (h)", 
            "sw `s0l, " + (offset+4) + "(`s1)        # tmp save (l)" };
      } else {
         return new String[] { 
            "sw `s0, " + offset      + "(`s1)          # tmp save" };
      }
   }

   public List makeStore(Temp r, int offset, Instr template) {
      StackInfo stack = ((CodeGen)frame.getCodeGen()).getStackInfo();
      offset = stack.localSaveOffset(offset);       
      String[] strs = getStrAssemStrs(r, offset);
      Util.ASSERT(strs.length == 1 || 
                  strs.length == 2);
	    
      if (strs.length == 2) {
         System.out.println("In makeStore, twoWord case");

         InstrMEM store1 = 
            new InstrMEM(template.getFactory(), template,
                         strs[0],
                         new Temp[]{ },
                         new Temp[]{ r , SP() });
         InstrMEM store2 = 
            new InstrMEM(template.getFactory(), template,
                         strs[1],
                         new Temp[]{ },
                         new Temp[]{ r , SP() });
         store2.layout(store1, null);
         Util.ASSERT(store1.getNext() == store2, "store1.next == store2");
         Util.ASSERT(store2.getPrev() == store1, "store2.prev == store1");
         return Arrays.asList(new InstrMEM[]{ store1, store2 });
      } else {

         InstrMEM store = 
            new InstrMEM(template.getFactory(), template,
                         strs[0],
                         new Temp[]{ },
                         new Temp[]{ r , SP() });
         return Arrays.asList(new InstrMEM[] { store });
      }
   }

   public InstrLABEL makeLabel(Instr template) {
      Label l = new Label();
      InstrLABEL il = new InstrLABEL(template.getFactory(), 
                                     template,
                                     l.toString() + ":", l);
      return il;
   }

   /** Returns a new <code>InstrLABEL</code> for generating new
       arbitrary code blocks to branch to.
       @param template An <code>Instr</code> to base the generated
       <code>InstrLABEL</code>.
       <code>template</code> should be part of the
       instruction stream that the returned
       <code>InstrLABEL</code> is intended for. 
   */
   public InstrLABEL makeLabel(Label l, Instr template) {
      InstrLABEL il = new InstrLABEL(template.getFactory(), 
                                     template,
                                     l.toString() + ":", l);
      return il;
   }
}
