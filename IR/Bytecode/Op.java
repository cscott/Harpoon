// Op.java, created Sun Sep 13 22:49:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.Util.Util;
/**
 * The <code>Op</code> class contains constants and tables that we are
 * likely to find useful when grokking a java bytecode stream.
 * <p>
 * Included are: <UL>
 * <LI>final static byte constants for all the JVM opcodes.
 * <LI>a method to convert any byte opcode to the string naming it.
 * <LI>a method giving the size (number of operands) of an
 *     arbitrary opcode.
 * <LI>a way to tell whether a given opcode is a branch instruction, and
 *     if so whether the branch is conditional or unconditional.
 * <LI>a method to decode the targets of any branch instruction.
 * </UL>
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Op.java,v 1.5 2002-02-26 22:45:32 cananian Exp $
 * @see     harpoon.IR.RawClass.AttributeCode
 * @see     harpoon.IR.Bytecode.Instr
 * @see     harpoon.IR.Bytecode.Code
 */

public abstract class Op {

  // Set up table of opcodes.
  public final static byte 
    NOP		    = (byte) 0x00,
    ACONST_NULL	    = (byte) 0x01,
    ICONST_M1       = (byte) 0x02,
    ICONST_0        = (byte) 0x03,
    ICONST_1        = (byte) 0x04,
    ICONST_2        = (byte) 0x05,
    ICONST_3        = (byte) 0x06,
    ICONST_4        = (byte) 0x07,
    ICONST_5        = (byte) 0x08,
    LCONST_0        = (byte) 0x09,
    LCONST_1        = (byte) 0x0A,
    FCONST_0        = (byte) 0x0B,
    FCONST_1        = (byte) 0x0C,
    FCONST_2        = (byte) 0x0D,
    DCONST_0        = (byte) 0x0E,
    DCONST_1        = (byte) 0x0F,
    BIPUSH          = (byte) 0x10,
    SIPUSH          = (byte) 0x11,
    LDC             = (byte) 0x12,
    LDC_W	    = (byte) 0x13,
    LDC2_W	    = (byte) 0x14,
    ILOAD	    = (byte) 0x15,
    LLOAD	    = (byte) 0x16,
    FLOAD	    = (byte) 0x17,
    DLOAD	    = (byte) 0x18,
    ALOAD	    = (byte) 0x19,
    ILOAD_0	    = (byte) 0x1A,
    ILOAD_1	    = (byte) 0x1B,
    ILOAD_2	    = (byte) 0x1C,
    ILOAD_3	    = (byte) 0x1D,
    LLOAD_0	    = (byte) 0x1E,
    LLOAD_1	    = (byte) 0x1F,
    LLOAD_2	    = (byte) 0x20,
    LLOAD_3	    = (byte) 0x21,
    FLOAD_0	    = (byte) 0x22,
    FLOAD_1	    = (byte) 0x23,
    FLOAD_2	    = (byte) 0x24,
    FLOAD_3	    = (byte) 0x25,
    DLOAD_0	    = (byte) 0x26,
    DLOAD_1	    = (byte) 0x27,
    DLOAD_2	    = (byte) 0x28,
    DLOAD_3	    = (byte) 0x29,
    ALOAD_0	    = (byte) 0x2A,
    ALOAD_1	    = (byte) 0x2B,
    ALOAD_2	    = (byte) 0x2C,
    ALOAD_3	    = (byte) 0x2D,
    IALOAD	    = (byte) 0x2E,
    LALOAD	    = (byte) 0x2F,
    FALOAD	    = (byte) 0x30,
    DALOAD	    = (byte) 0x31,
    AALOAD	    = (byte) 0x32,
    BALOAD	    = (byte) 0x33,
    CALOAD	    = (byte) 0x34,
    SALOAD	    = (byte) 0x35,
    ISTORE	    = (byte) 0x36,
    LSTORE	    = (byte) 0x37,
    FSTORE	    = (byte) 0x38,
    DSTORE	    = (byte) 0x39,
    ASTORE	    = (byte) 0x3A,
    ISTORE_0	    = (byte) 0x3B,
    ISTORE_1	    = (byte) 0x3C,
    ISTORE_2	    = (byte) 0x3D,
    ISTORE_3	    = (byte) 0x3E,
    LSTORE_0	    = (byte) 0x3F;
  public final static byte 
    LSTORE_1        = (byte) 0x40,
    LSTORE_2	    = (byte) 0x41,
    LSTORE_3	    = (byte) 0x42,
    FSTORE_0	    = (byte) 0x43,
    FSTORE_1	    = (byte) 0x44,
    FSTORE_2	    = (byte) 0x45,
    FSTORE_3	    = (byte) 0x46,
    DSTORE_0	    = (byte) 0x47,
    DSTORE_1	    = (byte) 0x48,
    DSTORE_2	    = (byte) 0x49,
    DSTORE_3	    = (byte) 0x4A,
    ASTORE_0	    = (byte) 0x4B,
    ASTORE_1	    = (byte) 0x4C,
    ASTORE_2	    = (byte) 0x4D,
    ASTORE_3	    = (byte) 0x4E,
    IASTORE	    = (byte) 0x4F,
    LASTORE	    = (byte) 0x50,
    FASTORE	    = (byte) 0x51,
    DASTORE	    = (byte) 0x52,
    AASTORE	    = (byte) 0x53,
    BASTORE	    = (byte) 0x54,
    CASTORE	    = (byte) 0x55,
    SASTORE	    = (byte) 0x56,
    POP		    = (byte) 0x57,
    POP2	    = (byte) 0x58,
    DUP		    = (byte) 0x59,
    DUP_X1	    = (byte) 0x5A,
    DUP_X2	    = (byte) 0x5B,
    DUP2	    = (byte) 0x5C,
    DUP2_X1	    = (byte) 0x5D,
    DUP2_X2	    = (byte) 0x5E,
    SWAP	    = (byte) 0x5F,
    IADD	    = (byte) 0x60,
    LADD	    = (byte) 0x61,
    FADD	    = (byte) 0x62,
    DADD	    = (byte) 0x63,
    ISUB	    = (byte) 0x64,
    LSUB	    = (byte) 0x65,
    FSUB	    = (byte) 0x66,
    DSUB	    = (byte) 0x67,
    IMUL	    = (byte) 0x68,
    LMUL	    = (byte) 0x69,
    FMUL	    = (byte) 0x6A,
    DMUL	    = (byte) 0x6B,
    IDIV	    = (byte) 0x6C,
    LDIV	    = (byte) 0x6D,
    FDIV	    = (byte) 0x6E,
    DDIV	    = (byte) 0x6F,
    IREM	    = (byte) 0x70,
    LREM	    = (byte) 0x71,
    FREM	    = (byte) 0x72,
    DREM	    = (byte) 0x73,
    INEG	    = (byte) 0x74,
    LNEG	    = (byte) 0x75,
    FNEG	    = (byte) 0x76,
    DNEG	    = (byte) 0x77,
    ISHL	    = (byte) 0x78,
    LSHL	    = (byte) 0x79,
    ISHR	    = (byte) 0x7A,
    LSHR	    = (byte) 0x7B,
    IUSHR	    = (byte) 0x7C,
    LUSHR	    = (byte) 0x7D,
    IAND	    = (byte) 0x7E,
    LAND	    = (byte) 0x7F;
  public final static byte 
    IOR             = (byte) 0x80,
    LOR		    = (byte) 0x81,
    IXOR	    = (byte) 0x82,
    LXOR	    = (byte) 0x83,
    IINC	    = (byte) 0x84,
    I2L		    = (byte) 0x85,
    I2F		    = (byte) 0x86,
    I2D		    = (byte) 0x87,
    L2I		    = (byte) 0x88,
    L2F		    = (byte) 0x89,
    L2D		    = (byte) 0x8A,
    F2I		    = (byte) 0x8B,
    F2L		    = (byte) 0x8C,
    F2D		    = (byte) 0x8D,
    D2I		    = (byte) 0x8E,
    D2L		    = (byte) 0x8F,
    D2F		    = (byte) 0x90,
    I2B		    = (byte) 0x91,
    I2C		    = (byte) 0x92,
    I2S		    = (byte) 0x93,
    LCMP	    = (byte) 0x94,
    FCMPL	    = (byte) 0x95,
    FCMPG	    = (byte) 0x96,
    DCMPL	    = (byte) 0x97,
    DCMPG	    = (byte) 0x98,
    IFEQ	    = (byte) 0x99,
    IFNE	    = (byte) 0x9A,
    IFLT	    = (byte) 0x9B,
    IFGE	    = (byte) 0x9C,
    IFGT	    = (byte) 0x9D,
    IFLE	    = (byte) 0x9E,
    IF_ICMPEQ	    = (byte) 0x9F,
    IF_ICMPNE	    = (byte) 0xA0,
    IF_ICMPLT	    = (byte) 0xA1,
    IF_ICMPGE	    = (byte) 0xA2,
    IF_ICMPGT	    = (byte) 0xA3,
    IF_ICMPLE	    = (byte) 0xA4,
    IF_ACMPEQ	    = (byte) 0xA5,
    IF_ACMPNE	    = (byte) 0xA6,
    GOTO	    = (byte) 0xA7,
    JSR		    = (byte) 0xA8,
    RET		    = (byte) 0xA9,
    TABLESWITCH     = (byte) 0xAA,
    LOOKUPSWITCH    = (byte) 0xAB,
    IRETURN	    = (byte) 0xAC,
    LRETURN	    = (byte) 0xAD,
    FRETURN	    = (byte) 0xAE,
    DRETURN	    = (byte) 0xAF,
    ARETURN	    = (byte) 0xB0,
    RETURN	    = (byte) 0xB1,
    GETSTATIC	    = (byte) 0xB2,
    PUTSTATIC	    = (byte) 0xB3,
    GETFIELD	    = (byte) 0xB4,
    PUTFIELD	    = (byte) 0xB5,
    INVOKEVIRTUAL   = (byte) 0xB6,
    INVOKESPECIAL   = (byte) 0xB7,
    INVOKESTATIC    = (byte) 0xB8,
    INVOKEINTERFACE = (byte) 0xB9,
    XXXUNUSEDXXX    = (byte) 0xBA,
    NEW		    = (byte) 0xBB,
    NEWARRAY	    = (byte) 0xBC,
    ANEWARRAY	    = (byte) 0xBD,
    ARRAYLENGTH	    = (byte) 0xBE,
    ATHROW	    = (byte) 0xBF;
  public final static byte 
    CHECKCAST	    = (byte) 0xC0,
    INSTANCEOF	    = (byte) 0xC1,
    MONITORENTER    = (byte) 0xC2,
    MONITOREXIT	    = (byte) 0xC3,
    WIDE	    = (byte) 0xC4,
    MULTIANEWARRAY  = (byte) 0xC5,
    IFNULL	    = (byte) 0xC6,
    IFNONNULL	    = (byte) 0xC7,
    GOTO_W	    = (byte) 0xC8,
    JSR_W	    = (byte) 0xC9;
  // Reserved
  public final static byte 
    BREAKPOINT      = (byte) 0xCA;
  // _quick opcodes (never found in class files)
  public final static byte
    LDC_QUICK                 = (byte) 0xCB,
    LDC_W_QUICK               = (byte) 0xCC,
    LDC2_W_QUICK              = (byte) 0xCD,
    GETFIELD_QUICK            = (byte) 0xCE,
    PUTFIELD_QUICK            = (byte) 0xCF,
    GETFIELD2_QUICK           = (byte) 0xD0,
    PUTFIELD2_QUICK           = (byte) 0xD1,
    GETSTATIC_QUICK           = (byte) 0xD2,
    PUTSTATIC_QUICK           = (byte) 0xD3,
    GETSTATIC2_QUICK          = (byte) 0xD4,
    PUTSTATIC2_QUICK          = (byte) 0xD5,
    INVOKEVIRTUAL_QUICK       = (byte) 0xD6,
    INVOKENONVIRTUAL_QUICK    = (byte) 0xD7,
    INVOKESUPER_QUICK         = (byte) 0xD8,
    INVOKESTATIC_QUICK        = (byte) 0xD9,
    INVOKEINTERFACE_QUICK     = (byte) 0xDA,
    INVOKEVIRTUALOBJECT_QUICK = (byte) 0xDB,
    UNKNOWN_DC                = (byte) 0xDC,
    NEW_QUICK                 = (byte) 0xDD,
    ANEWARRAY_QUICK           = (byte) 0xDE,
    MULTIANEWARRAY_QUICK      = (byte) 0xDF,
    CHECKCAST_QUICK           = (byte) 0xE0,
    INSTANCEOF_QUICK          = (byte) 0xE1,
    INVOKEVIRTUAL_QUICK_W     = (byte) 0xE2,
    GETFIELD_QUICK_W          = (byte) 0xE3,
    PUTFIELD_QUICK_W          = (byte) 0xE4;
  // Unused
  public final static byte
    UNKNOWN_E5                = (byte) 0xE5,
    UNKNOWN_E6                = (byte) 0xE6,
    UNKNOWN_E7                = (byte) 0xE7,
    UNKNOWN_E8                = (byte) 0xE8,
    UNKNOWN_E9                = (byte) 0xE9,
    UNKNOWN_EA                = (byte) 0xEA,
    UNKNOWN_EB                = (byte) 0xEB,
    UNKNOWN_EC                = (byte) 0xEC,
    UNKNOWN_ED                = (byte) 0xED,
    UNKNOWN_EE                = (byte) 0xEE,
    UNKNOWN_EF                = (byte) 0xEF,
    UNKNOWN_F0                = (byte) 0xF0,
    UNKNOWN_F1                = (byte) 0xF1,
    UNKNOWN_F2                = (byte) 0xF2,
    UNKNOWN_F3                = (byte) 0xF3,
    UNKNOWN_F4                = (byte) 0xF4,
    UNKNOWN_F5                = (byte) 0xF5,
    UNKNOWN_F6                = (byte) 0xF6,
    UNKNOWN_F7                = (byte) 0xF7,
    UNKNOWN_F8                = (byte) 0xF8,
    UNKNOWN_F9                = (byte) 0xF9,
    UNKNOWN_FA                = (byte) 0xFA,
    UNKNOWN_FB                = (byte) 0xFB,
    UNKNOWN_FC                = (byte) 0xFC,
    UNKNOWN_FD                = (byte) 0xFD;
  // Reserved
  public final static byte 
    IMPDEP1	    = (byte) 0xFE,
    IMPDEP2	    = (byte) 0xFF;

  /** Get the human-readable name of a given opcode. */
  public final static String toString(byte opcode) 
    { return opcodeNames[((int)opcode)&0xFF]; }
  // Table of opcode names
  private final static String[] opcodeNames = new String[] {
    /* 0x00 */ "nop",
    /* 0x01 */ "aconst_null",
    /* 0x02 */ "iconst_m1",
    /* 0x03 */ "iconst_0",
    /* 0x04 */ "iconst_1",
    /* 0x05 */ "iconst_2",
    /* 0x06 */ "iconst_3",
    /* 0x07 */ "iconst_4",
    /* 0x08 */ "iconst_5",
    /* 0x09 */ "lconst_0",
    /* 0x0A */ "lconst_1",
    /* 0x0B */ "fconst_0",
    /* 0x0C */ "fconst_1",
    /* 0x0D */ "fconst_2",
    /* 0x0E */ "dconst_0",
    /* 0x0F */ "dconst_1",
    /* 0x10 */ "bipush",
    /* 0x11 */ "sipush",
    /* 0x12 */ "ldc",
    /* 0x13 */ "ldc_w",
    /* 0x14 */ "ldc2_w",
    /* 0x15 */ "iload",
    /* 0x16 */ "lload",
    /* 0x17 */ "fload",
    /* 0x18 */ "dload",
    /* 0x19 */ "aload",
    /* 0x1A */ "iload_0",
    /* 0x1B */ "iload_1",
    /* 0x1C */ "iload_2",
    /* 0x1D */ "iload_3",
    /* 0x1E */ "lload_0",
    /* 0x1F */ "lload_1",
    /* 0x20 */ "lload_2",
    /* 0x21 */ "lload_3",
    /* 0x22 */ "fload_0",
    /* 0x23 */ "fload_1",
    /* 0x24 */ "fload_2",
    /* 0x25 */ "fload_3",
    /* 0x26 */ "dload_0",
    /* 0x27 */ "dload_1",
    /* 0x28 */ "dload_2",
    /* 0x29 */ "dload_3",
    /* 0x2A */ "aload_0",
    /* 0x2B */ "aload_1",
    /* 0x2C */ "aload_2",
    /* 0x2D */ "aload_3",
    /* 0x2E */ "iaload",
    /* 0x2F */ "laload",
    /* 0x30 */ "faload",
    /* 0x31 */ "daload",
    /* 0x32 */ "aaload",
    /* 0x33 */ "baload",
    /* 0x34 */ "caload",
    /* 0x35 */ "saload",
    /* 0x36 */ "istore",
    /* 0x37 */ "lstore",
    /* 0x38 */ "fstore",
    /* 0x39 */ "dstore",
    /* 0x3A */ "astore",
    /* 0x3B */ "istore_0",
    /* 0x3C */ "istore_1",
    /* 0x3D */ "istore_2",
    /* 0x3E */ "istore_3",
    /* 0x3F */ "lstore_0",
    /* 0x40 */ "lstore_1",
    /* 0x41 */ "lstore_2",
    /* 0x42 */ "lstore_3",
    /* 0x43 */ "fstore_0",
    /* 0x44 */ "fstore_1",
    /* 0x45 */ "fstore_2",
    /* 0x46 */ "fstore_3",
    /* 0x47 */ "dstore_0",
    /* 0x48 */ "dstore_1",
    /* 0x49 */ "dstore_2",
    /* 0x4A */ "dstore_3",
    /* 0x4B */ "astore_0",
    /* 0x4C */ "astore_1",
    /* 0x4D */ "astore_2",
    /* 0x4E */ "astore_3",
    /* 0x4F */ "iastore",
    /* 0x50 */ "lastore",
    /* 0x51 */ "fastore",
    /* 0x52 */ "dastore",
    /* 0x53 */ "aastore",
    /* 0x54 */ "bastore",
    /* 0x55 */ "castore",
    /* 0x56 */ "sastore",
    /* 0x57 */ "pop",
    /* 0x58 */ "pop2",
    /* 0x59 */ "dup",
    /* 0x5A */ "dup_x1",
    /* 0x5B */ "dup_x2",
    /* 0x5C */ "dup2",
    /* 0x5D */ "dup2_x1",
    /* 0x5E */ "dup2_x2",
    /* 0x5F */ "swap",
    /* 0x60 */ "iadd",
    /* 0x61 */ "ladd",
    /* 0x62 */ "fadd",
    /* 0x63 */ "dadd",
    /* 0x64 */ "isub",
    /* 0x65 */ "lsub",
    /* 0x66 */ "fsub",
    /* 0x67 */ "dsub",
    /* 0x68 */ "imul",
    /* 0x69 */ "lmul",
    /* 0x6A */ "fmul",
    /* 0x6B */ "dmul",
    /* 0x6C */ "idiv",
    /* 0x6D */ "ldiv",
    /* 0x6E */ "fdiv",
    /* 0x6F */ "ddiv",
    /* 0x70 */ "irem",
    /* 0x71 */ "lrem",
    /* 0x72 */ "frem",
    /* 0x73 */ "drem",
    /* 0x74 */ "ineg",
    /* 0x75 */ "lneg",
    /* 0x76 */ "fneg",
    /* 0x77 */ "dneg",
    /* 0x78 */ "ishl",
    /* 0x79 */ "lshl",
    /* 0x7A */ "ishr",
    /* 0x7B */ "lshr",
    /* 0x7C */ "iushr",
    /* 0x7D */ "lushr",
    /* 0x7E */ "iand",
    /* 0x7F */ "land",
    /* 0x80 */ "ior",
    /* 0x81 */ "lor",
    /* 0x82 */ "ixor",
    /* 0x83 */ "lxor",
    /* 0x84 */ "iinc",
    /* 0x85 */ "i2l",
    /* 0x86 */ "i2f",
    /* 0x87 */ "i2d",
    /* 0x88 */ "l2i",
    /* 0x89 */ "l2f",
    /* 0x8A */ "l2d",
    /* 0x8B */ "f2i",
    /* 0x8C */ "f2l",
    /* 0x8D */ "f2d",
    /* 0x8E */ "d2i",
    /* 0x8F */ "d2l",
    /* 0x90 */ "d2f",
    /* 0x91 */ "i2b",
    /* 0x92 */ "i2c",
    /* 0x93 */ "i2s",
    /* 0x94 */ "lcmp",
    /* 0x95 */ "fcmpl",
    /* 0x96 */ "fcmpg",
    /* 0x97 */ "dcmpl",
    /* 0x98 */ "dcmpg",
    /* 0x99 */ "ifeq",
    /* 0x9A */ "ifne",
    /* 0x9B */ "iflt",
    /* 0x9C */ "ifge",
    /* 0x9D */ "ifgt",
    /* 0x9E */ "ifle",
    /* 0x9F */ "if_icmpeq",
    /* 0xA0 */ "if_icmpne",
    /* 0xA1 */ "if_icmplt",
    /* 0xA2 */ "if_icmpge",
    /* 0xA3 */ "if_icmpgt",
    /* 0xA4 */ "if_icmple",
    /* 0xA5 */ "if_acmpeq",
    /* 0xA6 */ "if_acmpne",
    /* 0xA7 */ "goto",
    /* 0xA8 */ "jsr",
    /* 0xA9 */ "ret",
    /* 0xAA */ "tableswitch",
    /* 0xAB */ "lookupswitch",
    /* 0xAC */ "ireturn",
    /* 0xAD */ "lreturn",
    /* 0xAE */ "freturn",
    /* 0xAF */ "dreturn",
    /* 0xB0 */ "areturn",
    /* 0xB1 */ "return",
    /* 0xB2 */ "getstatic",
    /* 0xB3 */ "putstatic",
    /* 0xB4 */ "getfield",
    /* 0xB5 */ "putfield",
    /* 0xB6 */ "invokevirtual",
    /* 0xB7 */ "invokespecial",
    /* 0xB8 */ "invokestatic",
    /* 0xB9 */ "invokeinterface",
    /* 0xBA */ "xxxunusedxxx",
    /* 0xBB */ "new",
    /* 0xBC */ "newarray",
    /* 0xBD */ "anewarray",
    /* 0xBE */ "arraylength",
    /* 0xBF */ "athrow",
    /* 0xC0 */ "checkcast",
    /* 0xC1 */ "instanceof",
    /* 0xC2 */ "monitorenter",
    /* 0xC3 */ "monitorexit",
    /* 0xC4 */ "wide",
    /* 0xC5 */ "multianewarray",
    /* 0xC6 */ "ifnull",
    /* 0xC7 */ "ifnonnull",
    /* 0xC8 */ "goto_w",
    /* 0xC9 */ "jsr_w",
    /* 0xCA */ "breakpoint",
    /* 0xCB */ "ldc_quick",
    /* 0xCC */ "ldc_w_quick",
    /* 0xCD */ "ldc2_w_quick",
    /* 0xCE */ "getfield_quick",
    /* 0xCF */ "putfield_quick",
    /* 0xD0 */ "getfield2_quick",
    /* 0xD1 */ "putfield2_quick",
    /* 0xD2 */ "getstatic_quick",
    /* 0xD3 */ "putstatic_quick",
    /* 0xD4 */ "getstatic2_quick",
    /* 0xD5 */ "putstatic2_quick",
    /* 0xD6 */ "invokevirtual_quick",
    /* 0xD7 */ "invokenonvirtual_quick",
    /* 0xD8 */ "invokesuper_quick",
    /* 0xD9 */ "invokestatic_quick",
    /* 0xDA */ "invokeinterface_quick",
    /* 0xDB */ "invokevirtualobject_quick",
    /* 0xDC */ "unknown_dc",
    /* 0xDD */ "new_quick",
    /* 0xDE */ "anewarray_quick",
    /* 0xDF */ "multianewarray_quick",
    /* 0xE0 */ "checkcast_quick",
    /* 0xE1 */ "instanceof_quick",
    /* 0xE2 */ "invokevirtual_quick_w",
    /* 0xE3 */ "getfield_quick_w",
    /* 0xE4 */ "putfield_quick_w",
    /* 0xE5 */ "unknown_e5",
    /* 0xE6 */ "unknown_e6",
    /* 0xE7 */ "unknown_e7",
    /* 0xE8 */ "unknown_e8",
    /* 0xE9 */ "unknown_e9",
    /* 0xEA */ "unknown_ea",
    /* 0xEB */ "unknown_eb",
    /* 0xEC */ "unknown_ec",
    /* 0xED */ "unknown_ed",
    /* 0xEE */ "unknown_ee",
    /* 0xEF */ "unknown_ef",
    /* 0xF0 */ "unknown_f0",
    /* 0xF1 */ "unknown_f1",
    /* 0xF2 */ "unknown_f2",
    /* 0xF3 */ "unknown_f3",
    /* 0xF4 */ "unknown_f4",
    /* 0xF5 */ "unknown_f5",
    /* 0xF6 */ "unknown_f6",
    /* 0xF7 */ "unknown_f7",
    /* 0xF8 */ "unknown_f8",
    /* 0xF9 */ "unknown_f9",
    /* 0xFA */ "unknown_fa",
    /* 0xFB */ "unknown_fb",
    /* 0xFC */ "unknown_fc",
    /* 0xFD */ "unknown_fd",
    /* 0xFE */ "impdep1",
    /* 0xFF */ "impdep2" 
  };
  
  // Set up table indicating instruction length for each opcode.
  private static byte offset[] = new byte[256];
  static {
    for (int i=0; i<offset.length; i++) offset[i]=0;
    offset[ub2i(BIPUSH)] = 1;
    offset[ub2i(SIPUSH)] = 2;
    offset[ub2i(LDC)]    = 1;
    offset[ub2i(LDC_W)]  = 2;
    offset[ub2i(LDC2_W)] = 2;
    for (int i=ub2i(ILOAD); i<=ub2i(ALOAD); i++) offset[i]=1;
    for (int i=ub2i(ISTORE);i<=ub2i(ASTORE);i++) offset[i]=1;
    offset[ub2i(IINC)]   = 2;
    for (int i=ub2i(IFEQ);  i<=ub2i(JSR); i++)   offset[i]=2;
    offset[ub2i(RET)]    = 1;
    // TABLESWITCH and LOOKUPSWITCH are highly irregular.
    for (int i=ub2i(GETSTATIC); i<=ub2i(INVOKESTATIC); i++) offset[i]=2;
    offset[ub2i(INVOKEINTERFACE)] = 4;
    offset[ub2i(NEW)]      = 2;
    offset[ub2i(NEWARRAY)] = 1;
    offset[ub2i(ANEWARRAY)]= 2;
    offset[ub2i(CHECKCAST)]= 2;
    offset[ub2i(INSTANCEOF)]=2;
    // WIDE is irregular.
    offset[ub2i(MULTIANEWARRAY)] = 3;
    offset[ub2i(IFNULL)]   = 2;
    offset[ub2i(IFNONNULL)]= 2;
    offset[ub2i(GOTO_W)]   = 4;
    offset[ub2i(JSR_W)]    = 4;
  }

  /** 
   * Gives the length of the instruction at the specified offset in a 
   * bytecode array.
   * <P>
   * The length is the number of operand bytes plus 1 byte for the opcode.
   * @param code the bytecode array.
   * @param pc   the offset into the array where the opcode of interest is
   *             located.
   * @return the length of the opcode and operands.
   */
  public final static int instrSize(byte[] code, int pc) {
    byte opcode = code[pc];
    switch(opcode) {
    case LOOKUPSWITCH:
      {
      int pad = 3-(pc%4);
      long npairs = make_s4(code, pc+pad+5);
      Util.ASSERT(npairs>=0);
      return (int) (npairs*8) + pad + 9;
      }
    case TABLESWITCH:
      {
      int pad = 3-(pc%4);
      long low = make_s4(code, pc+pad+5);
      long high= make_s4(code, pc+pad+9);
      long npairs = high - low + 1;
      Util.ASSERT(low <= high);
      return (int) (npairs*4) + pad + 13;
      }
    case WIDE:
      if (code[pc+1]==IINC) return 6;
      return 4;
    default:
      return 1+offset[ub2i(opcode)];
    }
  }

  // Set up table to determine whether an opcode is a branch instruction.
  private static boolean isBranch[] = new boolean[256];
  static {
    for (int i=0; i<isBranch.length; i++) isBranch[i]=false;
    for (int i=ub2i(IFEQ); i<=ub2i(RETURN); i++) isBranch[i]=true;
    for (int i=ub2i(IFNULL); i<=ub2i(JSR_W); i++) isBranch[i]=true;
    isBranch[ub2i(ATHROW)]=true;
  }

  /**
   * Tells whether a given opcode is a branch instruction.
   * @param opcode the opcode to check.
   * @return true if the opcode is a branch instruction.
   */
  public final static boolean isBranch(byte opcode) { 
    return isBranch[ub2i(opcode)];
  }

  /**
   * Tells whether a given opcode is an unconditional branch instruction.
   * @param opcode the opcode to check.
   * @return true if the opcode is an unconditional branch instruction.
   */
  public final static boolean isUnconditionalBranch(byte opcode) {
    switch(opcode) {
    case GOTO:
    case GOTO_W:
    case JSR:
    case JSR_W:
    case RET:
    case IRETURN:
    case LRETURN:
    case FRETURN:
    case DRETURN:
    case ARETURN:
    case RETURN:
    case ATHROW:
    case LOOKUPSWITCH:
    case TABLESWITCH:
      return true;
    default:
      return false;
    }
  }

  /**
   * Tells whether a given opcode is a jump to subrouting (JSR) instruction.
   * @param opcode the opcode to check.
   * @return true if the opcode is a JSR or JSR_W instruction.
   */
  public final static boolean isJSR(byte opcode) {
      if (opcode == JSR) return true;
      if (opcode == JSR_W) return true;
      return false;
  }

  /** 
   * Computes all possible targets for a branch instruction.
   * @exception Error if the opcode is not a branch instruction.
   * @param code the bytecode array.
   * @param pc   the offset into the bytecode array at which the desired
   *             instruction is found.
   * @return     an array of all possible targets, in the same format as 
   *             <code>pc</code>. (ie, offsets from the start of the
   *             bytecode array.)
   */
  public final static int[] branchTargets(byte[] code, int pc) {
    if (!isBranch(code[pc])) 
      throw new Error("Asking for target of non-branch.");
    // Funky switch statements.
    if (code[pc]==LOOKUPSWITCH) {
      int pad = 3-(pc%4);
      long deflt  = make_s4(code, pc+pad+1);
      long npairs = make_s4(code, pc+pad+5);
      Util.ASSERT(npairs >= 0);

      int result[] = new int[(int)npairs+1];
      result[0] = pc + ((int) deflt);
      for (int i=0; i<npairs; i++) {
	long offset = make_s4(code, pc+pad+9+4+(8*i));
	result[i+1] = pc + ((int) offset);
      }
      return result;
    }
    if (code[pc]==TABLESWITCH) {
      int pad = 3-(pc%4);
      long deflt  = make_s4(code, pc+pad+1);
      long low    = make_s4(code, pc+pad+5);
      long high   = make_s4(code, pc+pad+9);
      long npairs = high - low + 1;
      Util.ASSERT(low <= high);

      int result[] = new int[(int)npairs+1];
      result[0] = pc + ((int) deflt);
      for (int i=0; i<npairs; i++) {
	long offset = make_s4(code, pc+pad+13+(4*i));
	result[i+1] = pc + ((int) offset);
      }
      return result;
    }
    // Wide goto and jsr.
    if (code[pc]==GOTO_W || code[pc]==JSR_W) {
      long offset = make_s4(code, pc+1);
      int result[] = new int[1];
      result[0] = pc + ((int)offset);
      return result;
    }
    // ret/return/throw (no targets)
    if (code[pc]==RET     || code[pc]==RETURN  ||
	code[pc]==IRETURN || code[pc]==LRETURN ||
	code[pc]==FRETURN || code[pc]==DRETURN ||
	code[pc]==ARETURN || code[pc]==ATHROW  )
      return new int[0];
    // all other cases:
    {
      short offset = (short) ((ub2i(code[pc+1]) << 8) | ub2i(code[pc+2]));
      int result[] = new int[1];
      result[0] = pc+offset;
      return result;
    }
  }

  // Helper functions.
  private static final long make_u4(byte[] b, int off) {
    return make4(false, b[off], b[off+1], b[off+2], b[off+3]);
  }
  private static final long make_s4(byte[] b, int off) {
    return make4(true, b[off], b[off+1], b[off+2], b[off+3]);
  }
  private static final long make4(boolean signed, 
				  byte b1, byte b2, byte b3, byte b4) {
    long l1 = ((long) b1); // keep sign bits on msb.
    long l2 = ((long) b2) & 0xFF;
    long l3 = ((long) b3) & 0xFF;
    long l4 = ((long) b4) & 0xFF;
    if (!signed) l1 &= 0xFF;
    return (l1<<24)|(l2<<16)|(l3<<8)|l4;
  }
  /** Make an unsigned byte into an integer. */
  private static int ub2i(byte b) {
    return ((int) b) & 0xFF;
  }
}
