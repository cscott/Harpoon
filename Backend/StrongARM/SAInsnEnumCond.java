// SAInsnEnumCond.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

/**
 * <code>SAInsnEnumCond</code> is an enumerated type for the various kinds
 * of conditional execution code mnemonics which can be appended to any
 * <code>SAInsn</code>.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SAInsnEnumCond.java,v 1.1.2.1 1999-02-08 00:54:30 andyb Exp $
 */
public class SAInsnEnumCond {
    private static int n = min();
    private static String[] names = {"EQ", "NE", "CS", "CC", "MI", "PL",
                                     "VS", "VC", "HI", "LS", "GE", "LT",
                                     "GT", "LE", "AL", "NV"};

    /* EQ: Equal, Z flag set */
    public final static int EQ = n++;
    
    /* NE: Not Equal, Z flag clear */
    public final static int NE = n++;

    /* CS: Carry Set, C flag set */
    public final static int CS = n;     /* don't increment. CS == HS */ 

    /* HS: Unsigned Higher or Same, C flag set */
    public final static int HS = n++;

    /* CC: Carry Clear, C flag clear */
    public final static int CC = n;     /* don't increment. CC == LO */

    /* LO: Unsigned Loweer, C flag clear */
    public final static int LO = n++;

    /* MI: Minus/Negative, N flag set */
    public final static int MI = n++;

    /* PL: Plus/Positive or Zero, N flag clear */
    public final static int PL = n++;

    /* VS: Overflow, V flag set */
    public final static int VS = n++;

    /* VC: No Overflow, V flag clear */
    public final static int VC = n++;

    /* HI: Unsigned Higher, C set and Z clear */
    public final static int HI = n++;

    /* LS: Unsigned Lower or Same, C clear or Z set */
    public final static int LS = n++;

    /* GE: Signed Greater Than or Equal, N == V */
    public final static int GE = n++;

    /* LT: Signed Less Than, N != V */
    public final static int LT = n++;

    /* GT: Signed Greater Than, Z clear && N == V */
    public final static int GT = n++;

    /* LE: Signed Less Than or Equal, Z set || N != V */
    public final static int LE = n++;

    /* AL: Always, unconditional */
    public final static int AL = n++;

    /* NV: Never
     * WARNING: use of this causes UNPREDICTABLE behavior.
     * See ARM Reference Manual edition B, page 3-4 */
    public final static int NV = n++;

    public static int min() { return 0; }
    public static int max() { return n; }

    public static boolean isValid(int k) {
        return (min() <= k) && (k < max());
    }

    public static String str(int condCode) {
        return isValid(condCode) ? names[condCode] : "";
    }
}
