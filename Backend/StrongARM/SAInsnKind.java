// SAInsnKind.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

/**
 * <code>SAInsnKind</code> is an enumerated type for the various kinds of
 * <code>SAInsn</code>s.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SAInsnKind.java,v 1.1.2.1 1999-02-08 00:54:31 andyb Exp $
 */
public abstract class SAInsnKind {
    private static int n = min();

    public final static int ADC = n++;
    public final static int ADD = n++;
    public final static int AND = n++;
    public final static int B = n++;
    public final static int BL = n++;
    public final static int BIC = n++;
    public final static int CMN = n++;
    public final static int CMP = n++;
    public final static int EOR = n++;
    public final static int LDM = n++;
    public final static int LDR = n++;
    public final static int LDRB = n++;
    public final static int LDRH = n++;
    public final static int LDRSB = n++;
    public final static int LDRSH = n++;
    public final static int MLA = n++;
    public final static int MOV = n++;
    public final static int MRS = n++;
    public final static int MSR = n++;
    public final static int MUL = n++;
    public final static int MVN = n++;
    public final static int ORR = n++;
    public final static int RSB = n++;
    public final static int RSC = n++;
    public final static int SBC = n++;
    public final static int SMLAL = n++;
    public final static int SMULL = n++;
    public final static int STM = n++;
    public final static int STR = n++;
    public final static int STRB = n++;
    public final static int STRH = n++;
    public final static int SWI = n++;
    public final static int SWP = n++;
    public final static int SWPB = n++;
    public final static int TEQ = n++;
    public final static int TST = n++;
    public final static int UMLAL = n++;
    public final static int UMULL = n++;

    public static int min() { return 0; }
    public static int max() { return n; }

    public static boolean isValid(int k) {
        return (min() <= k) && (k < max());
    }
}
