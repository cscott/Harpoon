// RegFileInfo.java, created Sat Sep 11 00:43:20 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.LocationFactory.Location;
import harpoon.Backend.Generic.RegFileInfo.SpillException;
import harpoon.Backend.StrongARM.TwoWordTemp;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode.PrintCallback;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HDataElement;
import harpoon.IR.Tree.Data;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Collections.LinearSet;
import harpoon.Util.Collections.ListFactory;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
 * <code>RegFileInfo</code> encapsulates information about the
 * MIPS register set.  This object also implements
 * <code>Generic.LocationFactory</code>, allowing the creation of
 * global registers for the use of the runtime.
 * 
 * @author  Emmett Witchel <witchel@mit.edu>
 * @version $Id: RegFileInfo.java,v 1.1.2.10 2001-07-07 18:30:47 pnkfelix Exp $
 */
public class RegFileInfo
extends harpoon.Backend.Generic.RegFileInfo 
implements harpoon.Backend.Generic.LocationFactory
{
   // FSK wants to relinquish author ship of the first half of this
   // file, since it was just cut-and-pasted out of the
   // hack-once-known-as-SAFrame

   final Temp[] reg;
   final Set callerSaveRegs;
   final Set calleeSaveRegs;
   final Set liveOnExitRegs;
   final Temp[] regGeneral; 
   final TempFactory regtf;
   Set oneWordAssigns, twoWordAssigns;

   // Use symbolic names
   
   final Temp ZERO; // Frame pointer
   final Temp AT;   // Assembler Register
   final Temp V0;   // return
   final Temp V1;   // return
   final Temp A0;   // arg 0
   final Temp A1;   // arg 1
   final Temp A2;   // arg 2
   final Temp A3;   // arg 3
   final Temp T0;   // temps
   final Temp T1; 
   final Temp T2;
   final Temp T3;
   final Temp T4;
   final Temp T5;
   final Temp T6;
   final Temp T7;
   final Temp T8;
   final Temp T9;
   final Temp S0;  // Caller saved
   final Temp S1;
   final Temp S2;
   final Temp S3;
   final Temp S4;
   final Temp S5;
   final Temp S6;
   final Temp S7;
   final Temp K0;  // Reserved for the kernel
   final Temp K1;
   final Temp GP;  // pointer to take of immediates, for assembler
   final Temp FP;  // Frame pointer
   final Temp SP;  // Stack pointer
   final Temp LR;  // Link register

   /** Creates a <code>RegFileInfo</code>. 
     */
   public RegFileInfo() {
      // On MIPS, $0 = zero $1 = assembler reg, 26 & 27 are for the kernel
      // We exclude 28 (gp) since C code asssumes it has a useful value
      reg = new Temp[32];
      regGeneral = new Temp[32];
      callerSaveRegs = new LinearSet(18);
      calleeSaveRegs = new LinearSet(10);
      liveOnExitRegs = new LinearSet(4);
      regtf = new TempFactory() {
            private int i = 0;
            private final String scope = "mips-registers";
	    
            // 
            /* r0 = 0
             * r29 is sp
             * r30 is our choice for an fp
             * r28 is global pointer which we don't deal with right now
             * see Kane for more details
             */
            private final String[] names = 
            {"$0", "$1", "$2", "$3", "$4", "$5", "$6", "$7",
             "$8", "$9", "$10", "$11", "$12", "$13", "$14", "$15",
             "$16", "$17", "$18", "$19", "$20", "$21", "$22", "$23", 
             "$24", "$25", "$26", "$27", "$28", "$sp", "$30", "$31" };
	    
            public String getScope() { return scope; }
            protected synchronized String getUniqueID(String suggestion) {
               Util.assert(i < names.length, "Don't use the "+
                           "TempFactory of Register Temps");
               i++;
               return names[i-1];
            }
         };

      class RegTemp extends Temp implements MachineRegLoc {
         int offset;
         RegTemp(TempFactory tf, int offset) {
            super(tf);
            this.offset = offset;
         }
         public int kind() { return MachineRegLoc.KIND; }
         public int regIndex() { return offset; }
	    
      }

      for(int i = 0; i < reg.length; ++i) {
         regGeneral[i] = reg[i] = new RegTemp(regtf, i);
      }

      ZERO = reg[0];
      AT   = reg[1];
      V0   = reg[2];
      V1   = reg[3];
      A0   = reg[4];
      A1   = reg[5];
      A2   = reg[6];
      A3   = reg[7];
      T0   = reg[8];
      T1   = reg[9];
      T2   = reg[10];
      T3   = reg[11];
      T4   = reg[12];
      T5   = reg[13];
      T6   = reg[14];
      T7   = reg[15];
      S0   = reg[16];
      S1   = reg[17];
      S2   = reg[18];
      S3   = reg[19];
      S4   = reg[20];
      S5   = reg[21];
      S6   = reg[22];
      S7   = reg[23];
      T8   = reg[24];
      T9   = reg[25];
      K0   = reg[26];
      K1   = reg[27];
      GP   = reg[28];
      SP   = reg[29];
      FP   = reg[30];
      LR   = reg[31];
	
      liveOnExitRegs.add(V0);  // return value
      liveOnExitRegs.add(V1); // (possible) long word return value
      liveOnExitRegs.add(FP);
      liveOnExitRegs.add(SP);

      // callee clobbers v0, v1, a0-a3, t0-t9, gp, ra
      callerSaveRegs.add(V0);
      callerSaveRegs.add(V1);
      callerSaveRegs.add(A0);
      callerSaveRegs.add(A1);
      callerSaveRegs.add(A2);
      callerSaveRegs.add(A3);
      callerSaveRegs.add(T0);
      callerSaveRegs.add(T1);
      callerSaveRegs.add(T2);
      callerSaveRegs.add(T3);
      callerSaveRegs.add(T4);
      callerSaveRegs.add(T5);
      callerSaveRegs.add(T6);
      callerSaveRegs.add(T7);
      callerSaveRegs.add(T8);
      callerSaveRegs.add(T9);
      callerSaveRegs.add(GP);
      callerSaveRegs.add(LR);  // lr
	
      // callee saves s0-s8, sp
      calleeSaveRegs.add(S0);
      calleeSaveRegs.add(S1);
      calleeSaveRegs.add(S2);
      calleeSaveRegs.add(S3);
      calleeSaveRegs.add(S4);
      calleeSaveRegs.add(S5);
      calleeSaveRegs.add(S6);
      calleeSaveRegs.add(S7);
      calleeSaveRegs.add(SP);  // SP
      calleeSaveRegs.add(FP);  // s8

      oneWordAssigns = new HashSet();
      // Start at 2 because R0 == 0 and R1 is the assembler register.
      for (int i=2; i<regGeneral.length; i++) {
         if(excludeReg(i) == false) {
            Temp[] assign = new Temp[] { regGeneral[i] };
            oneWordAssigns.add(Arrays.asList(assign));
         }
      }
      oneWordAssigns = Collections.unmodifiableSet(oneWordAssigns);
      twoWordAssigns = new HashSet();
      // Start at 2 because R0 == 0 and R1 is the assembler register.
      for (int i=2; i<regGeneral.length-1; i++) {
         if(excludeReg(i) == false && excludeReg(i+1) == false) {
            Temp[] assign = new Temp[] { regGeneral[i] ,
                                         regGeneral[i+1] };
            twoWordAssigns.add(Arrays.asList(assign));
         }
      }
      twoWordAssigns = Collections.unmodifiableSet(twoWordAssigns);

   }
    
   public Temp[] getAllRegisters() { 
      return (Temp[]) Util.safeCopy(Temp.arrayFactory, reg); 
   }

   public Temp getRegister(int index) {
      return reg[index];
   }

   public Temp[] getGeneralRegisters() { 
      return (Temp[]) Util.safeCopy(Temp.arrayFactory, regGeneral); 
   }

   public TempFactory regTempFactory() { return regtf; }

   public boolean isRegister(Temp t) {
      return t.tempFactory() == regTempFactory();
   }

   public int getSize(Temp temp) {
      if (temp instanceof TwoWordTemp) {
         return 2;
      } else {
         return 1;
      }
   }

    public Set getRegAssignments(Temp t) {
	if (t instanceof TwoWordTemp) {
	    return twoWordAssigns;
	} else {
	    return oneWordAssigns;
	}
    }

   /** Return true for any registers we want to keep out of register
    * allocation */ 
   private boolean excludeReg(int reg) {
      // These registers are used by the yellow pekoe assembler
      if(reg == 14) return true;
      if(reg == 15) return true;
      if(reg == 24) return true;
      if(reg == 25) return true;

      // These are the kernel registers
      if(reg == 26) return true;
      if(reg == 27) return true;

      // We exlude the GP since the C code we call into assumes GP has
      // a proper value
      // GP, SP, FP, LR
      if(reg == 28) return true;
      if(reg == 29) return true;
      if(reg == 30) return true;
      if(reg == 31) return true;
      return false;
   }

   public Iterator suggestRegAssignment(Temp t, final Map regFile, final Collection preassigned) 
      throws SpillException {
      final ArrayList suggests = new ArrayList();
      final ArrayList spills = new ArrayList();
	
      if (t instanceof TwoWordTemp) {
         // double word, find two registers ( the strongARM
         // doesn't require them to be in a row, but its 
         // simpler to search for adjacent registers )
         // FSK: forcing alignment to solve regalloc problem
         for (int i=2; i<regtop; i+=2) {
            if(excludeReg(i) == false && excludeReg(i + 1) == false) {
               Temp[] assign = new Temp[] { regGeneral[i+1] ,
                                            regGeneral[i] };
               if ((regFile.get(assign[0]) == null) &&
                   (regFile.get(assign[1]) == null)) {
                  suggests.add(Arrays.asList(assign));
               } else {
                  // don't add precolored registers to potential
                  // spills. 
		   if ( !(preassigned.contains(regFile.get(assign[0]))) &&
			!(preassigned.contains(regFile.get(assign[1])))){

                     Set s = new LinearSet(2);
                     s.add(assign[1]);
                     s.add(assign[0]);
                     spills.add(s);
                  }
               }
            }
         }

      } else {
         // single word, find one register
         for (int i=2; i<=regtop; i++) {
            if(excludeReg(i) == false) {
               if ((regFile.get(regGeneral[i]) == null)) {
                  suggests.add(ListFactory.singleton(regGeneral[i]));
               } else {
                  Set s = new LinearSet(1);
                  // don't add precolored registers to potential
                  // spills. 
                  if (!( preassigned.contains(regFile.get(regGeneral[i])))){
                     s.add(regGeneral[i]);
                     spills.add(s);
                  }
               }
            }
         }
      }
      if (suggests.isEmpty()) {
         throw new SpillException() {
               public Iterator getPotentialSpills() {
                  // System.out.println("RFI: Spills.size() "+spills.size());
                  return spills.iterator();
               }
            };
      }
      return suggests.iterator();
   }

   public Set liveOnExit() {
      return Collections.unmodifiableSet(liveOnExitRegs);
   }
    
   public Set callerSave() { 
      return Collections.unmodifiableSet(callerSaveRegs);
   }
    
   public Set calleeSave() { 
      return Collections.unmodifiableSet(calleeSaveRegs);
   }
    

   // LocationFactory interface.

    /** Allocate a global register of the specified type and return a
     *  handle to it.
     *  @param type a <code>IR.Tree.Type</code> specifying the type
     *              of the register.
     */
   public Location allocateLocation(final int type) {
      Util.assert(Type.isValid(type), "invalid type");
      Util.assert(!makeLocationDataCalled,
                  "allocateLocation() may not be called after "+
                  "makeLocationData() has been called.");
      Util.assert(type!=Type.LONG && type!=Type.DOUBLE,
                  "doubleword locations not implemented by this "+
                  "LocationFactory");
      // all other types of locations need a single register.

      // FSK: in theory, we could support arbitrary numbers of 
      // allocations by switching to mem locations.  But I don't
      // want to try to implement that yet.  
      Util.assert(regtop > 4, "allocated WAY too many locations, something's wrong");

      final Temp allocreg = reg[regtop--];

      // take this out of callersave, calleesave, etc.
      calleeSaveRegs.remove(allocreg);
      callerSaveRegs.remove(allocreg);
      liveOnExitRegs.remove(allocreg);

      return new Location() {
            public Exp makeAccessor(TreeFactory tf, HCodeElement source) {
               return new TEMP(tf, source, type, allocreg);
            }
         };
   }

   /** The index of the next register to be allocated. */
   private int regtop=25;

   // since we're just making global registers, we don't need to
   // allocate the storage anywhere.

   /** Create an <code>HData</code> which allocates static space for
     *  any <code>LocationFactory.Location</code>s that have been created.
     *  As this implementation only allocates global registers, the
     *  <code>HData</code> returned is always empty. */
   public HData makeLocationData(final Frame f) {
      // make sure we don't call allocateLocation after this.
      makeLocationDataCalled=true;
      // return an empty HData.
      return new Data("location-data",f) {
            /** Global data, so <code>HClass</code> is <code>null</code>. */
            public HClass getHClass() { return null; }
            /** Empty tree, so root element is <code>null</code>. */
            public HDataElement getRootElement() { return null; }
            /** Tell a human reader that there is no data here. */
            public void print(java.io.PrintWriter pw, PrintCallback cb) {
               pw.println("--- no data ---");
            }
         };
   }
   private boolean makeLocationDataCalled=false;
}
